package yaycrawler.cache.service;

import org.springframework.cache.Cache;

/**
 * Created by ucs_yuananyun on 2016/8/30.
 */
public interface ICacheService {

    /**
     * 获取或创建一个指定名称的缓存
     *
     * @param cacheName
     * @return
     */
    Cache getOrCreateMemoryCache(String cacheName);
}
