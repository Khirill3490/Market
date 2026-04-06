package ru.example.authmodule.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.identitydomain.entity.Company;

import java.util.Optional;


@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByInn(String inn);

    Optional<Company> findByInn(String inn);


}
