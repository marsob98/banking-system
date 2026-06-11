package com.bank.banking_system.user;

import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.customer.CustomerRepository;
import com.bank.banking_system.exception.DuplicatePeselException;
import com.bank.banking_system.exception.DuplicateUsernameException;
import com.bank.banking_system.security.JwtService;
import com.bank.banking_system.user.dto.AuthResponse;
import com.bank.banking_system.user.dto.LoginRequest;
import com.bank.banking_system.user.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository,
                       JwtService jwtService, AuthenticationManager authenticationManager,
                       CustomerRepository customerRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public User register(RegisterRequest request) {
        userRepository.findByUsername(request.username())
                .ifPresent(existing -> {
                    throw new DuplicateUsernameException("Username already taken");});
        customerRepository.findByPesel(request.pesel())
                .ifPresent(e -> {throw new DuplicatePeselException("Pesel already exists");});


        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPesel(request.pesel());
        Customer savedCustomer = customerRepository.save(customer);

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setCustomer(savedCustomer);

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(), request.password()));

        String token = jwtService.generateToken(authentication.getName());
        return new AuthResponse(token);
    }
}
