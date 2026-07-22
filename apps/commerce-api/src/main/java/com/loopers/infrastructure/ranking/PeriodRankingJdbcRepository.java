package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.PeriodRankingRepository;
import com.loopers.domain.ranking.RankingPeriod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 주간·월간 랭킹 MV 조회 구현체 — JdbcTemplate 기반.
 *
 * <p>MV(mv_product_rank_weekly / _monthly)는 commerce-batch가 소유·적재하는 테이블이다. commerce-api가
 * JPA 엔티티로 매핑하면 ddl-auto(local/test는 create)가 이 테이블을 재생성해 배치가 쌓은 데이터를 덮을 수 있어,
 * 읽기 전용인 이쪽은 엔티티 없이 JdbcTemplate으로 조회만 한다 (배치 테스트가 product_metrics_daily를 읽던 방식과 대칭).
 */
@Repository
public class PeriodRankingJdbcRepository implements PeriodRankingRepository {

    private final JdbcTemplate jdbcTemplate;

    public PeriodRankingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Long> findTopProductIds(RankingPeriod period, long offset, int limit) {
        String table = tableOf(period);
        return jdbcTemplate.queryForList(
            "SELECT product_id FROM " + table + " ORDER BY rank_no ASC LIMIT ? OFFSET ?",
            Long.class, limit, offset
        );
    }

    @Override
    public long countRanked(RankingPeriod period) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableOf(period), Long.class);
        return count == null ? 0L : count;
    }

    /** period → MV 테이블명. 값이 enum 상수라 SQL 주입 위험이 없다. DAILY는 Redis 경로라 여기 오면 안 된다. */
    private String tableOf(RankingPeriod period) {
        return switch (period) {
            case WEEKLY -> "mv_product_rank_weekly";
            case MONTHLY -> "mv_product_rank_monthly";
            case DAILY -> throw new IllegalArgumentException("일간 랭킹은 MV가 아닌 Redis에서 조회합니다.");
        };
    }
}
