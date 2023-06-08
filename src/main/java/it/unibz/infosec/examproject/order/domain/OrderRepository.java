package it.unibz.infosec.examproject.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @NonNull
    @Override
    Optional<Order> findById(@NonNull Long id);

    @NonNull
    List<Order> findByClientId(@NonNull Long customer);

    @NonNull
    List<Order> findByVendorId(@NonNull Long vendor);

    @NonNull
    List<Order> findByVendorIdAndIsApprovedTrue(@NonNull Long vendor);

    @NonNull
    List<Order> findByVendorIdAndIsApprovedFalse(@NonNull Long vendor);

    @NonNull
    List<Order> findByProductId(@NonNull Long productId);
}
