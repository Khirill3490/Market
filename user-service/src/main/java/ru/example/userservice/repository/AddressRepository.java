package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.identitydomain.entity.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByAccountIdOrderByIsDefaultDescCreatedAtDesc(Long accountId);

    Optional<Address> findByPublicIdAndAccountId(String publicId, Long accountId);

    Optional<Address> findByAccountIdAndIsDefaultTrue(Long accountId);
}