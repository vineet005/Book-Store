package com.book.store.service.userservice;

import com.book.store.dto.userdto.LoginDTO;
import com.book.store.dto.userdto.UserDTO;
import com.book.store.jwtutil.userjwtutil.UserJwtUtil;
import com.book.store.model.cartmodel.Cart;
import com.book.store.model.usermodel.User;
import com.book.store.repository.cartrepository.CartRepository;
import com.book.store.repository.userrepository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService implements UserServiceI {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserJwtUtil userJwtUtil;

    @Autowired
    private CartRepository cartRepository;

    @Override
    public void registerUser(UserDTO userDTO){
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode( userDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        User saveUser = userRepository.save(user);

        Cart cart=new Cart();
        cart.setUser(saveUser);
        cart.setCreatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    public String loginUser(LoginDTO loginDTO) {
         User  user =  userRepository.findByUsername( loginDTO.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (passwordEncoder.matches( loginDTO.getPassword(),  user.getPassword())) {
            return  userJwtUtil.generateToken( user.getUsername());
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public boolean verifyToken(String token) {
    String username = userJwtUtil.extractUsername(token);
    Optional<User> userOptional = userRepository.findByUsername(username);

    if (userOptional.isPresent()) {
        User user = userOptional.get();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
        return userJwtUtil.validateToken(token, userDetails);
    }
    return false;
   }

//    @Transactional
//    public UserDTO updateUser(UserDTO userDTO) {
//        User user = userRepository.findByEmail(userDTO.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        user.setUsername(userDTO.getUsername());
//        user.setAddress(userDTO.getAddress());
//        user.setPhoneNumber(userDTO.getPhoneNumber());
//
//        userRepository.save(user);
//
//        return userDTO;
//    }

}
