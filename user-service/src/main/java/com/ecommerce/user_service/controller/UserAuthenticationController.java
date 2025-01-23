package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.CredentialDTO;
import com.ecommerce.user_service.dto.ResponseDTO;
import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class UserAuthenticationController {

    @Autowired
    private UserService userService;

    @PostMapping("register")
    public ResponseEntity<ResponseDTO> registerNewUser(@RequestBody @Valid UserDTO userDTO) {
        return new ResponseEntity<>(userService.saveUser(userDTO), HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<ResponseDTO> authenticateUser(@RequestBody @Valid CredentialDTO credentialDTO) {
        return new ResponseEntity<>(userService.loginUser(credentialDTO), HttpStatus.CREATED);
    }

    @GetMapping("checkCustomer/{email}")
    public ResponseEntity<String> checkCustomer(@PathVariable String email) {
        return new ResponseEntity<>(userService.checkCustomer(email), HttpStatus.OK);
    }
}
