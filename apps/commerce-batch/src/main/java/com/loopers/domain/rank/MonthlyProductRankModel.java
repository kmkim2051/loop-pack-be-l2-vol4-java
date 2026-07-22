package com.loopers.domain.rank;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * 월간 TOP 100 랭킹 MV (`mv_product_rank_monthly`). 배치가 매 실행 시 전체 교체(clear→insert)한다.
 */
@Entity
@Table(
    name = "mv_product_rank_monthly",
    indexes = @Index(name = "idx_mv_monthly_rank", columnList = "rank_no")
)
public class MonthlyProductRankModel extends ProductRankSnapshotModel {

    protected MonthlyProductRankModel() {}

    public MonthlyProductRankModel(int rankNo, Long productId, double score, LocalDate periodStart, LocalDate periodEnd) {
        super(rankNo, productId, score, periodStart, periodEnd);
    }
}
