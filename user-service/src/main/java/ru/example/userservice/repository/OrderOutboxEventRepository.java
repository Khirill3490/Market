package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.example.userservice.entity.OrderOutboxEvent;

import java.util.List;

public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, Long> {

    @Query(
            value = """
                    select *
                    from order_outbox_events
                    where status = :status
                    order by created_at
                    limit :batchSize
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<OrderOutboxEvent> findBatchForPublishing(
            @Param("status") String status,
            @Param("batchSize") int batchSize
    );
}