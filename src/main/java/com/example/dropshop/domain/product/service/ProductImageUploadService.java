package com.example.dropshop.domain.product.service;

import com.example.dropshop.common.exception.ErrorCode;
import com.example.dropshop.domain.product.dto.request.PresignedUrlIssueRequest;
import com.example.dropshop.domain.product.dto.response.PresignedUrlIssueResponse;
import com.example.dropshop.domain.product.exception.ProductException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 상품 이미지 업로드 URL 발급 서비스.
 */
@Service
@RequiredArgsConstructor
public class ProductImageUploadService {

  private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
      "jpeg", "image/jpeg",
      "jpg", "image/jpeg",
      "png", "image/png",
      "webp", "image/webp",
      "image/jpeg", "image/jpeg",
      "image/png", "image/png",
      "image/webp", "image/webp"
  );

  private final S3Presigner s3Presigner;
  private final ProductImageUploadProperties properties;

  /**
   * 상품 이미지 업로드를 위한 Presigned URL을 발급한다.
   */
  public PresignedUrlIssueResponse issuePresignedUrl(
      Long sellerId,
      PresignedUrlIssueRequest request
  ) {
    String normalizedFileType = normalizeFileType(request.getFileType());
    String contentType = resolveContentType(normalizedFileType);
    String key = createObjectKey(sellerId, normalizedFileType);

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(properties.getBucket())
          .key(key)
          .contentType(contentType)
          .build();

      PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
          .signatureDuration(Duration.ofSeconds(properties.getPresignedExpirationSeconds()))
          .putObjectRequest(putObjectRequest)
          .build();

      PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

      return PresignedUrlIssueResponse.builder()
          .presignedUrl(presignedRequest.url().toString())
          .imageUrl(createImageUrl(key))
          .build();
    } catch (RuntimeException ex) {
      throw new ProductException(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
    }
  }

  private String normalizeFileType(String fileType) {
    String normalized = fileType.trim().toLowerCase(Locale.ROOT);
    if ("jpg".equals(normalized)) {
      return "jpeg";
    }
    return normalized;
  }

  private String resolveContentType(String fileType) {
    String contentType = CONTENT_TYPE_MAP.get(fileType);
    if (contentType == null) {
      throw new ProductException(ErrorCode.INVALID_IMAGE_FILE_TYPE);
    }
    return contentType;
  }

  private String createObjectKey(Long sellerId, String fileType) {
    LocalDate today = LocalDate.now();
    String prefix = properties.getKeyPrefix();

    return String.format(
        "%s/%d/%d/%02d/%02d/%s.%s",
        prefix,
        sellerId,
        today.getYear(),
        today.getMonthValue(),
        today.getDayOfMonth(),
        UUID.randomUUID(),
        fileType
    );
  }

  private String createImageUrl(String key) {
    String cdnBaseUrl = properties.getCdnBaseUrl();
    if (cdnBaseUrl.endsWith("/")) {
      return cdnBaseUrl + key;
    }
    return cdnBaseUrl + "/" + key;
  }
}

