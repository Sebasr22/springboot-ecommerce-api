package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.Cart;
import com.farmatodo.reto_tecnico.domain.model.CartItem;
import com.farmatodo.reto_tecnico.domain.port.out.CartRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CartItemMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CartMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CartJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation for cart persistence.
 * Implements the CartRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartRepositoryAdapter implements CartRepositoryPort {

    private final CartJpaRepository jpaRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Cart save(Cart cart) {
        log.debug("Saving cart: {} for customer: {}", cart.getId(), cart.getCustomerId());

        // Convert domain to entity
        CartEntity entity = cartMapper.toEntity(cart);

        // Clear existing items to avoid duplicates
        entity.getItems().clear();

        // Convert and add items with bidirectional relationship
        for (CartItem domainItem : cart.getItems()) {
            CartItemEntity itemEntity = cartItemMapper.toEntity(domainItem);

            // FIX: Use EntityManager.getReference() to get a managed ProductEntity reference
            // instead of the transient one created by the mapper.
            // This prevents TransientPropertyValueException when persisting CartItemEntity.
            UUID productId = domainItem.getProduct().getId();
            ProductEntity managedProduct = entityManager.getReference(ProductEntity.class, productId);
            itemEntity.setProduct(managedProduct);

            entity.addItem(itemEntity);
        }

        // Save to database
        CartEntity savedEntity = jpaRepository.save(entity);

        // Convert back to domain
        return cartMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findByCustomerId(UUID customerId) {
        log.debug("Finding cart for customer: {}", customerId);

        return jpaRepository.findByCustomerId(customerId)
                .map(cartMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cart> findById(UUID cartId) {
        log.debug("Finding cart by ID: {}", cartId);

        return jpaRepository.findById(cartId)
                .map(cartMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByCustomerId(UUID customerId) {
        log.debug("Deleting cart for customer: {}", customerId);

        jpaRepository.deleteByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCustomerId(UUID customerId) {
        return jpaRepository.existsByCustomerId(customerId);
    }
}
