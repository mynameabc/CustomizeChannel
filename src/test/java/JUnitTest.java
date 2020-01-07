
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

@Slf4j
public class JUnitTest {

    private static final Logger rootLogger = LogManager.getRootLogger();
    private static final Logger nameLogger = LogManager.getLogger("name");
    private static final Logger parentLogger = LogManager.getLogger(JUnitTest.class);
    public static void main(String[] args) {
        rootLogger.warn("rootLogger:warn");
        rootLogger.error("rootLogger:error");
        rootLogger.info("rootLogger:info");
        nameLogger.warn("nameLogger:warn");
        nameLogger.error("nameLogger:error");
        nameLogger.info("nameLogger:info");
        parentLogger.warn("parentLogger:warn");
        parentLogger.error("parentLogger:error");
        parentLogger.info("parentLogger:info");
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

/*    public static void main(String args[]) {

        log.trace("trace level");
        log.debug("debug level");
        log.info("info level");
        log.warn("warn level");
        log.error("error level");
//        log.fatal("fatal level");
    }*/
}
