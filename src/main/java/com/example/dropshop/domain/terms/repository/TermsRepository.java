package com.example.dropshop.domain.terms.repository;

import com.example.dropshop.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {}
