package com.bl21.service;
import com.bl21.dto.request.LoginRequest;
import com.bl21.dto.request.RegisterRequest;
import com.bl21.entity.Role;
import com.bl21.entity.User;
import com.bl21.repository.RoleRepository;
import com.bl21.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
@Service
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(
                request.getUsername()
        )) {

            throw new RuntimeException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }

        Role userRole = roleRepository
                .findByName("USER")
                .orElseThrow(() ->
                        new RuntimeException("USER role not found"));

        User user = new User();

        user.setUsername(request.getUsername());

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.getRoles().add(userRole);

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        try {

            authenticationManager.authenticate(

                    new UsernamePasswordAuthenticationToken(

                            request.getUsername(),

                            request.getPassword()
                    )
            );

            return jwtService.generateToken(
                    request.getUsername()
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Invalid credentials"
            );
        }
    }
}