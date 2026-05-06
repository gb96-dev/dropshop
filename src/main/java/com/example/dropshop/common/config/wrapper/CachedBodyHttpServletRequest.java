package com.example.dropshop.common.config.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 레퍼 요청 클래스.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

  private final byte[] cachedBody;

  /**
   * 생성자.
   * @param request 요청.
   * @throws IOException 예외.
   */
  public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    this.cachedBody = request.getInputStream().readAllBytes();
  }

  @Override
  public ServletInputStream getInputStream() {
    ByteArrayInputStream byteArrayInputStream =
        new ByteArrayInputStream(cachedBody);

    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(
          ReadListener readListener) {}

      @Override
      public int read() {
        return byteArrayInputStream.read();
      }
    };
  }

  public String getBody() {
    return new String(cachedBody, StandardCharsets.UTF_8);
  }
}