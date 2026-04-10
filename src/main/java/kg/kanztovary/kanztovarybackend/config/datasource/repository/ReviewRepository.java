package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findAverageRating();

    @Query("SELECT r.rating AS rating, COUNT(r) AS count FROM Review r GROUP BY r.rating ORDER BY r.rating")
    java.util.List<Object[]> findRatingDistribution();
}