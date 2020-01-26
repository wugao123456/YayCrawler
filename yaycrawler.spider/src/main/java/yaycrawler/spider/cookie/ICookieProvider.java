package yaycrawler.spider.cookie;

import yaycrawler.common.model.CrawlerCookie;

import java.util.List;

/**
 * 表示一个Cookie自动获取器
 * Created by  yuananyun on 2017/3/25.
 */
public interface ICookieProvider {

    /**
     * 表示是否支持某个域名获取cookie
     *
     * @param domain
     * @return
     */
    boolean support(String domain);


    List<CrawlerCookie> getCookies();

}
