package br.com.order.application.product;

import br.com.order.application.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity {

    @Column(unique = true, length = 50)
    private String sku;
    @Column(length = 150)
    private String name;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}
