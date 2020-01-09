
import com.pojo.customize.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

        ConcurrentHashMap<String, Client> websocketMap = new ConcurrentHashMap<>();

        Map<String, Client> collect = websocketMap.entrySet().stream()
                .filter(map -> map.getValue().getLoginStatus() == 1)
                .filter(map -> map.getValue().getPlaceOrderStatus() == 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Client> list = new ArrayList<>(collect.values());
        if (null == list) {
            log.error("null");
        }
        if (list.isEmpty()) {
            log.error("isEmpty");
        }
        if(list==null && list.isEmpty()){
            log.error("3");
        }

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
