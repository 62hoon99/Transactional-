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

        // ???????????? ????????? ?????? ???????????? ???????????? ?????? ???????????? ???????????? ????????? ????????? ????????????.
        private void innerPrivate() {
            log.info("call innerPrivate");
            printTxInfo();
            log.info("out innerPrivate");
        }

        /**
         * innerPublic??? ???????????? ????????? ??? ????????? ????????? ????????? ????????? ??????????????? ????????????
         * innerPublic??? duplicateTransaction??? ????????? ?????? ?????? ??????????????? ???????????? ????????? ?????????
         * transaction??? ???????????? ?????? ??????
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
