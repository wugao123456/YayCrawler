package yaycrawler.dao.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import yaycrawler.cache.service.ICacheService;
import yaycrawler.dao.repositories.RuleVersionRepository;

/**
 * 数据访问的切面类
 * Created by ucs_yuananyun on 2016/8/30.
 */
//@Component
//@Aspect
public class DataRepositoryAspect {

    public static final String RESPOSITORY_PONINT_CUT = "execution(public * yaycrawler.dao.repositories..*.*(..))";
    public static final String RULE_CACHE_NAME = "dataRepositoryCache";
    public static final String VERSION = "ruleDataVersion";

    @Autowired
    private RuleVersionRepository versionRepository;

    @Autowired
    private ICacheService cacheService;


    @Pointcut(RESPOSITORY_PONINT_CUT)
    public void pointCut() {
    }

    @Before(value = "pointCut()")
    public Object before(JoinPoint point) {
        /**
         * 对比缓存中的版本号和数据库中的版本号，如果不一致则重新加载缓存
         */
        Integer maxVersionNo = versionRepository.getMaxVersionNo();
        Cache ruleDataCache = cacheService.getOrCreateMemoryCache(RULE_CACHE_NAME);
        Integer cacheVersion = ruleDataCache.get(VERSION, Integer.class);
        if (cacheVersion == null || cacheVersion < maxVersionNo) {

        }

        Object target = point.getTarget();
        System.out.println(target.toString());
        String method = point.getSignature().getName();
        System.out.println(method);

        MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) point;
        try {
            Object result = methodPoint.proceed(point.getArgs());
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

}
