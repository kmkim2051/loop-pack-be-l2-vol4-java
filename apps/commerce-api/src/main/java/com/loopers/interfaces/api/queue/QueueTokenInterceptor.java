package com.loopers.interfaces.api.queue;

import com.loopers.domain.queue.EntryTokenRepository;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 주문 생성(POST /api/v1/orders) 앞단에서 입장 토큰을 검증한다.
 * AuthInterceptor가 먼저 실행되어 request 속성에 currentUser를 채워둔 뒤 동작한다는 전제.
 */
@RequiredArgsConstructor
@Component
public class QueueTokenInterceptor implements HandlerInterceptor {

    static final String HEADER_QUEUE_TOKEN = "X-Queue-Token";

    private final EntryTokenRepository entryTokenRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true; // 주문 생성만 검증 대상 — 목록/상세 조회는 통과
        }

        UserModel currentUser = (UserModel) request.getAttribute("currentUser");
        String token = request.getHeader(HEADER_QUEUE_TOKEN);

        if (token == null || !entryTokenRepository.isValid(currentUser.getId(), token)) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "유효한 입장 토큰이 없습니다. 대기열에 먼저 진입해주세요.");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return;
        }
        if (HttpStatus.valueOf(response.getStatus()).is2xxSuccessful()) {
            UserModel currentUser = (UserModel) request.getAttribute("currentUser");
            entryTokenRepository.delete(currentUser.getId());
        }
    }
}
