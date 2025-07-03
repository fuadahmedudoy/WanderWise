package com.example.demo.service;

import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    
@Override
public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    Pattern pattern = Pattern.compile(emailRegex);
    
    UserDetails user;

    if (pattern.matcher(login).matches()) {
        // If the login string is an email, search by email
        user = userRepository.findByEmail(login).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + login);
        }
    } else {
        // Otherwise, search by username
        user = userRepository.findByUsername(login).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + login);
        }
    }
    
    return user;
}
}
