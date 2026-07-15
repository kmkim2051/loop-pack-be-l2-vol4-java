package com.loopers.infrastructure.ranking;

import com.loopers.config.redis.RedisConfig;
import com.loopers.domain.ranking.RankingKey;
import com.loopers.domain.ranking.RankingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Redis Sorted Set 기반 랭킹 저장소 — ZINCRBY로 점수를 누적한다.
 * 쓰기 직후 읽기 일관성이 필요한 대기열과 달리 랭킹은 쓰기 전용이지만,
 * replica-preferred 기본 템플릿은 getExpire(읽기)가 복제 지연으로 새 키를 못 볼 수 있어 master 템플릿을 사용한다.
 */
@Repository
public class RankingRedisRepository implements RankingRepository {

    private static final long TTL_NOT_SET = -1L;

    private final RedisTemplate<String, String> redisTemplate;

    public RankingRedisRepository(
        @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void incrementScore(LocalDate date, Long productId, double scoreDelta) {
        String key = RankingKey.daily(date);
        redisTemplate.opsForZSet().incrementScore(key, String.valueOf(productId), scoreDelta);
        setTtlOnCreation(key);
    }

    /**
     * TTL은 키 생성 시 1회만 설정한다 — 매 가산마다 갱신하면 쓰기가 이어지는 동안 만료가 계속 밀린다.
     */
    private void setTtlOnCreation(String key) {
        Long expire = redisTemplate.getExpire(key);
        if (expire != null && expire == TTL_NOT_SET) {
            redisTemplate.expire(key, RankingKey.DAILY_TTL);
        }
    }
}
