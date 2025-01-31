package it.unibz.infosec.examproject.user.domain;

import org.springframework.lang.NonNull;

import java.util.Optional;

public interface ISanitizedUserRepository {
    @NonNull
    Optional<UserEntity> findByEmail(String email);
    @NonNull
    Optional<UserEntity> findById(Long id);
}
