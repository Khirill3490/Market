package ru.example.productservice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.identitydomain.entity.Account;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByPublicId(String publicId);
    

    boolean existsByEmail(String email);


}
