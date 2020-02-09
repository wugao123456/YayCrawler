package yaycrawler.worker.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.utils.UrlUtils;
import yaycrawler.common.model.CrawlerRequest;
import yaycrawler.common.utils.FTPUtils;
import yaycrawler.common.utils.FtpClientUtils;
import yaycrawler.common.utils.FtpUtil;
import yaycrawler.common.utils.HttpUtil;
import yaycrawler.spider.listener.IPageParseListener;
import yaycrawler.spider.persistent.IResultPersistentService;
import yaycrawler.spider.persistent.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ucs_yuananyun on 2016/5/23.
 */
@Component
public class DocumentPersistentService implements IResultPersistentService {

   // @Value("${ftp.server.url}")
    private String url;
   // @Value("${ftp.server.port}")
    private int port;
   // @Value("${ftp.server.username}")
    private String username;
   // @Value("${ftp.server.password}")
    private String password;

    @Value("${ftp.server.path}")
    private String ftpPath;

    private FTPUtils ftpUtil;

    @Autowired(required = false)
    private IPageParseListener pageParseListener;

    @Autowired(required = false)
    private DownloadService downloadService;

    @Override
    /**
     * param data {id:"",srcList:""}
     */
    public boolean saveCrawlerResult(String pageUrl, Map<String, Object> data) {
        //List<String> childRequestList = new LinkedList<>();
        try {
            List<String> srcList = new ArrayList<>();
            String id = "";
            //HttpUtil httpUtil = HttpUtil.getInstance();
//            List<Header> headers = new ArrayList<>();
//            headers.add(new BasicHeader("",""));
            for (Object o : data.values()) {
                Map<String, Object> regionData = (Map<String, Object>) o;
                if (regionData == null) continue;
                for (Object src : regionData.values()) {
                    if (src instanceof List)
                        srcList = (List<String>) src;
                    else if (src instanceof HashedMap) {
                        srcList.add(MapUtils.getString((HashedMap) src, "src"));
                    } else {
                        id = String.valueOf(src);
                    }
                }
                CrawlerRequest crawlerRequest = new CrawlerRequest();
                crawlerRequest.setDomain(UrlUtils.getDomain(pageUrl));
                crawlerRequest.setHashCode(DigestUtils.sha1Hex(pageUrl));
                crawlerRequest.setMethod("get");
                crawlerRequest.setUrl(pageUrl + "?$download=pdf");
                crawlerRequest.setExtendMap(ImmutableMap.of("$DOWNLOAD", ".pdf", "$src", srcList));
                downloadService.startCrawlerDownload(Lists.newArrayList(crawlerRequest));
//                if (srcList == null || srcList.isEmpty())
//                    continue;
//                else{
//                    ftpUtil = new FTPUtils();
//                    ftpUtil.connect(url, port, username, password);
//                }
//
//                for (String src : srcList) {
//                    HttpResponse response =  httpUtil.doGet(src,null,null);
//                    if(response.getStatusLine().getStatusCode() != 200) {
//                        childRequestList.add(src);
//                        continue;
//                    }
//
//                    byte[] bytes = EntityUtils.toByteArray(response.getEntity());
//                    String documentName = StringUtils.substringAfterLast(src,"/");
//                    if (!StringUtils.contains(documentName,".")) {
//                        documentName = documentName + ".pdf";
//                    }
//                    File document = new File(ftpPath + "/" + documentName);
//                    Files.createParentDirs(document);
//                    Files.write(bytes,document);
//
//                    String path = UrlUtils.getDomain(pageUrl) + "/" + DigestUtils.sha1Hex(pageUrl) + "/" + id;
//                    //上传文件
//                    ftpUtil.upLoadByFtp(ftpPath + "/" + documentName, path, documentName);
//                    document.delete();
//                }
//                ftpUtil.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
//            if(childRequestList.size() > 0) {
//                CrawlerRequest crawlerRequest = new CrawlerRequest();
//                crawlerRequest.setDomain(UrlUtils.getDomain(pageUrl));
//                crawlerRequest.setHashCode(DigestUtils.sha1Hex(pageUrl));
//                crawlerRequest.setMethod("get");
//                crawlerRequest.setUrl(pageUrl + "?$download=pdf");
//                crawlerRequest.setExtendMap(ImmutableMap.of("$DOWNLOAD",".pdf","$src",childRequestList));
//                pageParseListener.onSuccess(new Request(pageUrl), Lists.newArrayList(crawlerRequest));
//            }
        }
        return true;
    }

    @Override
    public String getSupportedDataType() {
        return PersistentDataType.DOCMUENT;
    }

}
