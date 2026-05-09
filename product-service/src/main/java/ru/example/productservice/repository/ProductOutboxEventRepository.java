package ru.example.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.example.productservice.entity.ProductOutboxEvent;

import java.util.List;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, Long> {

    @Query(
            value = """
                    select *
                    from product_outbox_events
                    where status = :status
                    order by created_at
                    limit :batchSize
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<ProductOutboxEvent> findBatchForPublishing(
            @Param("status") String status,
            @Param("batchSize") int batchSize
    );
}