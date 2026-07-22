package com.loopers.infrastructure.rank;

import com.loopers.domain.rank.MonthlyProductRankModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyProductRankJpaRepository extends JpaRepository<MonthlyProductRankModel, Long> {
}
