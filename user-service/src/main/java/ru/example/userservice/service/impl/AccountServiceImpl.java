package ru.example.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.common.dto.SimpleMessageResponse;
import ru.example.common.exception.EntityNotFoundException;
import ru.example.identitydomain.entity.User;
import ru.example.userservice.mapper.AccountMapper;
import ru.example.userservice.model.response.AccountResponse;
import ru.example.userservice.repository.UserRepository;
import ru.example.userservice.service.AccountService;
import ru.example.userservice.service.MailService;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final MailService mailService;

    @Transactional
    @Override
    public AccountResponse getAccountById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Аккаунт с id " + id + " не найдет"));
        return accountMapper.toAccountResponse(user, user.getCompany());
    }

    @Transactional
    @Override
    public AccountResponse getAccountByPublicId(String publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Аккаунт с publicId " + publicId + " не найдет"));
        return accountMapper.toAccountResponse(user, user.getCompany());
    }

    @Override
    public SimpleMessageResponse changeUserStatus(String publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Аккаунт с publicId " + publicId + " не найден"));

        mailService.sendMessage(user);

        return new SimpleMessageResponse("Запрос на смену статуса успешно сформирован. Заявка будет рассмотрена администрацией в ближайшее время.");
    }

}
