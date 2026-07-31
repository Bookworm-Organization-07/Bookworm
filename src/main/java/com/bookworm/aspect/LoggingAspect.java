package com.bookworm.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TODO #1 — AOP before/after/around, applied to the service layer.
 * Around times every service call; before/after specifically bracket royalty calculation,
 * since that's the one BRD-critical side effect (§13.1) worth its own audit trail in the logs.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.bookworm.service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = System.currentTimeMillis() - start;
            log.debug("{} took {}ms", joinPoint.getSignature().toShortString(), elapsedMs);
        }
    }

    @Before("execution(* com.bookworm.service.RoyaltyService.*(..))")
    public void beforeRoyaltyCalculation(JoinPoint joinPoint) {
        log.info("Royalty calculation starting: {} with args {}", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
    }

    @AfterReturning("execution(* com.bookworm.service.RoyaltyService.*(..))")
    public void afterRoyaltyCalculation(JoinPoint joinPoint) {
        log.info("Royalty calculation finished: {}", joinPoint.getSignature().toShortString());
    }
}
