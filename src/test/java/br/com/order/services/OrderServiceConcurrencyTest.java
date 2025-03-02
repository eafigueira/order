package br.com.order.services;

import br.com.order.application.order.Order;
import br.com.order.application.order.OrderRepository;
import br.com.order.application.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase
class OrderServiceConcurrencyTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionalExecutor transactionalExecutor;

    @Test
    @DisplayName("Deve lançar PessimisticLockingFailureException quando duas transações tentam acessar o mesmo pedido")
    void getOrderById_WhenConcurrentAccess_ShouldThrowPessimisticLockingFailureException() throws Exception {
        Order order = transactionalExecutor.execute(() -> createOrder());

        var executor = Executors.newFixedThreadPool(2);
        var latch = new CountDownLatch(1);

        Future<Void> future1 = executor.submit(() -> {
            transactionalExecutor.execute(() -> {
                accessOrderWithLock(order.getId(), latch);
                return null;
            });
            return null;
        });

        latch.await();

        Future<Exception> future2 = executor.submit(() -> {
            try {
                transactionalExecutor.execute(() -> {
                    transactionalFindWithLock(order.getId());
                    return null;
                });
                return null;
            } catch (Exception e) {
                return e;
            }
        });

        Exception exception = future2.get(5, TimeUnit.SECONDS);
        assertNotNull(exception, "Deveria ter lançado uma exceção");
        assertInstanceOf(PessimisticLockingFailureException.class, exception, "Deveria lançar PessimisticLockException");

        future1.get();

        executor.shutdown();
    }

    private Order createOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        return orderRepository.save(order);
    }

    private void accessOrderWithLock(Long orderId, CountDownLatch latch) throws Exception {
        transactionalExecutor.execute(() -> {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            latch.countDown();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
    }

    private void transactionalFindWithLock(Long orderId) throws Exception {
        transactionalExecutor.execute(() -> {
            try {
                orderRepository.findById(orderId);
            } catch (PessimisticLockingFailureException e) {
                throw e;
            }
            return null;
        });
    }
}
