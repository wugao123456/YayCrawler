package yaycrawler.quartz.model;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    public static final String TRIGGERNAME = "triggerName";
    public static final String TRIGGERGROUP = "triggerGroup";
    public static final String STARTTIME = "startTime";
    public static final String ENDTIME = "endTime";
    public static final String REPEATCOUNT = "repeatCount";
    public static final String REPEATINTERVEL = "repeatInterval";

    public static final Map<String, String> status = ImmutableMap.of("ACQUIRED", "运行中", "PAUSED", "暂停中", "WAITING", "等待中");

    public static final String JOB_PARAM_KEY = "scheduleJob";

}
