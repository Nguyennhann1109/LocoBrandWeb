package com.example.forthehood.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.forthehood.dto.CreateOrderRequest;
import com.example.forthehood.dto.OrderItemRequest;
import com.example.forthehood.dto.OrderItemResponse;
import com.example.forthehood.dto.OrderResponse;
import com.example.forthehood.entity.Account;
import com.example.forthehood.entity.Customer;
import com.example.forthehood.entity.Order;
import com.example.forthehood.entity.OrderItem;
import com.example.forthehood.entity.OrderStatusHistory;
import com.example.forthehood.entity.Product;
import com.example.forthehood.enums.OrderStatus;
import com.example.forthehood.repository.AccountRepository;
import com.example.forthehood.repository.CustomerRepository;
import com.example.forthehood.repository.OrderItemRepository;
import com.example.forthehood.repository.OrderRepository;
import com.example.forthehood.repository.OrderStatusHistoryRepository;
import com.example.forthehood.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        AccountRepository accountRepository,
                        CustomerRepository customerRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    private void recordStatusHistory(Order order, OrderStatus fromStatus, OrderStatus toStatus, String changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .build();
        orderStatusHistoryRepository.save(history);
    }

    private Customer getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account not found for current user"));
        return customerRepository.findAll().stream()
                .filter(c -> c.getAccount().getId().equals(account.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Customer profile not found for current user"));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getProduct().getName(),
                        oi.getQuantity(),
                        oi.getPrice()
                ))
                .collect(Collectors.toList());
        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Customer customer = getCurrentCustomer();
        String currentUser = getCurrentUsername();

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdBy(currentUser)
                .totalPrice(BigDecimal.ZERO)
                .build();
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getProductId()));

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new IllegalArgumentException("Product is not available for ordering: " + product.getName());
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(price)
                    .build();
            orderItemRepository.save(orderItem);
        }

        order.setTotalPrice(total);
        order = orderRepository.save(order);

        // record initial status history (from null to PENDING)
        recordStatusHistory(order, null, OrderStatus.PENDING, currentUser);

        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByCurrentCustomer() {
        Customer customer = getCurrentCustomer();
        List<Order> orders = orderRepository.findByCustomerId(customer.getId());
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public OrderResponse getOrderForCurrentCustomer(Long orderId) {
        Customer customer = getCurrentCustomer();
        String currentUser = getCurrentUsername();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Order does not belong to current customer");
        }

        return mapToResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus targetStatus = status;

        // Prevent invalid status transitions
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.SHIPPED) {
            if (targetStatus != currentStatus) {
                throw new IllegalArgumentException("Cannot change status from " + currentStatus + " to " + targetStatus);
            }
        }

        if (currentStatus == OrderStatus.PAID && targetStatus == OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot change status from PAID back to PENDING");
        }

        if ((currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.CANCELLED)
                && targetStatus == OrderStatus.PAID) {
            throw new IllegalArgumentException("Cannot change status to PAID from " + currentStatus);
        }

        String currentUser = getCurrentUsername();

        order.setStatus(targetStatus);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(currentUser);
        order = orderRepository.save(order);

        recordStatusHistory(order, currentStatus, targetStatus, currentUser);
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderByCustomer(Long orderId, CreateOrderRequest request) {
        Customer customer = getCurrentCustomer();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Order does not belong to current customer");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be updated by customer");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // restore stock from existing items and remove them
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        orderItemRepository.deleteAll(order.getOrderItems());
        order.getOrderItems().clear();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getProductId()));

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new IllegalArgumentException("Product is not available for ordering: " + product.getName());
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(price)
                    .build();
            orderItemRepository.save(orderItem);
        }

        order.setTotalPrice(total);
        order = orderRepository.save(order);

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrderByCustomer(Long orderId) {
        Customer customer = getCurrentCustomer();
        String currentUser = getCurrentUsername();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Order does not belong to current customer");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be cancelled by customer");
        }

        // restore stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(currentUser);
        order = orderRepository.save(order);

        recordStatusHistory(order, previousStatus, OrderStatus.CANCELLED, currentUser);

        return mapToResponse(order);
    }
}
