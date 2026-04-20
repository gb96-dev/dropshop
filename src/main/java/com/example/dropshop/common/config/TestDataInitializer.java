package com.example.dropshop.common.config;

import com.example.dropshop.domain.drops.entity.Drops;
import com.example.dropshop.domain.drops.repository.DropsRepository;
import com.example.dropshop.domain.order.repository.OrderItemRepository;
import com.example.dropshop.domain.order.repository.OrderRepository;
import com.example.dropshop.domain.payment.repository.PaymentRepository;
import com.example.dropshop.domain.product.entity.Product;
import com.example.dropshop.domain.product.enums.ProductStatus;
import com.example.dropshop.domain.product.repository.ProductRepository;
import com.example.dropshop.domain.user.entity.User;
import com.example.dropshop.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class TestDataInitializer {

  @Bean
  @Profile({"local", "dev"})
  CommandLineRunner initTestData(
      UserRepository userRepository,
      ProductRepository productRepository,
      DropsRepository dropsRepository,
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      PaymentRepository paymentRepository,
      JdbcTemplate jdbcTemplate
  ) {
    return args -> initialize(
        userRepository,
        productRepository,
        dropsRepository,
        orderRepository,
        orderItemRepository,
        paymentRepository,
        jdbcTemplate
    );
  }

  @Transactional
  public void initialize(
      UserRepository userRepository,
      ProductRepository productRepository,
      DropsRepository dropsRepository,
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      PaymentRepository paymentRepository,
      JdbcTemplate jdbcTemplate
  ) {
    resetOrderAndPaymentData(orderRepository, orderItemRepository, paymentRepository, jdbcTemplate);

    if (userRepository.count() > 0 || productRepository.count() > 0 || dropsRepository.count() > 0) {
      return;
    }

    // 1. 테스트 유저 생성
    User user = User.signup(
        "test-user@dropshop.com",
        "encoded-password",
        "테스트유저"
    );
    userRepository.save(user);

    // 2. 테스트 상품 1 생성
    Product product1 = Product.create(
        1L,
        "테스트 상품 1",
        "SHOES",
        new BigDecimal("1000"),
        21,
        50,
        "https://dummy-image-1",
        "테스트 상품 1 설명",
        "테스트 상품 1 스펙",
        "기본 배송 안내",
        "기본 환불 정책"
    );
    product1.updateStatusByDrop(ProductStatus.READY);
    productRepository.save(product1);

    // 3. 테스트 상품 2 생성
    Product product2 = Product.create(
        1L,
        "테스트 상품 2",
        "CLOTHING",
        new BigDecimal("150000"),
        10,
        30,
        "https://dummy-image-2",
        "테스트 상품 2 설명",
        "테스트 상품 2 스펙",
        "기본 배송 안내",
        "기본 환불 정책"
    );
    product2.updateStatusByDrop(ProductStatus.READY);
    productRepository.save(product2);

    // 4. 테스트 드랍 1 생성
    Drops drop1 = Drops.create(
        product1,
        LocalDateTime.now().minusMinutes(10),
        LocalDateTime.now().plusDays(1),
        20L,
        1L,
        false
    );
    drop1.activate();
    dropsRepository.save(drop1);

    // 5. 테스트 드랍 2 생성
    Drops drop2 = Drops.create(
        product2,
        LocalDateTime.now().minusMinutes(10),
        LocalDateTime.now().plusDays(1),
        10L,
        1L,
        false
    );
    drop2.activate();
    dropsRepository.save(drop2);
  }

  private void resetOrderAndPaymentData(
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      PaymentRepository paymentRepository,
      JdbcTemplate jdbcTemplate
  ) {
    paymentRepository.deleteAllInBatch();
    orderItemRepository.deleteAllInBatch();
    orderRepository.deleteAllInBatch();

    jdbcTemplate.execute("ALTER TABLE payments AUTO_INCREMENT = 1");
    jdbcTemplate.execute("ALTER TABLE order_items AUTO_INCREMENT = 1");
    jdbcTemplate.execute("ALTER TABLE orders AUTO_INCREMENT = 1");
  }
}
