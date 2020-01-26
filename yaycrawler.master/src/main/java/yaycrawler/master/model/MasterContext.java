package yaycrawler.master.model;

import yaycrawler.common.model.WorkerHeartbeat;
import yaycrawler.common.model.WorkerRegistration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Master的环境上下文
 * Created by ucs_yuananyun on 2016/5/13.
 */
public class MasterContext {

    public static final ConcurrentHashMap<String, WorkerRegistration> workerRegistrationMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, WorkerHeartbeat> workerHeartbeatMap = new ConcurrentHashMap<>();

    public static void registeWorker(WorkerRegistration registration) {
        if (registration.getWorkerId() != null && registration.getWorkerContextPath() != null) {
            workerRegistrationMap.put(registration.getWorkerContextPath(), registration);
        }
    }


    public static void receiveWorkerHeartbeat(WorkerHeartbeat heartbeat) {
        long currentTime = System.currentTimeMillis();
        WorkerRegistration registration = workerRegistrationMap.get(heartbeat.getWorkerContextPath());
        if (registration == null) {
            //通过心跳注册
            registration = new WorkerRegistration(heartbeat.getWorkerId(), heartbeat.getWorkerContextPath());
            registration.setHeartbeatInteval(heartbeat.getHeartbeatInteval());
            registeWorker(registration);
        }
        registration.setLastHeartbeatTime(currentTime);
        registration.setWaitTaskCount(heartbeat.getWaitTaskCount());
        workerHeartbeatMap.put(heartbeat.getWorkerContextPath(), heartbeat);
    }


}
