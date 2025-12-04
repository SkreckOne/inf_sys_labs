package org.lab1.aspect;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cache.logging.enabled", havingValue = "true")
public class CacheLogAspect {

    private final EntityManagerFactory entityManagerFactory;

    @Around("execution(* org.lab1.service.MovieService.find*(..)) || execution(* org.lab1.service.MovieService.get*(..))")
    public Object logCacheStats(ProceedingJoinPoint joinPoint) throws Throwable {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();

        long hitBefore = stats.getSecondLevelCacheHitCount();
        long missBefore = stats.getSecondLevelCacheMissCount();

        Object result = joinPoint.proceed();

        long hitAfter = stats.getSecondLevelCacheHitCount();
        long missAfter = stats.getSecondLevelCacheMissCount();

        if (hitAfter > hitBefore || missAfter > missBefore) {
            log.info("L2 Cache Stats [{}]: Hits +{}, Misses +{}",
                    joinPoint.getSignature().getName(),
                    (hitAfter - hitBefore),
                    (missAfter - missBefore));
        }
        return result;
    }
}