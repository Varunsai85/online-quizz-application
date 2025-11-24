package org.varun.onlinequizzapp.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.varun.onlinequizzapp.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepo.findByUsernameOrEmail(login,login).orElseThrow(()->new UsernameNotFoundException("User not found with "+login));
    }
}
