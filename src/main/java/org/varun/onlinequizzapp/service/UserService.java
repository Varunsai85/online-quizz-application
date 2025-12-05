package org.varun.onlinequizzapp.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.dto.ApiResponse;
import org.varun.onlinequizzapp.dto.user.UserResponseDto;
import org.varun.onlinequizzapp.model.User;
import org.varun.onlinequizzapp.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepo.findByUsernameOrEmail(login, login).orElseThrow(() -> new UsernameNotFoundException("User not found with " + login));
    }

    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepo.findAll();
        List<UserResponseDto> userResponseDtoList = users.stream().map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isEnabled())).toList();
        return new ResponseEntity<>(new ApiResponse<>(true, "Fetched all users", userResponseDtoList), HttpStatus.OK);
    }
}
