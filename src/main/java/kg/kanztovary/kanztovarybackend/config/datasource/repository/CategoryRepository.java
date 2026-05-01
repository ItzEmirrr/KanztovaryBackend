package kg.kanztovary.kanztovarybackend.config.datasource.repository;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Boolean existsByName(String name);
    Boolean existsBySlug(String slug);
    Boolean existsBySlugAndIdNot(String slug, Integer id);
    Boolean existsByNameAndIdNot(String name, Integer id);
}