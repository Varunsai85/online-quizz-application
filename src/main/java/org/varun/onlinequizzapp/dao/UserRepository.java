package org.varun.onlinequizzapp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.varun.onlinequizzapp.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameOrEmail(String username, String email);
}
