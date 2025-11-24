package org.varun.onlinequizzapp.service;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.SignUpDto;
import org.varun.onlinequizzapp.model.User;
import org.varun.onlinequizzapp.model.type.Role;
import org.varun.onlinequizzapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    public ResponseEntity<?> signUp(@Valid SignUpDto signUpDto) {
        Optional<User> existingUser = userRepo.findUserByEmail(signUpDto.email());
        if (existingUser.isPresent()) {
            if (existingUser.get().isEnabled()) {
                return new ResponseEntity<>(new ApiResponse<>("User already exists"), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(new ApiResponse<>("User already exists, but not verified"), HttpStatus.BAD_REQUEST);
        }
        User newUser = User.builder()
                .username(signUpDto.username())
                .email(signUpDto.email())
                .password(encoder.encode(signUpDto.password()))
                .isEnabled(false)
                .verificationCode(generateVerificationCode())
                .codeExpiresIn(LocalDateTime.now().plusMinutes(10))
                .role(Role.USER)
                .build();
        userRepo.save(newUser);
        try {
            sendVerificationEmail(newUser);
            return new ResponseEntity<>(new ApiResponse<>("User registered successfully, Please verify your account"), HttpStatus.CREATED);
        } catch (MessagingException e) {
            log.error("Verification failed for {} {}", newUser.getEmail(), e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Failed to send verification email"), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Something went wrong {}", e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendVerificationEmail(User user) throws MessagingException {
        String subject = "Account Verification";
        String verificationCode = "Verification Code " + user.getVerificationCode();
        String htmlText = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        emailService.sendEmail(user.getEmail(), subject, htmlText);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
