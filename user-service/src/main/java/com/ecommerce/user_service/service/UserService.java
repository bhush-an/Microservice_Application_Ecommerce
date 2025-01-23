package com.ecommerce.user_service.service;

import com.ecommerce.user_service.repository.IUserRepository;
import com.ecommerce.user_service.dto.CredentialDTO;
import com.ecommerce.user_service.dto.JWTDTO;
import com.ecommerce.user_service.dto.ResponseDTO;
import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.entity.UserRole;
import com.ecommerce.user_service.utils.JwtUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    @Autowired
    private IUserRepository userRepo;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private JwtUtils utils;

    @Value("${EXP_TIMEOUT}")
    private String jwtExpiry;

    public ResponseDTO saveUser(UserDTO userDTO) {
        User user = mapper.map(userDTO, User.class);
        user.setPassword(encoder.encode(user.getPassword()));
        User persistentUser = userRepo.save(user);
        UserDTO userResponseDTO = mapper.map(persistentUser, UserDTO.class);
        String responseMessage = getMessageBasedOnRole(persistentUser);
        return ResponseDTO.builder()
                .message(responseMessage)
                .userDetails(userResponseDTO)
                .build();
    }

    private static String getMessageBasedOnRole(User persistentUser) {
        String responseMessage;
        if (persistentUser.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            responseMessage = "ADMIN created successfully with User ID: " + persistentUser.getUserId();
        } else if (persistentUser.getUserRole().equals(UserRole.ROLE_CUSTOMER)) {
            responseMessage = "CUSTOMER created successfully with User ID: " + persistentUser.getUserId();
        } else {
            responseMessage = "CUSTOMER created successfully with User ID: " + persistentUser.getUserId();
        }
        return responseMessage;
    }

    public ResponseDTO loginUser(CredentialDTO credentialDTO) {
        User user = userRepo.findByEmailId(credentialDTO.getEmailId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid User: " + credentialDTO.getEmailId()));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(credentialDTO.getEmailId(),
                credentialDTO.getPassword());
        Authentication authenticationDetails = manager.authenticate(authToken);
        try {
            JWTDTO accessTokenDetails = JWTDTO.builder()
                    .jwt(utils.generateJwtToken(authenticationDetails))
                    .expiresInMin((Long.parseLong(jwtExpiry)) / (60 * 1000))
                    .build();
            return ResponseDTO.builder().message("Authentication successful!")
                    .accessTokenDetails(accessTokenDetails)
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Credentials! Please try again.");
        }
    }

    public String checkCustomer(String email) {
        User user = userRepo.findByEmailId(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid User: " + email));
        if (user.getUserRole().equals(UserRole.ROLE_ADMIN)) {
            throw new RuntimeException("User has ADMIN role.");
        }
        return user.getUserId();
    }
}
