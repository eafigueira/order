package br.com.order.application.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1592401179972971019L;

    private Long id;
    private List<OrderItemDTO> items;
    private Long customerId;
    private BigDecimal discount;
    private OrderStatus status;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getDiscount() {
        return discount != null ? discount : BigDecimal.ZERO;
    }
}
