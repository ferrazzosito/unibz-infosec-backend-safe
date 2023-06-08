package it.unibz.infosec.examproject.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanitizedUserRepository
        extends JpaRepository<UserEntity, Long>, ISanitizedUserRepository {
}
