package com.gateway.authenticationservice.service;




import com.gateway.authenticationservice.model.User;
import com.gateway.authenticationservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gateway.authenticationservice.model.Role; // Asegurarse de importar Role
import java.util.List; // Importar List
import java.util.Optional; // Importar Optional


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }


    public Optional<User> updateUserStatus(Long id, boolean isEnabled) {
        return userRepository.findById(id).map(user -> {
            user.setEnabled(isEnabled);
            return userRepository.save(user);
        });
    }


    public Optional<User> updateUserRole(Long id, Role newRole) {
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            return userRepository.save(user);
        });
    }








}


