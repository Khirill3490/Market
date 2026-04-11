package ru.example.authmodule.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.authmodule.exception.EntityAlreadyExistsException;
import ru.example.authmodule.exception.ErrorMessageGlobal;
import ru.example.authmodule.mapper.UserAndCompanyMapper;
import ru.example.authmodule.model.request.UserRegRequest;
import ru.example.authmodule.repository.CompanyRepository;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.authmodule.service.ActivationService;
import ru.example.authmodule.service.AccountService;
import ru.example.identitydomain.entity.Company;
import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.enums.RoleType;
import ru.example.identitydomain.entity.enums.RulesType;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;
    private final UserAndCompanyMapper userAndCompanyMapper;
    private final AccountService accountService;

    public void register(UserRegRequest request) {
        if (companyRepository.existsByInn(request.getInn())) {
            throw new EntityAlreadyExistsException("Компания с данным ИНН уже зарегистрирована");
        }

        Optional<Account> userOptional = accountRepository.findByEmailEqualsIgnoreCase(request.getEmail());

        if (userOptional.isPresent()) {
            Account accountDb = userOptional.get();

            if (accountDb.isActive()) {
                throw new EntityAlreadyExistsException(
                        "Ошибка. Пользователь с email: " + request.getEmail() + " уже существует"
                );
            }

            throw new ErrorMessageGlobal(
                    "Сообщение с ссылкой для подтверждения аккаунта уже было отправлено на email"
            );
        }

        Company company = userAndCompanyMapper.toCompany(request);

        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(false)
                .role(RoleType.ROLE_USER)
                .rules(RulesType.BUYER)
                .company(company)
                .build();

        company.setOwner(account);

        Account savedAccount = accountService.save(account);
        activationService.sendUri(savedAccount.getPublicId(), savedAccount.getEmail());
    }
}