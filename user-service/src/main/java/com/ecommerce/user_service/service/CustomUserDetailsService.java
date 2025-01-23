package com.ecommerce.user_service.service;

import com.ecommerce.user_service.repository.IUserRepository;
import com.ecommerce.user_service.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmailId(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid User: " + username));
        return new CustomUserDetails(user);
    }
}
