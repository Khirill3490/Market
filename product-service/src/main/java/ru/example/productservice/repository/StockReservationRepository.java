package ru.example.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.productservice.entity.StockReservation;
import ru.example.productservice.entity.StockReservationStatus;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findAllByOrderPublicIdAndStatus(
            String orderPublicId,
            StockReservationStatus status
    );
}