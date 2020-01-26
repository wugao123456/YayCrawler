package yaycrawler.common.model;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import yaycrawler.common.utils.UrlUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/12.
 */
public class CrawlerRequest implements Serializable {

    private String url;
    private String method;
    private Map data;
    private String hashCode;
    private String domain;
    private Map extendMap;

    /**
     * startTime:开始时间
     * workerId:分配workerID
     * message;
     * crawlerRequests
     * hashCode
     *
     * @return
     */
    public Map getExtendMap() {
        return extendMap == null ? new HashMap() : extendMap;
    }

    public void setExtendMap(Map extendMap) {
        this.extendMap = extendMap;
    }

    public String getHashCode() {
        if (hashCode != null) return hashCode;
        return DigestUtils.sha1Hex(getUniqueUrl(this));
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public CrawlerRequest() {
    }

    public CrawlerRequest(String url, String domain, String method) {
        this.url = url;
        this.method = method;
        this.domain = domain;
    }

    public CrawlerRequest(String url, String method, Map data) {
        this.url = url;
        this.method = method;
        this.domain = UrlUtils.getDomain(url);
        this.data = data;
    }


    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url.trim();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CrawlerRequest{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }

    /**
     * 为爬虫任务生成一个唯一的url
     *
     * @param crawlerRequest 请求
     * @return
     */
    private String getUniqueUrl(CrawlerRequest crawlerRequest) {
        if (crawlerRequest.getData() != null && crawlerRequest.getData().size() != 0) {
            StringBuilder urlBuilder = new StringBuilder(crawlerRequest.getUrl().trim());
            String random = DigestUtils.sha1Hex(JSON.toJSONString(crawlerRequest.getData()));
            urlBuilder.append(String.format("%s%s=%s", urlBuilder.indexOf("?") > 0 ? "&" : "?", "random", random));
            return urlBuilder.toString();
        } else {
            return crawlerRequest.getUrl();
        }
    }

}
