package yaycrawler.spider.downloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import yaycrawler.common.model.CrawlerCookie;
import yaycrawler.common.utils.UrlUtils;
import yaycrawler.dao.domain.PageInfo;
import yaycrawler.dao.domain.SiteCookie;
import yaycrawler.dao.service.PageCookieService;
import yaycrawler.dao.service.PageParserRuleService;
import yaycrawler.spider.cookie.DynamicCookieManager;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ucs_yuananyun on 2016/5/27.
 */
@Component
public class GenericCrawlerDownLoader extends AbstractDownloader {

//    private static Logger logger = LoggerFactory.getLogger(GenericCrawlerDownLoader.class);

    @Autowired
    private PageParserRuleService pageParserRuleService;
    @Autowired
    private PageCookieService pageCookieService;

    @Autowired
    private DynamicCookieManager dynamicCookieManager;

    private CrawlerHttpClientDownloader httpClientDownloader;
    private PhantomJsMockDonwnloader mockDonwnloader;


    public GenericCrawlerDownLoader() {
        httpClientDownloader = new CrawlerHttpClientDownloader();
        mockDonwnloader = new PhantomJsMockDonwnloader();
    }

    private static Pattern redirectPattern = Pattern.compile("<script.*(?s)setInterval.*location.href\\s*=.*(?s).*</script>");

    @Override
    public Page download(Request request, Task task) {
        PageInfo pageInfo = pageParserRuleService.findOnePageInfoByRgx(request.getUrl());
        if (pageInfo == null && request.getExtra("$pageInfo") != null) {
            pageInfo = (PageInfo) request.getExtra("$pageInfo");
        }
        boolean isJsRendering = pageInfo != null && "1".equals(pageInfo.getIsJsRendering());
        String pageUrl = request.getUrl();
        SiteCookie siteCookie = pageCookieService.getCookieByUrl(pageUrl);
        String cookie = "";
        if (siteCookie != null) {
            cookie = siteCookie.getCookie();
            String cookieId = siteCookie.getId();
            request.putExtra("cookieId", cookieId);
        }
        //获取动态的cookies
        List<CrawlerCookie> dynamicCookieList = dynamicCookieManager.getCookiesByDomain(UrlUtils.getDomain(pageUrl));
        if (dynamicCookieList != null) {
            cookie += ";";
            for (CrawlerCookie crawlerCookie : dynamicCookieList) {
                cookie += String.format("%s=%s", crawlerCookie.getName(), crawlerCookie.getValue());
            }
        }
        Page page = !isJsRendering ? httpClientDownloader.download(request, task, cookie) : mockDonwnloader.download(request, task, cookie);
        if (!isJsRendering && (!"post".equalsIgnoreCase(request.getMethod()) && page != null) && page.getRawText() != null && redirectPattern.matcher(page.getRawText()).find())
            page = mockDonwnloader.download(request, task, cookie);
        if (page != null && page.getRawText() == null)
            return null;
        return page;
    }

    @Override
    public void setThread(int threadNum) {
        httpClientDownloader.setThread(threadNum);
        mockDonwnloader.setThread(threadNum);
    }
}
