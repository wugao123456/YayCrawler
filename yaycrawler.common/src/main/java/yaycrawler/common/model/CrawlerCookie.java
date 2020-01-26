package yaycrawler.common.model;

/**
 * 表示一个cookie对象
 * Created by  yuananyun on 2017/3/25.
 */
public class CrawlerCookie {
    //    private String domain;
    private String name;
    private String value;

//    public String getDomain() {
//        return domain;
//    }
//
//    public void setDomain(String domain) {
//        this.domain = domain;
//    }


    public CrawlerCookie() {
    }

    public CrawlerCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
