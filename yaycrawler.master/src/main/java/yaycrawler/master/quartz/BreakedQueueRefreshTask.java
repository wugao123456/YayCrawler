package yaycrawler.master.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaycrawler.master.service.ICrawlerQueueService;

/**
 * Created by ucs_yuananyun on 2016/5/31.
 */
public class BreakedQueueRefreshTask {
    private static final Logger logger = LoggerFactory.getLogger(BreakedQueueRefreshTask.class);

    private ICrawlerQueueService queueService;
    private Long queueTimeOut;

    public void refreshBreakedQueue() {
        logger.info("开始刷新中断任务队列……");
        queueService.refreshBreakedQueue(queueTimeOut);
    }

    public void setQueueTimeOut(Long queueTimeOut) {
        this.queueTimeOut = queueTimeOut;
    }

    public void setQueueService(ICrawlerQueueService queueService) {
        this.queueService = queueService;
    }
}
