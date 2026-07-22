package com.loopers.domain.ranking;

import java.util.List;

/**
 * 주간·월간 랭킹 MV 조회 전용 저장소 — 적재는 commerce-batch가 담당하고, 여기서는 최신 스냅샷을 읽기만 한다.
 * MV는 매 배치 실행마다 전체 교체되므로 항상 "가장 최근에 집계된 스냅샷" 하나만 존재한다 (조회 시 날짜 개념 없음).
 */
public interface PeriodRankingRepository {

    /** 해당 기간 MV에서 순위 오름차순으로 offset부터 limit개의 상품 ID를 조회한다. */
    List<Long> findTopProductIds(RankingPeriod period, long offset, int limit);

    /** 해당 기간 MV의 전체 상품 수. */
    long countRanked(RankingPeriod period);
}
