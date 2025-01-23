package com.ecommerce.order_service.service;

import com.ecommerce.order_service.config.InventoryServiceProxy;
import com.ecommerce.order_service.config.PaymentServiceProxy;
import com.ecommerce.order_service.config.ProductServiceProxy;
import com.ecommerce.order_service.config.UserServiceProxy;
import com.ecommerce.order_service.dto.InventoryDTO;
import com.ecommerce.order_service.dto.OrderDTO;
import com.ecommerce.order_service.dto.ProductDetailsDTO;
import com.ecommerce.order_service.dto.ResponseDTO;
import com.ecommerce.order_service.entity.Order;
import com.ecommerce.order_service.entity.PaymentStatus;
import com.ecommerce.order_service.entity.ProductDetails;
import com.ecommerce.order_service.event.OrderPlacedEvent;
import com.ecommerce.order_service.exception.*;
import com.ecommerce.order_service.repository.IOrderRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private ProductServiceProxy productProxy;

    @Autowired
    private InventoryServiceProxy inventoryProxy;

    @Autowired
    private PaymentServiceProxy paymentProxy;

    @Autowired
    private UserServiceProxy userProxy;

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Autowired
    private IOrderRepository orderRepo;

    public ResponseDTO getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        List<OrderDTO> orderDTOS = orders.stream()
                .map(order -> mapper.map(order, OrderDTO.class))
                .toList();
        return ResponseDTO.builder()
                .message("Displaying all orders below.")
                .listOfOrderDetails(orderDTOS)
                .build();
    }

    public ResponseDTO getSpecificOrderDetails(String orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Invalid Order ID: " + orderId));
        OrderDTO orderDTO = mapper.map(order, OrderDTO.class);
        return ResponseDTO.builder()
                .message("Displaying Order Details for Order ID: " + orderId)
                .orderDetails(orderDTO)
                .build();
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "productServiceFallback")
    public ResponseDTO getAllProducts() {
        return productProxy.fetchAllProducts().getBody();
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "productServiceFallback")
    public ResponseDTO getProductByProductName(String product) {
        return productProxy.getProductDetailsById(product).getBody();
    }

