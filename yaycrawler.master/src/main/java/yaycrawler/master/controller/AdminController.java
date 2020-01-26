package yaycrawler.master.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import yaycrawler.common.model.CrawlerRequest;
import yaycrawler.common.model.QueueQueryParam;
import yaycrawler.common.model.RestFulResult;
import yaycrawler.master.model.MasterContext;
import yaycrawler.master.service.ICrawlerQueueService;

import java.util.List;

/**
 * Created by ucs_yuananyun on 2016/5/12.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ICrawlerQueueService crawlerQueueService;

    @RequestMapping("/registerQueues")
    @ResponseBody
    public RestFulResult acceptAdminTask(@RequestBody List<CrawlerRequest> crawlerRequests) {
        Boolean flag = crawlerQueueService.pushTasksToWaitingQueue(crawlerRequests, true);
        if (flag)
            return RestFulResult.success(flag);
        else
            return RestFulResult.failure(flag.toString());
    }

    @RequestMapping(value = "/retrievedWorkerRegistrations", method = RequestMethod.POST)
    @ResponseBody
    public Object retrievedWorkerRegistrations() {
        return RestFulResult.success(MasterContext.workerRegistrationMap.values());
    }

    @RequestMapping(value = "/retrievedSuccessQueueRegistrations", method = RequestMethod.POST)
    @ResponseBody
    public RestFulResult retrievedSuccessQueueRegistrations(@RequestBody QueueQueryParam queryParam) {
        return RestFulResult.success(crawlerQueueService.querySuccessQueues(queryParam));
    }

    @RequestMapping(value = "/retrievedFailQueueRegistrations", method = RequestMethod.POST)
    @ResponseBody
    public RestFulResult retrievedFailQueueRegistrations(@RequestBody QueueQueryParam queryParam) {
        return RestFulResult.success(crawlerQueueService.queryFailQueues(queryParam));
    }

    @RequestMapping(value = "/retrievedRunningQueueRegistrations", method = RequestMethod.POST)
    @ResponseBody
    public RestFulResult retrievedRunningQueueRegistrations(@RequestBody QueueQueryParam queryParam) {
        return RestFulResult.success(crawlerQueueService.queryRunningQueues(queryParam));
    }

    @RequestMapping(value = "/retrievedWaitingQueueRegistrations", method = RequestMethod.POST)
    @ResponseBody
    public RestFulResult retrievedWaitingQueueRegistrations(@RequestBody QueueQueryParam queryParam) {
        return RestFulResult.success(crawlerQueueService.queryWaitingQueues(queryParam));
    }

}
