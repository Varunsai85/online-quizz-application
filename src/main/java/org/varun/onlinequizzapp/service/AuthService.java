package org.varun.onlinequizzapp.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.SignInDto;
import org.varun.onlinequizzapp.dto.SignUpDto;
import org.varun.onlinequizzapp.dto.VerificationCodeDto;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public ResponseEntity<?> signUp(@Valid SignUpDto signUpDto) {
        //Check if username exists
        Optional<User> userNameExistingUser = userRepo.findUserByUsername(signUpDto.username());
        if (userNameExistingUser.isPresent()) {
            return new ResponseEntity<>(new ApiResponse<>("Username not available"), HttpStatus.BAD_REQUEST);
        }

        //Check if email exists
        Optional<User> existingUser = userRepo.findUserByEmail(signUpDto.email());
        if (existingUser.isPresent()) {
            //Check if User is verified?
            if (existingUser.get().isEnabled()) {
                return new ResponseEntity<>(new ApiResponse<>("User with this email already exists"), HttpStatus.BAD_REQUEST);
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
        try {
            userRepo.save(newUser);
            sendVerificationEmail(newUser);
            return new ResponseEntity<>(new ApiResponse<>("User registered successfully, Please verify your account"), HttpStatus.CREATED);
        } catch (MessagingException e) {
            log.error("[Sign-up] Verification failed for {} {}", newUser.getEmail(), e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Failed to send verification email"), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[Sign-up] Encountered error in AuthService {}", e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> signIn(@Valid SignInDto signInDto) {
        try {
            User user = userRepo.findByUsernameOrEmail(signInDto.login(), signInDto.login()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInDto.login(), signInDto.password()));
            String jwtToken = jwtService.generateJwtToken(user.getEmail());
            return new ResponseEntity<>(new ApiResponse<>("User logged in successfully", jwtToken), HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            log.error("[Sign-in] User {} not found", signInDto.login());
            return new ResponseEntity<>(new ApiResponse<>("User not found", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (DisabledException e) {
            log.error("[Sign-in] User {}, is not verified", signInDto.login());
            return new ResponseEntity<>(new ApiResponse<>("Please verify your account", e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException e) {
            log.error("[Sign-in] Wrong credentials for user {}", signInDto.login());
            return new ResponseEntity<>(new ApiResponse<>("Invalid credentials", e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("[Sign-in] Encountered error in AuthService");
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> verifyCode(@Valid VerificationCodeDto verificationCodeDto) {
        try {
            User user = userRepo.findUserByEmail(verificationCodeDto.email()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (user.getCodeExpiresIn().isBefore(LocalDateTime.now())) {
                log.warn("[Verify-code] verification code expired for user {}", verificationCodeDto.email());
                return new ResponseEntity<>(new ApiResponse<>("Verification code expired"), HttpStatus.FORBIDDEN);
            }
            if (user.getVerificationCode().equals(verificationCodeDto.code())) {
                user.setCodeExpiresIn(null);
                user.setVerificationCode(null);
                user.setEnabled(true);
                userRepo.save(user);
                return new ResponseEntity<>(new ApiResponse<>("User verified successfully"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse<>("Invalid verification code"), HttpStatus.UNAUTHORIZED);
            }
        } catch (UsernameNotFoundException e) {
            log.error("[Verify-code] User {}, not found", verificationCodeDto.email());
            return new ResponseEntity<>(new ApiResponse<>("User not found", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("[Verify-code] Encountered error in AuthService");
            return new ResponseEntity<>(new ApiResponse<>("Something went wrong", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> resendVerificationEmail(@Valid String email) {
        User user = userRepo.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        try {
            if (!user.isEnabled()) {
                user.setVerificationCode(generateVerificationCode());
                user.setCodeExpiresIn(LocalDateTime.now().plusMinutes(10));
                userRepo.save(user);
                sendVerificationEmail(user);
                return new ResponseEntity<>(new ApiResponse<>("Verification mail resent"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse<>("User already verified"), HttpStatus.BAD_REQUEST);
            }
        } catch (MessagingException e) {
            log.error("[resend-verify-mail] Verification failed for {} {}", user.getEmail(), e.getMessage());
            return new ResponseEntity<>(new ApiResponse<>("Failed sending verification mail"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("[resend-verify-mail] Encountered error in AuthService {}", e.getMessage());
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
