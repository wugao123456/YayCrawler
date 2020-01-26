package yaycrawler.master.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import yaycrawler.common.model.CrawlerResult;
import yaycrawler.common.model.RestFulResult;
import yaycrawler.common.model.WorkerHeartbeat;
import yaycrawler.common.model.WorkerRegistration;
import yaycrawler.master.dispatcher.CrawlerTaskDispatcher;
import yaycrawler.master.model.MasterContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ucs_yuananyun on 2016/5/12.
 */
@Controller
@RequestMapping(value = "/worker", produces = "application/json;charset=UTF-8")
public class WorkerController {
    private static Logger logger = LoggerFactory.getLogger(WorkerController.class);

    //    @Autowired
//    private WorkerHearbeatHandler hearbeatHandler;
    @Autowired
    private CrawlerTaskDispatcher taskDispatcher;

    /**
     * worker注册
     *
     * @param registration
     * @return
     */
    @RequestMapping("/register")
    @ResponseBody
    public RestFulResult register(HttpServletRequest request, @RequestBody WorkerRegistration registration) {
        logger.info("接收到worker的注册信息：{}", JSON.toJSON(registration));
        Assert.notNull(registration.getWorkerId());
        Assert.notNull(registration.getWorkerContextPath());
        MasterContext.registeWorker(registration);
        //分派任务
//        taskDispatcher.assignTasks(heartbeat);
        return RestFulResult.success(true);
    }

    /**
     * worker心跳
     *
     * @param heartbeat
     * @return
     */
    @RequestMapping("/heartBeat")
    @ResponseBody
    public RestFulResult heartBeat(HttpServletRequest request, @RequestBody WorkerHeartbeat heartbeat) {
        Assert.notNull(heartbeat.getWorkerId());
        Assert.notNull(heartbeat.getWorkerContextPath());
        logger.debug("workerId {} 剩余任务数{}", heartbeat.getWorkerId(), heartbeat.getWaitTaskCount());
        //刷新心跳信息
        MasterContext.receiveWorkerHeartbeat(heartbeat);
        //更新任务状态
        if (heartbeat.getCompletedCrawlerResultList() != null)
            for (CrawlerResult result : heartbeat.getCompletedCrawlerResultList())
                taskDispatcher.dealResultNotify(result);
        //重新分派任务
        taskDispatcher.assignTasks(heartbeat);
        return RestFulResult.success(true);
    }


    @RequestMapping("/crawlerSuccessNotify")
    @ResponseBody
    public RestFulResult crawlerSuccessNotify(HttpServletRequest request, @RequestBody CrawlerResult crawlerResult) {
        logger.debug("接收到任务执行成功通知:{}", crawlerResult.toString());
        Assert.notNull(crawlerResult);
        taskDispatcher.dealResultNotify(crawlerResult);
        return RestFulResult.success(true);
    }

    @RequestMapping("/crawlerFailureNotify")
    @ResponseBody
    public RestFulResult crawlerFailureNotify(HttpServletRequest request, @RequestBody CrawlerResult crawlerResult) {
        logger.debug("接收到任务执行失败通知:{}", JSON.toJSON(crawlerResult));
        Assert.notNull(crawlerResult);
        taskDispatcher.dealResultNotify(crawlerResult);
        return RestFulResult.success(true);
    }


}
