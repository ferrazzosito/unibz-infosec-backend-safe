package it.unibz.infosec.examproject.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanitizedProductRepository
        extends JpaRepository<Product, Long>, ISanitizedProductRepository {
}
