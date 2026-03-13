package kg.kanztovary.kanztovarybackend.config.datasource.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "categories", schema = "stationery")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** URL-friendly название категории */
    @Size(max = 100)
    @Column(name = "slug", unique = true, length = 100)
    private String slug;

    /** Родительская категория  */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    /** Дочерние подкатегории */
    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
    private List<Category> children;

    @ManyToMany(mappedBy = "categories")
    private Set<Product> products;
}
