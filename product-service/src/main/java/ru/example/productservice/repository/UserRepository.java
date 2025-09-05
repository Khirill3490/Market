package ru.example.productservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.common.entity.User;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicId(String publicId);
    

    boolean existsByEmail(String email);


}
