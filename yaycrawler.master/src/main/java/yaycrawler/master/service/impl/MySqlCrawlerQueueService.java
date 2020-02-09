package yaycrawler.master.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yaycrawler.common.model.CrawlerRequest;
import yaycrawler.common.model.CrawlerResult;
import yaycrawler.common.model.QueueQueryParam;
import yaycrawler.common.model.QueueQueryResult;
import yaycrawler.dao.domain.CrawlerTask;
import yaycrawler.dao.repositories.CrawlerTaskRepository;
import yaycrawler.master.service.ICrawlerQueueService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 基于mysql的队列服务  CrawlerTask
 * Created by  yuananyun on 2017/3/24.
 */
@Service(value = "crawlerQueueService")
//@Transactional
public class MySqlCrawlerQueueService implements ICrawlerQueueService {

    @Autowired
    private CrawlerTaskRepository crawlerTaskRepository;

    //    待运行
    private static int STATUS_WAITING = -2;
    //    运行中
    private static int STATUS_RUNNING = -1;
    //    失败
    private static int STATUS_FAILURE = 0;
    //    成功
    private static int STATUS_SUCCESS = 1;
    //    超时
    private static int STATUS_TIMEOUT = 2;

    /**
     * 添加任务到待执行队列
     *
     * @param crawlerRequests  任务
     * @param removeDuplicated 当存在相同任务时是否移除相同任务
     * @return
     */
    @Override
    public boolean pushTasksToWaitingQueue(List<CrawlerRequest> crawlerRequests, boolean removeDuplicated) {
        boolean exists;
        for (CrawlerRequest crawlerRequest : crawlerRequests) {
           // exists = crawlerTaskRepository.exists(crawlerRequest.getHashCode());
           exists = crawlerTaskRepository.existsById(crawlerRequest.getHashCode());
            if (exists && !removeDuplicated) break;
            //if (exists)
               // crawlerTaskRepository.delete(crawlerRequest.getHashCode());
            CrawlerTask task = CrawlerTask.convertFromCrawlerRequest(crawlerRequest);
            task.setStatus(STATUS_WAITING);
            task.setCreatedTime(new Date());
            crawlerTaskRepository.save(task);
        }
        return true;
    }

    /**
     * 从待执行队列中拿取指定数目的任务
     *
     * @param taskCount 任务数目
     * @return
     */
    @Override
    public List<CrawlerRequest> fetchTasksFromWaitingQueue(long taskCount) {
        Pageable pageable =  PageRequest.of(0, (int) taskCount,  Sort.by(new Sort.Order(Sort.Direction.ASC, "createdTime")));
        Page<CrawlerTask> pageData = crawlerTaskRepository.findAllByStatus(STATUS_WAITING, pageable);
        return getCrawlerRequests(pageData.getContent());
    }


    /**
     * 将任务从待执行移到执行中队列
     *
     * @param workerId        任务在哪个Worker上执行
     * @param crawlerRequests
     */
    @Override
    public boolean moveWaitingTaskToRunningQueue(String workerId, List<CrawlerRequest> crawlerRequests) {
        for (CrawlerRequest request : crawlerRequests) {
            crawlerTaskRepository.updateTaskToRunningStatus(request.getHashCode(), workerId);
        }
        return true;
    }

    /**
     * 当任务执行失败后将任务移到失败队列
     *
     * @param taskCode
     * @param message
     * @return
     */
    @Override
    public boolean moveRunningTaskToFailQueue(String taskCode, String message) {
        crawlerTaskRepository.updateTaskToFailureStatus(taskCode, message);
        return true;
    }

    /**
     * 从执行中队列把成功的任务移到成功队列
     *
     * @param crawlerResult
     * @return
     */
    @Override
    public boolean moveRunningTaskToSuccessQueue(CrawlerResult crawlerResult) {
        crawlerTaskRepository.updateTaskToSuccessStatus(crawlerResult.getKey());
        //把附带的子任务加入队列
        List<CrawlerRequest> childRequestList = crawlerResult.getCrawlerRequestList();
        if (childRequestList != null && childRequestList.size() > 0) {
            pushTasksToWaitingQueue(childRequestList, false);
        }
        return true;
    }

    /**
     * 刷新超时队列（把超时的运行中队列任务重新加入待执行队列）
     *
     * @param timeout
     */
    @Override
    public void refreshBreakedQueue(Long timeout) {
       // crawlerTaskRepository.refreshBreakedQueue(timeout);
        crawlerTaskRepository.refreshBreakedQueue();
    }


    /**
     * 查询待执行队列
     *
     * @param queryParam
     * @return
     */
    @Override
    public QueueQueryResult queryWaitingQueues(QueueQueryParam queryParam) {
        Pageable pageable =  PageRequest.of(queryParam.getPageIndex(), queryParam.getPageSize(),  Sort.by(new Sort.Order(Sort.Direction.ASC, "createdTime")));
        Page<CrawlerTask> pageData = crawlerTaskRepository.findAllByStatus(STATUS_WAITING, pageable);
        return new QueueQueryResult(getCrawlerRequests(pageData.getContent()), pageData.getTotalPages(), pageData.getTotalElements());
    }

    /**
     * 查询执行中队列
     *
     * @param queryParam
     * @return
     */
    @Override
    public QueueQueryResult queryRunningQueues(QueueQueryParam queryParam) {
        Pageable pageable =  PageRequest.of(queryParam.getPageIndex(), queryParam.getPageSize(),  Sort.by(new Sort.Order(Sort.Direction.DESC, "startedTime")));
        Page<CrawlerTask> pageData = crawlerTaskRepository.findAllByStatus(STATUS_RUNNING, pageable);
        return new QueueQueryResult(getCrawlerRequests(pageData.getContent()), pageData.getTotalPages(), pageData.getTotalElements());
    }

    /**
     * 查询失败队列
     *
     * @param queryParam
     * @return
     */
    @Override
    public QueueQueryResult queryFailQueues(QueueQueryParam queryParam) {
        Pageable pageable =  PageRequest.of(queryParam.getPageIndex(), queryParam.getPageSize(),  Sort.by(new Sort.Order(Sort.Direction.DESC, "completedTime")));
        Page<CrawlerTask> pageData = crawlerTaskRepository.findAllByStatus(STATUS_FAILURE, pageable);
        return new QueueQueryResult(getCrawlerRequests(pageData.getContent()), pageData.getTotalPages(), pageData.getTotalElements());
    }

    /**
     * 查询成功队列
     *
     * @param queryParam
     * @return
     */
    @Override
    public QueueQueryResult querySuccessQueues(QueueQueryParam queryParam) {
        Pageable pageable =  PageRequest.of(queryParam.getPageIndex(), queryParam.getPageSize(),  Sort.by(new Sort.Order(Sort.Direction.DESC, "completedTime")));
        Page<CrawlerTask> pageData = crawlerTaskRepository.findAllByStatus(STATUS_SUCCESS, pageable);
        return new QueueQueryResult(getCrawlerRequests(pageData.getContent()), pageData.getTotalPages(), pageData.getTotalElements());
    }


    private List<CrawlerRequest> getCrawlerRequests(List<CrawlerTask> taskList) {
        List<CrawlerRequest> requestList = new ArrayList<>(taskList.size());
        for (CrawlerTask task : taskList) {
            CrawlerRequest crawlerRequest = task.convertToCrawlerRequest();
            crawlerRequest.getExtendMap().put("startTime", task.getStartedTime());
            crawlerRequest.getExtendMap().put("extraInfo", task.getMessage());
            requestList.add(crawlerRequest);
        }
        return requestList;
    }


}
