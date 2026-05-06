package com.example.dropshop.domain.auth.sse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE(Server-Sent Events) 연결을 관리하는 서비스.
 *
 * <p>이메일을 키로 SseEmitter를 관리하며, 중복 로그인 시 기존 디바이스에
 * force-logout 이벤트를 전송한다.
 *
 * <p>연결 타임아웃: 30분. 타임아웃/완료/오류 시 자동으로 emitter를 제거한다.
 */
@Slf4j
@Service
public class SseEmitterService {

    /** 로그인된 사용자별 SSE 연결을 보관한다. 키: email */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L; // 30분

    /**
     * 클라이언트의 SSE 구독 요청을 처리한다.
     * 기존 연결이 있으면 교체한다.
     *
     * @param email 구독 요청 사용자 이메일
     * @return SseEmitter
     */
    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitters.put(email, emitter);
        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(() -> emitters.remove(email));
        emitter.onError(e -> emitters.remove(email));

        // 연결 직후 초기 이벤트 전송 (nginx 등 중간 프록시의 버퍼링 방지)
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            log.warn("[SSE] 초기 connect 이벤트 전송 실패 - email: {}", email, e);
            emitters.remove(email);
        }

        log.info("[SSE] 구독 등록 - email: {}", email);
        return emitter;
    }

    /**
     * 기존 디바이스에 강제 로그아웃 이벤트를 전송한다.
     * 다른 디바이스에서 같은 계정으로 로그인할 때 호출된다.
     *
     * @param email 강제 로그아웃할 사용자 이메일
     */
    public void sendForceLogout(String email) {
        SseEmitter emitter = emitters.get(email);
        if (emitter == null) {
            // 기존 디바이스가 SSE 구독 중이 아닌 경우 (앱 재시작 등) — 정상 케이스
            log.debug("[SSE] force-logout 대상 emitter 없음 - email: {}", email);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("force-logout")
                    .data("다른 기기에서 로그인하여 현재 세션이 종료됩니다."));
            emitter.complete();
            log.info("[SSE] force-logout 이벤트 전송 완료 - email: {}", email);
        } catch (IOException e) {
            log.warn("[SSE] force-logout 이벤트 전송 실패 - email: {}", email, e);
        } finally {
            emitters.remove(email);
        }
    }
}
