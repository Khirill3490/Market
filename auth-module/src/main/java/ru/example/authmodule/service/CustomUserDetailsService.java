//package ru.example.authmodule.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import ru.example.authmodule.repository.UserRepository;
//import ru.example.authmodule.security.AppUserPrincipal;
//import ru.example.identitydomain.entity.User;
//import ru.example.common.exception.EntityNotFoundException;
//
//@Service("customUserDetailsService")
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String publicId) throws UsernameNotFoundException {
//        User user = userRepository.findByPublicId(publicId)
//                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
//
//        return new AppUserPrincipal(user);
//    }
//}
