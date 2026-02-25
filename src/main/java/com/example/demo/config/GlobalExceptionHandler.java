package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 컨트롤러에서 처리되지 않은 예외를 가로채 흰 화면 500 대신 안내 페이지로 전환한다.
 *
 * <p>주의: HandlerInterceptor.postHandle/afterCompletion 에서 발생한 예외는
 * Spring MVC가 @ControllerAdvice 로 전달하지 않는다.
 * 해당 위치는 BeforeActionInterceptor 내부 try-catch 로 별도 처리한다.
 *
 * <p>핸들러 우선순위: 더 구체적인 예외 타입이 먼저 매칭된다.
 * NoResourceFoundException → IllegalArgument → IllegalState → Exception(폴백)
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ──────────────────────────────────────────────────────────────────────
    // 1) 정적 리소스 없음 (404) — favicon.ico / .well-known/* 등 브라우저 자동 요청
    //    기존 Exception 핸들러가 ERROR 로그를 남기는 것을 방지한다.
    //    ResponseEntity<Void>를 반환해 뷰 렌더링 없이 HTTP 상태만 응답한다.
    // ──────────────────────────────────────────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex,
                                                  HttpServletRequest req) {
        String uri = req.getRequestURI();

        // 브라우저 자동 요청(favicon, well-known 등)은 trace 수준으로만 기록
        if ("/favicon.ico".equals(uri) || uri.startsWith("/.well-known/")) {
            log.trace("자동 요청 리소스 없음(무시): {}", uri);
            // 204 No Content: 브라우저가 재요청을 줄임
            return ResponseEntity.noContent().build();
        }

        // 그 외 404 는 warn 수준으로 기록하고 404 응답
        log.warn("리소스 없음 [{}]: {}", uri, ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    // ──────────────────────────────────────────────────────────────────────
    // 2) 필수 파라미터(@RequestParam required=true) 누락 — 400 수준
    // ──────────────────────────────────────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParam(MissingServletRequestParameterException ex, Model model) {
        log.warn("필수 파라미터 누락: {}", ex.getMessage());
        model.addAttribute("msg", "필수 요청 정보가 누락되었습니다: " + ex.getParameterName());
        model.addAttribute("historyBack", true);
        return "usr/common/js";
    }

    // ──────────────────────────────────────────────────────────────────────
    // 3) 잘못된 인수 (예: 잘못된 형식의 ID 등)
    // ──────────────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        HttpServletRequest req, Model model) {
        log.warn("잘못된 요청 [{}]: {}", req.getRequestURI(), ex.getMessage());
        model.addAttribute("msg",
                ex.getMessage() != null ? ex.getMessage() : "잘못된 요청입니다.");
        model.addAttribute("historyBack", true);
        return "usr/common/js";
    }

    // ──────────────────────────────────────────────────────────────────────
    // 4) 처리 불가 상태 (예: 중복 제출, 데이터 불일치 등)
    // ──────────────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex,
                                     HttpServletRequest req, Model model) {
        log.warn("처리 오류 [{}]: {}", req.getRequestURI(), ex.getMessage());
        model.addAttribute("msg",
                ex.getMessage() != null ? ex.getMessage() : "처리 중 오류가 발생했습니다.");
        model.addAttribute("historyBack", true);
        return "usr/common/js";
    }

    // ──────────────────────────────────────────────────────────────────────
    // 5) 그 외 모든 예외 — 마지막 방어선
    // ──────────────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public String handleAll(Exception ex, HttpServletRequest req, Model model) {
        log.error("서버 오류 [{}]", req.getRequestURI(), ex);
        model.addAttribute("msg", "서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        model.addAttribute("historyBack", true);
        return "usr/common/js";
    }
}
