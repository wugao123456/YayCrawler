package yaycrawler.master.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yaycrawler.common.model.CrawlerRequest;
import yaycrawler.common.model.CrawlerResult;
import yaycrawler.common.model.WorkerHeartbeat;
import yaycrawler.common.model.WorkerRegistration;
import yaycrawler.master.communication.WorkerActor;
import yaycrawler.master.model.MasterContext;
import yaycrawler.master.service.ICrawlerQueueService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局任务调度器
 * Created by ucs_yuananyun on 2016/5/17.
 */
@Component
public class CrawlerTaskDispatcher {

    private static Logger logger = LoggerFactory.getLogger(CrawlerTaskDispatcher.class);

    @Value("${worker.task.batchSize}")
    private Integer batchSize;


    @Autowired
    private ICrawlerQueueService  crawlerQueueService;
    //private ICrawlerQueueService queueService;

    @Autowired
    private WorkerActor workerActor;

    public void dealResultNotify(CrawlerResult crawlerResult) {
        if (crawlerResult == null) return;
        if (crawlerResult.isSuccess()) {
            if (crawlerResult.getCrawlerRequestList().size() > 0)
                crawlerQueueService.pushTasksToWaitingQueue(crawlerResult.getCrawlerRequestList(), false);
            crawlerQueueService.moveRunningTaskToSuccessQueue(crawlerResult);
        } else {
            crawlerQueueService.moveRunningTaskToFailQueue(crawlerResult.getKey(), crawlerResult.getMessage());
        }
    }

    /**
     * 接收任务
     */

    /**
     * 分派任务
     */
    public void assignTasks(WorkerHeartbeat workerHeartbeat) {
        ConcurrentHashMap<String, WorkerRegistration> workerListMap = MasterContext.workerRegistrationMap;
        WorkerRegistration workerRegistration = workerListMap.get(workerHeartbeat.getWorkerContextPath());
        if (workerRegistration == null) return;

        logger.info("worker:{}剩余任务数:{}", workerHeartbeat.getWorkerId(), workerHeartbeat.getWaitTaskCount());
        int canAssignCount = batchSize - workerHeartbeat.getWaitTaskCount();
        if (canAssignCount <= 0) return;
        List<CrawlerRequest> crawlerRequests = crawlerQueueService.fetchTasksFromWaitingQueue(canAssignCount);
        if (crawlerRequests.size() == 0) return;
        boolean flag = workerActor.assignTasks(workerRegistration, crawlerRequests);
        if (flag) {
            logger.info("给worker:{}分派了{}个任务", workerHeartbeat.getWorkerId(), crawlerRequests.size());
            crawlerQueueService.moveWaitingTaskToRunningQueue(workerHeartbeat.getWorkerId(), crawlerRequests);
        }
    }


}
