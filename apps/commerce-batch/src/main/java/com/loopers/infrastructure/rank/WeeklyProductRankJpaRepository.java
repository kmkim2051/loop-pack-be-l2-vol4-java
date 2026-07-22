package com.loopers.infrastructure.rank;

import com.loopers.domain.rank.WeeklyProductRankModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyProductRankJpaRepository extends JpaRepository<WeeklyProductRankModel, Long> {
}
