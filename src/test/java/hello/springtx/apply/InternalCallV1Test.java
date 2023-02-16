package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @Test
    void externalCall() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    /**
     * 클래스 내에 하나의 메서드라도 @Transactional이 적용되어 있다면 프록시 객체로 생성된다.
     * 이러한 클래스에서 외부에서 호출한 메서드가 내부 메서드를 호출 할 때는 다음과 같이 로직이 실행된다.
     * 1. 외부 메서드는 Transactional이고 내부 메서드는 Transactional이 아닌 경우
     *  a. 외부 메서드를 호출하면 응답을 받은 프록시 객체가 Transactional 시작 후 진짜 클래스 메서드 실행
     *  b. 진짜 클래스에서 외부 메서드가 내부 메서드 실행
     *  c. 다 끝나면 Transactional 끝
     *
     * 2. 외부 메서드는 Transactional이 아니고 내부 메서드는 Transactionl인 경우
     *  a. 외부 메서드를 호출하면 응답을 받은 프록시 객체가 진짜 클래스 메서드 실행
     *  b. 진짜 클래스에서 외부 메서드가 내부 메서드 실행
     *      - 이 때는 프록시가 아니기 때문에 Transactional을 시작하지 못한다.
     *  c. Transactional 사용해보지도 못한 채로 끝난다.
     */

    @Slf4j
    static class CallService {

        @Transactional
        public void external() {
            log.info("call external");
            printTxInfo();
            internal();
            printTxInfo();
        }

        private void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }
}
