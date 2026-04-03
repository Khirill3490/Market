package ru.example.authmodule.service;




import ru.example.identitydomain.entity.Company;

import java.util.List;

public interface CompanyService {

    List<Company> findAll();
    Company findById(int id);
    Company save(Company company);
    Company update(Company company);
    void deleteById(int id);

}
