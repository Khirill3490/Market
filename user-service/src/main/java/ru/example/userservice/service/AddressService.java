package ru.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.identitydomain.entity.Account;
import ru.example.identitydomain.entity.Address;
import ru.example.userservice.exception.EntityNotFoundException;
import ru.example.userservice.exception.IncorrectDataException;
import ru.example.userservice.model.request.CreateAddressRequest;
import ru.example.userservice.model.request.UpdateAddressRequest;
import ru.example.userservice.model.response.AddressResponse;
import ru.example.userservice.repository.AccountRepository;
import ru.example.userservice.repository.AddressRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getCurrentUserAddresses(Jwt jwt) {
        Account account = getCurrentAccount(jwt);

        return addressRepository.findAllByAccountIdOrderByIsDefaultDescCreatedAtDesc(account.getId())
                .stream()
                .map(this::mapToAddressResponse)
                .toList();
    }

    @Transactional
    public AddressResponse createAddress(Jwt jwt, CreateAddressRequest request) {
        Account account = getCurrentAccount(jwt);

        boolean shouldBeDefault = Boolean.TRUE.equals(request.isDefault())
                || addressRepository.findByAccountIdAndIsDefaultTrue(account.getId()).isEmpty();

        if (shouldBeDefault) {
            addressRepository.findByAccountIdAndIsDefaultTrue(account.getId())
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        Address address = Address.builder()
                .publicId(generatePublicId())
                .account(account)
                .label(normalizeNullable(request.label()))
                .country(normalizeRequired(request.country(), "Страна обязательна"))
                .region(normalizeNullable(request.region()))
                .city(normalizeRequired(request.city(), "Город обязателен"))
                .street(normalizeRequired(request.street(), "Улица обязательна"))
                .house(normalizeRequired(request.house(), "Номер дома обязателен"))
                .apartment(normalizeNullable(request.apartment()))
                .postalCode(normalizeNullable(request.postalCode()))
                .comment(normalizeNullable(request.comment()))
                .isDefault(shouldBeDefault)
                .build();

        Address savedAddress = addressRepository.save(address);

        return mapToAddressResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(
            Jwt jwt,
            String addressPublicId,
            UpdateAddressRequest request
    ) {
        Account account = getCurrentAccount(jwt);

        Address address = addressRepository.findByPublicIdAndAccountId(addressPublicId, account.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Адрес текущего пользователя не найден"
                ));

        if (request.label() != null) {
            address.setLabel(normalizeNullable(request.label()));
        }
        if (request.country() != null) {
            address.setCountry(normalizeRequired(request.country(), "Страна обязательна"));
        }
        if (request.region() != null) {
            address.setRegion(normalizeNullable(request.region()));
        }
        if (request.city() != null) {
            address.setCity(normalizeRequired(request.city(), "Город обязателен"));
        }
        if (request.street() != null) {
            address.setStreet(normalizeRequired(request.street(), "Улица обязательна"));
        }
        if (request.house() != null) {
            address.setHouse(normalizeRequired(request.house(), "Номер дома обязателен"));
        }
        if (request.apartment() != null) {
            address.setApartment(normalizeNullable(request.apartment()));
        }
        if (request.postalCode() != null) {
            address.setPostalCode(normalizeNullable(request.postalCode()));
        }
        if (request.comment() != null) {
            address.setComment(normalizeNullable(request.comment()));
        }

        if (Boolean.TRUE.equals(request.isDefault()) && !address.isDefault()) {
            addressRepository.findByAccountIdAndIsDefaultTrue(account.getId())
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        addressRepository.save(existingDefault);
                    });

            address.setDefault(true);
        }

        Address savedAddress = addressRepository.save(address);
        return mapToAddressResponse(savedAddress);
    }

    @Transactional
    public void deleteAddress(Jwt jwt, String addressPublicId) {
        Account account = getCurrentAccount(jwt);

        Address address = addressRepository.findByPublicIdAndAccountId(addressPublicId, account.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Адрес текущего пользователя не найден"
                ));

        addressRepository.delete(address);
    }

    private Account getCurrentAccount(Jwt jwt) {
        String keycloakUserId = requireKeycloakUserId(jwt);

        return accountRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Локальный account для текущего пользователя не найден"
                ));
    }

    private String requireKeycloakUserId(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();

        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IncorrectDataException("В JWT отсутствует subject пользователя");
        }

        return keycloakUserId;
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .publicId(address.getPublicId())
                .label(address.getLabel())
                .country(address.getCountry())
                .region(address.getRegion())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .apartment(address.getApartment())
                .postalCode(address.getPostalCode())
                .comment(address.getComment())
                .isDefault(address.isDefault())
                .build();
    }

    private String generatePublicId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IncorrectDataException(message);
        }

        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}