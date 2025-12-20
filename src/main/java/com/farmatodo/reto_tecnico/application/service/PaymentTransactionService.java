package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.EventType;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Separated service to handle individual payment transactions.
 * Each method runs in its own transaction (REQUIRES_NEW) to avoid holding
 * database connections during retry delays.
 *
 * This fixes the critical issue of Thread.sleep() within @Transactional.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionService {

    private final OrderRepositoryPort orderRepository;
    private final AuditLogService auditLogService;

    /**
     * Saves order state within a new transaction.
     * This ensures database connection is released quickly.
     *
     * @param order the order to save
     * @return saved order
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveOrderState(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Updates order to PAYMENT_PROCESSING state with token.
     * Runs in its own transaction.
     *
     * @param order the order
     * @param token the payment token
     * @return updated order
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order assignTokenAndSave(Order order, String token) {
        order.assignPaymentToken(token);
        return orderRepository.save(order);
    }

    /**
     * Confirms payment and saves order.
     * Runs in its own transaction.
     *
     * @param order the order
     * @return updated order
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order confirmPaymentAndSave(Order order) {
        String oldStatus = order.getStatus().name();
        order.confirmPayment();
        Order savedOrder = orderRepository.save(order);
        String newStatus = savedOrder.getStatus().name();

        // RF8: Log order status change to audit trail
        String eventData = String.format(
                "{\"orderId\":\"%s\",\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}",
                savedOrder.getId(),
                oldStatus,
                newStatus
        );
        auditLogService.logEvent(
                EventType.ORDER_STATUS_CHANGED,
                "Order",
                savedOrder.getId(),
                "SUCCESS",
                eventData
        );

        return savedOrder;
    }

    /**
     * Marks payment as failed and saves order.
     * Runs in its own transaction.
     *
     * @param order the order
     * @return updated order
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order failPaymentAndSave(Order order) {
        String oldStatus = order.getStatus().name();
        order.failPayment();
        Order savedOrder = orderRepository.save(order);
        String newStatus = savedOrder.getStatus().name();

        // RF8: Log order status change to audit trail
        String eventData = String.format(
                "{\"orderId\":\"%s\",\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}",
                savedOrder.getId(),
                oldStatus,
                newStatus
        );
        auditLogService.logEvent(
                EventType.ORDER_STATUS_CHANGED,
                "Order",
                savedOrder.getId(),
                "SUCCESS",
                eventData
        );

        return savedOrder;
    }
}
