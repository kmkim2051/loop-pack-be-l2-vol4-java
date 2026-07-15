package com.loopers.domain.ranking;

import java.time.LocalDate;

public interface RankingRepository {

    /**
     * 해당 날짜의 랭킹판에서 상품 점수를 delta만큼 누적한다.
     */
    void incrementScore(LocalDate date, Long productId, double scoreDelta);
}
