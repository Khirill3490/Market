package ru.example.authmodule.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.example.common.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByInn(String inn);
}
