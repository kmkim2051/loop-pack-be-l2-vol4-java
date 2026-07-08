package com.loopers.interfaces.api.queue;

import com.loopers.domain.queue.EntryTokenRepository;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QueueTokenInterceptorTest {

    @InjectMocks private QueueTokenInterceptor interceptor;
    @Mock private EntryTokenRepository entryTokenRepository;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserModel currentUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        currentUser = new UserModel("user123", "encoded!", "홍길동", LocalDate.of(1990, 1, 15), "test@example.com");
    }

    @DisplayName("preHandle()을 호출할 때,")
    @Nested
    class PreHandle {

        @DisplayName("GET 요청은 토큰 검증 없이 통과한다.")
        @Test
        void passes_whenNotPostRequest() {
            // arrange
            request.setMethod("GET");

            // act
            boolean result = interceptor.preHandle(request, response, new Object());

            // assert
            assertTrue(result);
            then(entryTokenRepository).should(never()).isValid(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
        }

        @DisplayName("POST 요청에 토큰 헤더가 없으면 UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorized_whenTokenHeaderMissing() {
            // arrange
            request.setMethod("POST");
            request.setAttribute("currentUser", currentUser);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                interceptor.preHandle(request, response, new Object())
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @DisplayName("POST 요청에 유효하지 않은 토큰이면 UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorized_whenTokenInvalid() {
            // arrange
            request.setMethod("POST");
            request.setAttribute("currentUser", currentUser);
            request.addHeader("X-Queue-Token", "invalid-token");
            given(entryTokenRepository.isValid(currentUser.getId(), "invalid-token")).willReturn(false);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                interceptor.preHandle(request, response, new Object())
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @DisplayName("POST 요청에 유효한 토큰이면 통과한다.")
        @Test
        void passes_whenTokenValid() {
            // arrange
            request.setMethod("POST");
            request.setAttribute("currentUser", currentUser);
            request.addHeader("X-Queue-Token", "valid-token");
            given(entryTokenRepository.isValid(currentUser.getId(), "valid-token")).willReturn(true);

            // act
            boolean result = interceptor.preHandle(request, response, new Object());

            // assert
            assertTrue(result);
        }
    }

    @DisplayName("afterCompletion()을 호출할 때,")
    @Nested
    class AfterCompletion {

        @DisplayName("POST 요청이 성공(2xx)하면 토큰을 삭제한다.")
        @Test
        void deletesToken_whenPostSucceeds() {
            // arrange
            request.setMethod("POST");
            request.setAttribute("currentUser", currentUser);
            response.setStatus(200);

            // act
            interceptor.afterCompletion(request, response, new Object(), null);

            // assert
            then(entryTokenRepository).should().delete(currentUser.getId());
        }

        @DisplayName("POST 요청이 실패(4xx/5xx)하면 토큰을 삭제하지 않는다.")
        @Test
        void keepsToken_whenPostFails() {
            // arrange
            request.setMethod("POST");
            request.setAttribute("currentUser", currentUser);
            response.setStatus(500);

            // act
            interceptor.afterCompletion(request, response, new Object(), null);

            // assert
            then(entryTokenRepository).should(never()).delete(org.mockito.ArgumentMatchers.anyLong());
        }

        @DisplayName("GET 요청은 토큰 삭제 대상이 아니다.")
        @Test
        void doesNothing_whenNotPostRequest() {
            // arrange
            request.setMethod("GET");
            response.setStatus(200);

            // act
            interceptor.afterCompletion(request, response, new Object(), null);

            // assert
            then(entryTokenRepository).should(never()).delete(org.mockito.ArgumentMatchers.anyLong());
        }
    }
}
