package br.com.order.services;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

@Component
public class TransactionalExecutor {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T execute(Callable<T> callable) throws Exception {
        return callable.call();
    }
}
