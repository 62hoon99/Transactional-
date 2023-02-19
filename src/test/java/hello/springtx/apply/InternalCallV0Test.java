package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@SpringBootTest
@Slf4j
public class InternalCallV0Test {
    @Autowired
    CallServiceV2 callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void externalCallV2() {
        callService.external();
    }

    @Test
    void innerPrivateCall() {
        callService.externalPublic();
    }

    @TestConfiguration
    static class InternalCallV2TestConfig {
        @Bean
        CallServiceV2 callService() {
            return new CallServiceV2(innerService());
        }

        @Bean
        InternalService innerService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallServiceV2 {

        private final InternalService internalService;

        public void external() {
            log.info("call external");
            printTxInfo();
            internalService.internal();
            printTxInfo();
        }

        public void externalPublic() {
            log.info("call externalPublic");
            printTxInfo();
            internalService.innerPublic();
            printTxInfo();
            log.info("out externalPublic");
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }

    @Slf4j
    static class InternalService {

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        @Transactional
        public void innerPublic() {
            log.info("call innerPublic");
            printTxInfo();
//            innerPrivate();
            duplicateTransaction();
            printTxInfo();
            log.info("out innerPublic");
        }

        // 트랜잭션 안에서 내부 메서드를 호출하면 내부 메서드도 트랜잭션 안에서 로직을 수행한다.
        private void innerPrivate() {
            log.info("call innerPrivate");
            printTxInfo();
            log.info("out innerPrivate");
        }

        /**
         * innerPublic을 외부에서 호출할 때 프록시 객체가 호출을 받아서 트랜잭션을 시작하고
         * innerPublic이 duplicateTransaction을 호출할 때는 실제 객체안에서 호출하는 것이기 때문에
         * transaction이 중복되는 일은 없음
         */
        @Transactional
        public void duplicateTransaction() {
            log.info("call duplicateTransaction");
            printTxInfo();
            log.info("out duplicateTransaction");
        }

        private void printTxInfo() {
            boolean txActive =
                    TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }

}
