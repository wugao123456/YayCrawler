package yaycrawler.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 管理Casperjs的启动和执行
 * Created by ucs_yuananyun on 2016/6/23.
 */
public class CasperjsProgramManager {
    private static Logger logger = LoggerFactory.getLogger(CasperjsProgramManager.class);

    public static String launch(String jsFileName, List<String> params) {
        return launch(jsFileName, null, params);
    }

    public static String launch(String jsFileName, String pageCharset, List<String> params) {
        if (StringUtils.isBlank(jsFileName)) {
            logger.error("待执行的js文件名不能为空！");
            return null;
        }
        BufferedReader br = null;
        try {
            if (pageCharset == null) pageCharset = "utf-8";
            String path = CasperjsProgramManager.class.getResource("/").getPath();
            path = path.substring(1, path.lastIndexOf("/") + 1);
            String os = System.getProperties().getProperty("os.name");
            String casperJsPath = "";
            String phantomJsPath = "";
            if (StringUtils.startsWithIgnoreCase(os, "win")) {
                casperJsPath = path + "casperjs/bin/casperjs.exe";
                phantomJsPath = path + "phantomjs/window/phantomjs.exe";
            } else {
                path = "/" + path;
                casperJsPath = path + "casperjs/bin/casperjs";
                phantomJsPath = path + "phantomjs/linux/phantomjs";
            }
            logger.info("CasperJs程序地址:{}", casperJsPath);
            ProcessBuilder processBuilder = new ProcessBuilder(casperJsPath, jsFileName);
            if (params != null) {
                for (String param : params) {
                    processBuilder.command().add(param);
                }
            }
            processBuilder.command().add("--output-encoding=" + pageCharset);
            processBuilder.command().add("--web-security=no");
            processBuilder.command().add("--ignore-ssl-errors=true");

            processBuilder.directory(new File(path + "casperjs/js"));
            processBuilder.environment().put("PHANTOMJS_EXECUTABLE", phantomJsPath);

            Process p = processBuilder.start();
            InputStream is = p.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, pageCharset));
            StringBuffer sbf = new StringBuffer();
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                sbf.append(tmp).append("\r\n");
            }
            br.close();
            p.destroy();
            return sbf.toString();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return null;
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
