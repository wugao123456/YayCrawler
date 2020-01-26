package yaycrawler.worker.model;

import org.springframework.web.context.WebApplicationContext;
import yaycrawler.common.model.CrawlerResult;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ucs_yuananyun on 2016/5/13.
 */
public class WorkerContext {
    private static String contextPath;
    private static String masterAddress;
    private static Long heartbeatInteval;
    private static String token;
    private static Integer heartbeatFailCount = 0;
    public static boolean isSuccessRegisted = false;
    public static WebApplicationContext webApplicationContext;
    public static final ConcurrentHashMap<String, CrawlerResult> completedResultMap = new ConcurrentHashMap<>();

    public static String getWorkerId() {
        return getContextPath();
    }

    public static String getContextPath() {
        if (contextPath == null)
            contextPath = webApplicationContext.getEnvironment().getProperty("context.path");
        return contextPath;
    }

    public static String getMasterServerAddress() {
        if (masterAddress == null)
            masterAddress = webApplicationContext.getEnvironment().getProperty("master.server.address");
        return masterAddress;
    }

    public static synchronized long getHeartbeatInteval() {
        if (heartbeatInteval == null)
            heartbeatInteval = Long.parseLong(webApplicationContext.getEnvironment().getProperty("worker.heartbeat.inteval"));
        return heartbeatInteval;
    }

    public static String getSignatureSecret() {
        if (token == null)
            token = webApplicationContext.getEnvironment().getProperty("signature.token");
        return token;
    }

    public static void setWebApplicationContext(WebApplicationContext webApplicationContext) {
        WorkerContext.webApplicationContext = webApplicationContext;
    }

    public static void setIsSuccessRegisted(boolean isSuccessRegisted) {
        WorkerContext.isSuccessRegisted = isSuccessRegisted;
    }

    public static void addHeartbeatFailCount(int step) {
        heartbeatFailCount = heartbeatFailCount + step;
    }

    public static Integer getHeartbeatFailCount() {
        return heartbeatFailCount;
    }

    public static void clearHeartbeatFailCount() {
        heartbeatFailCount = 0;
    }
}