//    public ResponseDTO createOrder(List<ProductDetailsDTO> productDetailsDTOList) {
//        BigDecimal total = BigDecimal.ZERO;
//        Map<String, BigDecimal> productPriceMap = new HashMap<>();
//
//        for (ProductDetailsDTO productDetailsDTO : productDetailsDTOList) {
//            ResponseDTO inventoryResponse;
//            try {
//                inventoryResponse = inventoryProxy.getProductFromInventory(productDetailsDTO.getProduct())
//                        .getBody();
//            } catch (FeignException.NotFound e) {
//                throw new ProductNotFoundException("Invalid Product: " + productDetailsDTO.getProduct());
//            }
//            if (inventoryResponse == null) {
//                throw new ProductNotFoundException("Invalid Product: " + productDetailsDTO.getProduct());
//            }
//            InventoryDTO inventoryDTO = inventoryResponse.getInventoryDetails();
//            if (productDetailsDTO.getQuantity() > inventoryDTO.getQuantity()) {
//                return ResponseDTO.builder()
//                        .message("Insufficient quantity for product: " + productDetailsDTO.getProduct())
//                        .errorMessage("Only " + inventoryDTO.getQuantity() + " unit/units available in inventory.")
//                        .build();
//            }
//            ResponseDTO productResponse = productProxy.getProductDetailsById(productDetailsDTO.getProduct()).getBody();
//            assert productResponse != null;
//
//            BigDecimal price = productResponse.getProductDetails().getPrice();
//            productPriceMap.put(productDetailsDTO.getProduct(), price);
//
//            int quantity = productDetailsDTO.getQuantity();
//            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
//        }
//        Order order = new Order();
//        Set<ProductDetails> productDetailsSet = productDetailsDTOList.stream()
//                .map(productDetailsDTO -> {
//                    ProductDetails productDetails = mapper.map(productDetailsDTO, ProductDetails.class);
//                    productDetails.setUnitPrice(productPriceMap.get(productDetailsDTO.getProduct()));
//                    return productDetails;
//                })
//                .collect(Collectors.toSet());
//        order.setProducts(productDetailsSet);
//        order.setTotalAmount(total);
//        order.setPaymentStatus(PaymentStatus.PENDING);
//        Order savedOrder = orderRepo.save(order);
//
//        productDetailsDTOList.forEach(productDetails -> {
//            InventoryDTO inventoryDTO = mapper.map(productDetails, InventoryDTO.class);
//            inventoryProxy.updateProductInInventory(inventoryDTO);
//        });
//
//        ResponseEntity<String> linkResponse = paymentProxy.createPaymentLink(savedOrder.getOrderId(), total);
//        if (linkResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(500))) {
//            throw new RuntimeException("Something went wrong in Payment Service!");
//        }
//        String paymentUrl = linkResponse.getBody();
//
//        OrderDTO savedOrderDTO = mapper.map(savedOrder, OrderDTO.class);
//        return ResponseDTO.builder()
//                .message("Order created successfully! Order ID: " + savedOrder.getOrderId())
//                .paymentUrl(paymentUrl)
//                .orderDetails(savedOrderDTO)
//                .build();
//    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceFallback")
    public ResponseDTO createOrder(List<ProductDetailsDTO> productDetailsDTOList, String customer) {
        if (customer == null) {
            throw new MissingCustomerException("Missing Customer in Headers section!");
        }
        String customerId;
        try {
            customerId = userProxy.checkCustomer(customer);
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException("Invalid Customer: " + customer);
        }
        // Validate products and calculate total price
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> productPriceMap = new HashMap<>();
        for (ProductDetailsDTO productDetailsDTO : productDetailsDTOList) {
            total = total.add(validateProductAndCalculatePrice(productDetailsDTO, productPriceMap));
        }
        // Create and save order
        Order savedOrder = saveOrder(productDetailsDTOList, productPriceMap, total, customerId);

        // Update inventory based on product details
        productDetailsDTOList.forEach(productDetails -> {
            InventoryDTO inventoryDTO = mapper.map(productDetails, InventoryDTO.class);
            inventoryProxy.updateProductInInventory(inventoryDTO);
        });

        // Generate payment link
        String paymentUrl = createPaymentLink(savedOrder, total);

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
        orderPlacedEvent.setOrderId(savedOrder.getOrderId());
        orderPlacedEvent.setEmail(customer);
        orderPlacedEvent.setPaymentUrl(paymentUrl);
        kafkaTemplate.send("order-placed", orderPlacedEvent);

        OrderDTO savedOrderDTO = mapper.map(savedOrder, OrderDTO.class);
        return ResponseDTO.builder()
                .message("Order created successfully! Order ID: " + savedOrder.getOrderId())
                .paymentUrl(paymentUrl)
                .orderDetails(savedOrderDTO)
                .build();
    }

    private BigDecimal validateProductAndCalculatePrice(ProductDetailsDTO productDetailsDTO, Map<String, BigDecimal> productPriceMap) {
        ResponseDTO inventoryResponse = getInventoryResponse(productDetailsDTO);
        InventoryDTO inventoryDTO = inventoryResponse.getInventoryDetails();

        // Check available quantity
        if (productDetailsDTO.getQuantity() > inventoryDTO.getQuantity()) {
            throw new InsufficientQuantityException("Only " + inventoryDTO.getQuantity() +
                    " unit/units available in inventory for product: " + productDetailsDTO.getProduct());
        }

        // Get product price and calculate total
        BigDecimal price = getProductPrice(productDetailsDTO);
        productPriceMap.put(productDetailsDTO.getProduct(), price);
        return price.multiply(BigDecimal.valueOf(productDetailsDTO.getQuantity()));
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceFallback")
    private ResponseDTO getInventoryResponse(ProductDetailsDTO productDetailsDTO) {
        try {
            return inventoryProxy.getProductFromInventory(productDetailsDTO.getProduct()).getBody();
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException("Invalid Product: " + productDetailsDTO.getProduct());
        }
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "productServiceGetProductPriceFallback")
    private BigDecimal getProductPrice(ProductDetailsDTO productDetailsDTO) {
        ResponseDTO productResponse = productProxy.getProductDetailsById(productDetailsDTO.getProduct()).getBody();
        if (productResponse == null) {
            throw new ProductNotFoundException("Product details not found: " + productDetailsDTO.getProduct());
        }
        return productResponse.getProductDetails().getPrice();
    }

    private Order saveOrder(List<ProductDetailsDTO> productDetailsDTOList, Map<String, BigDecimal> productPriceMap,
                            BigDecimal total, String customerId) {
        Order order = new Order();
        Set<ProductDetails> productDetailsSet = productDetailsDTOList.stream()
                .map(productDetailsDTO -> {
                    ProductDetails productDetails = mapper.map(productDetailsDTO, ProductDetails.class);
                    productDetails.setUnitPrice(productPriceMap.get(productDetailsDTO.getProduct()));
                    return productDetails;
                })
                .collect(Collectors.toSet());

        order.setCustomer(customerId);
        order.setProducts(productDetailsSet);
        order.setTotalAmount(total);
        order.setPaymentStatus(PaymentStatus.PENDING);
        return orderRepo.save(order);
    }

    @CircuitBreaker(name = "payment-service", fallbackMethod = "paymentServiceFallback")
    private String createPaymentLink(Order savedOrder, BigDecimal total) {
        ResponseEntity<String> linkResponse = paymentProxy.createPaymentLink(savedOrder.getOrderId(), total);
        if (linkResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(500))) {
            throw new RuntimeException("Something went wrong in Payment Service!");
        }
        return linkResponse.getBody();
    }

    public ResponseDTO createOrderPaymentLink(String orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Invalid Order ID: " + orderId));
        if (order.getPaymentStatus().equals(PaymentStatus.SUCCESS)) {
            return ResponseDTO.builder()
                    .message("Payment is already successful for Order ID: " + orderId)
                    .build();
        } else if (order.getPaymentStatus().equals(PaymentStatus.FAILED)) {
            return ResponseDTO.builder()
                    .message("Payment is already failed for Order ID: " + orderId + " | Please create new order.")
                    .build();
        }
        // Generate payment link
        String paymentUrl = createPaymentLink(order, order.getTotalAmount());

        OrderDTO savedOrderDTO = mapper.map(order, OrderDTO.class);
        return ResponseDTO.builder()
                .message("Order was already created for Order ID: " + orderId + " | Please find payment link below.")
                .paymentUrl(paymentUrl)
                .orderDetails(savedOrderDTO)
                .build();
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceUpdateFallback")
    public void updatePaymentStatus(String orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Invalid Order ID: " + orderId));
        if (status.equalsIgnoreCase(PaymentStatus.SUCCESS.name())) {
            order.setPaymentStatus(PaymentStatus.SUCCESS);
        } else if (status.equalsIgnoreCase(PaymentStatus.FAILED.name())) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            for (ProductDetails productDetails : order.getProducts()) {
                InventoryDTO inventoryDTO = new InventoryDTO();
                inventoryDTO.setProduct(productDetails.getProduct());
                inventoryDTO.setQuantity(productDetails.getQuantity());
                inventoryProxy.increaseProductInInventory(inventoryDTO);
            }
        }
        orderRepo.save(order);
    }

    public ResponseDTO productServiceFallback(Throwable t) {
        throw new ServiceNotAvailableException("product-service");
    }

    public BigDecimal productServiceGetProductPriceFallback(Throwable t) {
        throw new ServiceNotAvailableException("product-service");
    }

    public ResponseDTO inventoryServiceFallback(Throwable t) {
        throw new ServiceNotAvailableException("inventory-service");
    }

    public void inventoryServiceUpdateFallback(Throwable t) {
        throw new ServiceNotAvailableException("inventory-service");
    }

    public String paymentServiceFallback(Throwable t) {
        throw new ServiceNotAvailableException("payment-service");
    }

}
