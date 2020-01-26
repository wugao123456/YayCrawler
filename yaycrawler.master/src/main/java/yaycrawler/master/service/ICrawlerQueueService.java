package yaycrawler.master.service;

import yaycrawler.common.model.CrawlerRequest;
import yaycrawler.common.model.CrawlerResult;
import yaycrawler.common.model.QueueQueryParam;
import yaycrawler.common.model.QueueQueryResult;

import java.util.List;

/**
 * Created by yuananyun on 2016/8/7.
 */
public interface ICrawlerQueueService {
    /**
     * 添加任务到待执行队列
     *
     * @param crawlerRequests  任务
     * @param removeDuplicated 当存在相同任务时是否移除相同任务
     * @return
     */
    boolean pushTasksToWaitingQueue(List<CrawlerRequest> crawlerRequests, boolean removeDuplicated);

    /**
     * 从待执行队列中拿取指定数目的任务
     *
     * @param taskCount 任务数目
     * @return
     */
    List<CrawlerRequest> fetchTasksFromWaitingQueue(long taskCount);

    /**
     * 将任务从待执行移到执行中队列
     *
     * @param workerId        任务在哪个Worker上执行
     * @param crawlerRequests
     */
    boolean moveWaitingTaskToRunningQueue(String workerId, List<CrawlerRequest> crawlerRequests);

    /**
     * 当任务执行失败后将任务移到失败队列
     *
     * @param taskCode
     * @param message
     * @return
     */
    boolean moveRunningTaskToFailQueue(String taskCode, String message);

    /**
     * 从执行中队列把成功的任务移到成功队列
     *
     * @param crawlerResult
     * @return
     */
    boolean moveRunningTaskToSuccessQueue(CrawlerResult crawlerResult);


    /**
     * 刷新超时队列（把超时的运行中队列任务重新加入待执行队列）
     *
     * @param timeout
     */
    void refreshBreakedQueue(Long timeout);

    /**
     * 查询待执行队列
     *
     * @param queryParam
     * @return
     */
    QueueQueryResult queryWaitingQueues(QueueQueryParam queryParam);

    /**
     * 查询执行中队列
     *
     * @param queryParam
     * @return
     */
    QueueQueryResult queryRunningQueues(QueueQueryParam queryParam);

    /**
     * 查询失败队列
     *
     * @param queryParam
     * @return
     */
    QueueQueryResult queryFailQueues(QueueQueryParam queryParam);

    /**
     * 查询成功队列
     *
     * @param queryParam
     * @return
     */
    QueueQueryResult querySuccessQueues(QueueQueryParam queryParam);
}
