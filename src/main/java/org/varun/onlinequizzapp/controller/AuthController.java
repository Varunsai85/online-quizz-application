package org.varun.onlinequizzapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.varun.onlinequizzapp.dto.auth.ResendDto;
import org.varun.onlinequizzapp.dto.auth.SignInDto;
import org.varun.onlinequizzapp.dto.auth.SignUpDto;
import org.varun.onlinequizzapp.dto.auth.VerificationCodeDto;
import org.varun.onlinequizzapp.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> sigUp(@Valid @RequestBody SignUpDto signUpDto){
        return authService.signUp(signUpDto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInDto signInDto){
        return authService.signIn(signInDto);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerificationCodeDto verificationCodeDto){
        return authService.verifyCode(verificationCodeDto);
    }

    @PostMapping("/resend-mail")
    public ResponseEntity<?> resendEmail(@Valid @RequestBody ResendDto resendDto){
        return authService.resendVerificationEmail(resendDto);
    }
}
