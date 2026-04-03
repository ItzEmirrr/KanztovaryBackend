package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.RetailSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface RetailSaleRepository extends JpaRepository<RetailSale, Long> {

    Page<RetailSale> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<RetailSale> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to, Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM RetailSale r " +
            "WHERE r.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalAmountBetween(@Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

}