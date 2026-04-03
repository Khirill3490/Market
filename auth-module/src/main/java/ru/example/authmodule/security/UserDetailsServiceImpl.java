package ru.example.authmodule.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.example.authmodule.repository.UserRepository;
import ru.example.common.exception.EntityNotFoundException;
import ru.example.identitydomain.entity.User;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Вызов loadUserByUsername: " + username);
        return new AppUserPrincipal(userRepository.findByEmailEqualsIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + username + " not found")));
    }

    public UserDetails loadUserByPublicId(String publicId) throws UsernameNotFoundException {
        System.out.println("Вызов loadUserByPublicId: " + publicId);
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        return new AppUserPrincipal(user);
    }
}
