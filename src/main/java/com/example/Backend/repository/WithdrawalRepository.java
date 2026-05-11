package com.example.Backend.repository;

import com.example.Backend.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    List<Withdrawal> findByUserIdOrderByRequestedAtDesc(Long userId);

    List<Withdrawal> findAllByOrderByRequestedAtDesc();
}
