package com.loopers.application.ranking;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.ranking.RankingPeriod;

import java.time.LocalDate;
import java.util.List;

/**
 * 랭킹 페이지 조회 결과 — 상품 ID가 아닌 상품정보(ProductInfo)가 Aggregation 되어 담긴다.
 * rank는 절대 순위(1-based) — 삭제된 상품이 목록에서 빠져도 남은 상품의 순위 번호는 유지된다.
 * date는 DAILY 조회 시 대상 날짜, WEEKLY/MONTHLY는 최신 스냅샷이라 특정 날짜 개념이 없어 null이다.
 */
public record RankingPageInfo(
    RankingPeriod period,
    LocalDate date,
    int page,
    int size,
    long totalCount,
    List<RankedProduct> items
) {
    public record RankedProduct(long rank, ProductInfo product) {}
}
