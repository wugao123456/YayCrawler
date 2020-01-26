package yaycrawler.spider.cookie;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yaycrawler.common.model.CrawlerCookie;

import java.util.List;

/**
 * 动态cookie管理器
 * Created by  yuananyun on 2017/3/25.
 */
@Component
public class DynamicCookieManager {

    @Autowired
    List<ICookieProvider> cookieProviderList;

    public List<CrawlerCookie> getCookiesByDomain(String domain) {
        if (StringUtils.isEmpty(domain)) return null;
        for (ICookieProvider provider : cookieProviderList) {
            if (provider.support(domain))
                return provider.getCookies();
        }
        return null;
    }

}
