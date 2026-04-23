package com.example.dropshop.domain.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 등록 요청 DTO.
 */
@Getter
public class ProductCreateRequest {

  @NotBlank
  @Size(max = 100)
  private String name;

  @NotNull
  private BigDecimal price;

  @NotNull
  private Integer discountRate;

  @NotNull
  @Positive
  private Integer stock;

  @NotBlank
  @Size(max = 100)
  private String category;

  @NotBlank
  private String description;

  @NotBlank
  private String specification;

  @Valid
  @NotEmpty
  @Size(max = 5)
  private List<ImageRequest> images;

  /**
   * 상품 이미지 등록 요청 DTO.
   */
  @Getter
  public static class ImageRequest {

    @NotBlank
    @Size(max = 500)
    private String imageUrl;

    @NotNull
    @Min(1)
    private Integer sortOrder;

    @NotNull
    private Boolean isThumbnail;
  }
}

