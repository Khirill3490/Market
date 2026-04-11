package ru.example.authmodule.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.example.authmodule.exception.EntityNotFoundException;
import ru.example.authmodule.repository.AccountRepository;
import ru.example.identitydomain.entity.Account;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new AppUserPrincipal(accountRepository.findByEmailEqualsIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found")));
    }

    public UserDetails loadUserByPublicId(String publicId) throws UsernameNotFoundException {
        Account account = accountRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        return new AppUserPrincipal(account);
    }
}

