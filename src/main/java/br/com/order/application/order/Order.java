package br.com.order.application.order;

import br.com.order.application.base.BaseEntity;
import br.com.order.application.customer.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NamedEntityGraph(
        name = "Order.items",
        attributeNodes = @NamedAttributeNode(value = "items", subgraph = "items.product"),
        subgraphs = @NamedSubgraph(name = "items.product", attributeNodes = @NamedAttributeNode("product"))
)
@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
public class Order extends BaseEntity {
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    public Order() {
        this.items = new ArrayList<>();
    }

    public BigDecimal getDiscount() {
        return Objects.isNull(discount) ? BigDecimal.ZERO : discount;
    }

    public BigDecimal getTotal() {
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountValue = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal finalTotal = total.subtract(discountValue);

        return finalTotal.max(BigDecimal.ZERO);
    }

}
