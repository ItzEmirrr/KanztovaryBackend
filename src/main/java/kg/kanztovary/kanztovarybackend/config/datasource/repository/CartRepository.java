package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Cart;
import kg.kanztovary.kanztovarybackend.domain.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.variant WHERE c.user.id = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatus(@Param("userId") Integer userId,
                                         @Param("status") CartStatus status);
}