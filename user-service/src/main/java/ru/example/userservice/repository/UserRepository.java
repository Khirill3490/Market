package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.common.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicId(String publicId);
}
