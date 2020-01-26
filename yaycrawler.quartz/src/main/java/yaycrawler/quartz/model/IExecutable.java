package yaycrawler.quartz.model;

import org.quartz.JobExecutionContext;

/**
 * Created by ucs_yuananyun on 2016/6/6.
 */
public interface IExecutable {

    /**
     * 执行某个操作
     *
     * @param context
     * @return
     */
    boolean execute(JobExecutionContext context);
}
