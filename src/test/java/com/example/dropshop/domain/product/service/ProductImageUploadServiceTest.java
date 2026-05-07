package com.example.dropshop.domain.product.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.request.PresignedUrlIssueRequest;
import com.example.dropshop.domain.product.exception.ProductException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ProductImageUploadServiceTest {

  @Test
  @SuppressWarnings("NonAsciiCharacters")
  void 허용되지_않은_이미지_타입이면_예외를_던진다() throws Exception {
    ProductImageUploadProperties properties =
        new ProductImageUploadProperties(
            "test-bucket", "ap-northeast-2", "products", "https://cdn.example.com", 300L);
    ProductImageUploadService service = new ProductImageUploadService(null, properties);

    PresignedUrlIssueRequest request = new PresignedUrlIssueRequest();
    Field fileTypeField = PresignedUrlIssueRequest.class.getDeclaredField("fileType");
    fileTypeField.setAccessible(true);
    fileTypeField.set(request, "gif");

    assertThatThrownBy(() -> service.issuePresignedUrl(1L, request))
        .isInstanceOf(ProductException.class)
        .hasMessage(ErrorCode.INVALID_IMAGE_FILE_TYPE.getMessage());
  }
}
