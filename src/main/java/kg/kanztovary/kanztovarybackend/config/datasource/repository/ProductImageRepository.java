package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /** Все изображения товара — нужны URL-ы перед удалением файлов с диска */
    List<ProductImage> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}