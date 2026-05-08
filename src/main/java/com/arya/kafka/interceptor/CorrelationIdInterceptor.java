package com.arya.kafka.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Spring MVC interceptor that seeds the SLF4J MDC with a {@code correlationId} for every
 * inbound HTTP request.
 *
 * <p>If the caller supplies an {@code X-Correlation-Id} header, that value is used verbatim;
 * otherwise a random UUID is generated. The correlation ID is propagated to Kafka message
 * headers by the producer service, enabling end-to-end request tracing across HTTP and
 * asynchronous Kafka boundaries.
 *
 * <p>The MDC entry is always cleared in {@link #afterCompletion} to avoid leaking context
 * across thread-pool-reused threads.
 *
 * @author rahul-ghadge
 */
@Slf4j
@Component
public class CorrelationIdInterceptor implements HandlerInterceptor {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        log.debug("Request received [method={} uri={} correlationId={}]",
                request.getMethod(), request.getRequestURI(), correlationId);
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        MDC.remove(CORRELATION_ID_MDC_KEY);
    }
}
