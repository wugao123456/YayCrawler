package yaycrawler.master.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import yaycrawler.common.model.CommunicationAPIs;
import yaycrawler.common.model.CrawlerRequest;
import yaycrawler.common.model.RestFulResult;
import yaycrawler.common.model.WorkerRegistration;
import yaycrawler.common.utils.HttpUtils;

import java.util.List;

/**
 * Created by ucs_yuananyun on 2016/5/13.
 */
@Component
public class WorkerActor {
    private static Logger logger = LoggerFactory.getLogger(WorkerActor.class);
    @Value("${signature.token}")
    private String secret;

    public boolean assignTasks(WorkerRegistration workerRegistration, List<CrawlerRequest> taskList) {
        String targetUrl = CommunicationAPIs.getFullRemoteUrl(workerRegistration.getWorkerContextPath(), CommunicationAPIs.MASTER_POST_WORKER_TASK_ASSIGN);
        RestFulResult result = HttpUtils.doSignedHttpExecute(secret, targetUrl, HttpMethod.POST, taskList);
        if (result != null && result.hasError())
            logger.error(result.getMessage());
        return result != null && !result.hasError();
    }

}
