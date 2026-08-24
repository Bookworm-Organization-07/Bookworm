package com.example.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Logs every sign-on and sign-up attempt and its outcome. Method names
 * only - never the arguments, which would put passwords in the log file.
 */

@Aspect
@Component
public class AuthLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthLoggingAspect.class);

    @Pointcut("execution(* com.example.Services.AuthService.*(..))")
    public void authServiceMethods() {
    }

    @Before("authServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("AUTH START   : {}", joinPoint.getSignature().getName());
    }

    @AfterReturning("authServiceMethods()")
    public void logAfterSuccess(JoinPoint joinPoint) {
        log.info("AUTH SUCCESS : {}", joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "authServiceMethods()", throwing = "ex")
    public void logAfterFailure(JoinPoint joinPoint, Exception ex) {
        log.warn("AUTH FAILED  : {} - {}", joinPoint.getSignature().getName(), ex.getMessage());
    }
}
