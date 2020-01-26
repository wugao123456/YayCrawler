package yaycrawler.dao.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yaycrawler.dao.domain.CrawlerTask;

import javax.transaction.Transactional;

/**
 * Created by  yuananyun on 2017/3/24.
 */
@Repository
public interface CrawlerTaskRepository extends PagingAndSortingRepository<CrawlerTask, String> {


    /**
     * 将任务修改为运行中
     *
     * @param code
     * @param workerId
     */
    @Modifying(clearAutomatically = true)
    @Query("update CrawlerTask set status =-1,workerId=:workerId,startedTime=current_timestamp() where code =:code")
    void updateTaskToRunningStatus(@Param("code") String code, @Param("workerId") String workerId);


    /**
     * 将任务修改为失败状态
     *
     * @param code
     */
    @Modifying(clearAutomatically = true)
    @Query("update CrawlerTask set status =0,message=:message,completedTime=current_timestamp() where code =:code")
    void updateTaskToFailureStatus(@Param("code") String code, @Param("message") String message);

    /**
     * 将任务修改为成功状态
     *
     * @param code
     */
    @Modifying(clearAutomatically = true)
    @Query("update CrawlerTask set status =1,completedTime=current_timestamp() where code =:code")
    void updateTaskToSuccessStatus(@Param("code") String code);

    /**
     * 将任务修改为超时状态
     *
     * @param code
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "update CrawlerTask set status =2 where code =:code")
    void updateTaskToTimeoutStatus(@Param("code") String code);

    /**
     * 刷新超时队列（把超时的运行中队列任务重新加入待执行队列）
     *
     * @param //timeout
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CrawlerTask set status =-2 where status=2 and current_timestamp()-startedTime>1800000")
    void refreshBreakedQueue();
   // void refreshBreakedQueue(@Param("timeout") long timeout);


    Page<CrawlerTask> findAllByStatus(int status, Pageable pageable);
}
