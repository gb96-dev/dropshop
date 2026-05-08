package com.example.dropshop.domain.terms.controller;

import com.example.dropshop.domain.terms.dto.response.TermsResponse;
import com.example.dropshop.domain.terms.entity.Terms;
import com.example.dropshop.domain.terms.repository.TermsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

  private final TermsRepository termsRepository;

  /** 전체 약관 리스트 반환 (회원가입 시 노출) */
  @GetMapping
  public ResponseEntity<List<TermsResponse>> getAllTerms() {
    List<TermsResponse> responses =
        termsRepository.findAll().stream().map(TermsResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  /** 특정 약관 상세 조회 */
  @GetMapping("/{id}")
  public TermsResponse getTerms(@PathVariable Long id) {
    Terms terms =
        termsRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 약관을 찾을 수 없습니다. (ID: " + id + ")")); // 404 반환
    return TermsResponse.from(terms);
  }
}
