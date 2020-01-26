package yaycrawler.spider.crawler;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.processor.PageProcessor;
import yaycrawler.spider.service.PageSiteService;

import java.util.ArrayList;

/**
 * Created by yuananyun on 2016/5/2.
 */
public class YaySpider extends Spider {
    private PageSiteService pageSiteService;
    private String domain;

    public YaySpider(String domain, PageSiteService pageSiteService, PageProcessor pageProcessor) {
        super(pageProcessor);
        this.pageSiteService = pageSiteService;
        this.site = pageSiteService.getSite(domain);
        if (site == null) site = Site.me();
        this.setSpiderListeners(new ArrayList<SpiderListener>());
        spawnUrl = true;
        this.domain = domain;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @Override
    protected void onError(Request request) {
        //失败后换代理重试一次
        Integer tryTimes = (Integer) request.getExtra("tryTimes");
        if (tryTimes == null) tryTimes = 0;
        if (tryTimes == 0) {
            //TODO 异常恢复
            this.site = pageSiteService.getSite(domain, true);
            request.putExtra("tryTimes", ++tryTimes);
            request.getExtras().remove("cookieIds");
            this.addRequest(request);
        } else
            super.onError(request);

    }


    public String getDomain() {
        return domain;
    }

}
