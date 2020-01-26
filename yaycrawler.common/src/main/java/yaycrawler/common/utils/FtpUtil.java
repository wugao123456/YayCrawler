package yaycrawler.common.utils;

/**
 * Created by bill on 2017/5/3.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.log4j.Logger;
import yaycrawler.common.model.FtpTransferFileVo;

/**
 * FTP相关操作工具类
 *
 * @author zzj
 * @date May 30, 2016 10:54:20 AM
 */
public class FtpUtil {

    /**
     * 日志记录器
     */
    private static Logger logger = Logger.getLogger(FtpUtil.class);

    /**
     * ftp服务器ip地址
     */
    private String ftpHost = "192.168.128.1";

    /**
     * ftp服务器登录名
     */
    private String ftpUserName = "pds";

    /**
     * ftp服务器登录密码
     */
    private String ftpPassword = "";

    /**
     * ftp服务器所在的根目录
     */
    private String ftpRootDir = "";

    /**
     * ftp服务器端口号
     */
    private int ftpPort = 21;

    /**
     * 文件最大存放天数，0表示永久存储，其他表示到达存放指定存放天数后进行删除文件
     */
    private int maxStoreDays = 0;

    /**
     * 流缓冲区存放的大小
     */
    private final static int UPLOAD_BUFFER_SIZE = 1024 * 1024 * 10;

    /**
     * 扫描总个数(总监控时间=SCAN_NUMBER*ONE_SECOND*SCAN_SECOND)
     * 目前：5*60*（10S）=3000S=50Min
     */
    public static final int SCAN_NUMBER = 5 * 60;

    /**
     * 一秒
     */
    public static final int ONE_SECOND = 1000;

    /**
     * 每次休眠时间
     */
    public static final int SCAN_SECOND = 10;

    public boolean uploadFile(String url, int port, String username, String password, String path, String filename, InputStream input) {
        logger.info("start uploading file to ftp...");
        boolean result = false;
        FTPClient ftpClient = null;
        try {

            // 创建并登陆ftp服务器
            ftpClient = getFTPClient(url, password, username, port);
            if (ftpClient == null)
                return result;

            // 设置ftp上传进度监听器
            ftpClient.setCopyStreamListener(createListener());

            // 设置PassiveMode被动模式-向服务发送传输请求

            ftpClient.enterLocalPassiveMode();

            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            //添加超时监控线程
            new DemaonThread(ftpClient, filename).start();

            // 处理目录的操作
            createDirs(ftpClient, path);

            logger.info("being upload,please waiting...");
            // 最后，将io流上传到指定的目录
            result = ftpClient.storeFile(filename, input);
            input.close();
            logger.info("the upload result is:" + result + " and file path:" + filename);
        } catch (Exception e) {
            logger.error("upload file failed,", e);
            //删除有可能产生的临时文件
            if (filename != null) {
                try {
                    ftpClient.deleteFile(filename);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            try {
                ftpClient.logout();
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                    ftpClient = null;
                }
            } catch (IOException e) {
                logger.error("最后操作ftp期间发生异常,", e);
            }
        }
        return result;
    }

    /**
     * 将指定远程url的网络文件上传到ftp目录
     *
     * @param remoteStorDir ftp的绝对路径(目录+文件名),这个路径必须以/结尾
     * @param fileName      文件所在的http的绝对路径
     * @throws Exception
     * @author zzj
     */
    public boolean uploadHttpFile(InputStream in, String remoteStorDir, String fileName) throws Exception {

        logger.info("start uploading file to ftp...");
        boolean result = false;
        FTPClient ftpClient = null;
        String absFilePath = null;
        try {

            // 创建并登陆ftp服务器
            ftpClient = this.getFTPClient(ftpHost, ftpPassword, ftpUserName, ftpPort);
            /*ftpClient.setDataTimeout(1000*1000);
			ftpClient.setConnectTimeout(connectTimeout)*/

            // 设置ftp上传进度监听器
            //ftpClient.setCopyStreamListener(createListener());

            // 设置PassiveMode被动模式-向服务发送传输请求
            ftpClient.enterLocalPassiveMode();

            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            //添加超时监控线程
            new DemaonThread(ftpClient, fileName).start();

            // 处理目录的操作
            createDirs(ftpClient, remoteStorDir);

            logger.info("being upload,please waiting...");
            absFilePath = remoteStorDir + fileName;

            // 最后，将io流上传到指定的目录
            result = ftpClient.storeFile(absFilePath, in);
            in.close();
            logger.info("the upload result is:" + result + " and file path:" + absFilePath);
        } catch (Exception e) {
            logger.error("upload file failed,", e);
            //删除有可能产生的临时文件
            if (absFilePath != null) {
                ftpClient.deleteFile(absFilePath);
            }
            throw new Exception(e);
        } finally {
            try {
				/*if (!result && absFilePath!=null) {
					ftpClient.deleteFile(absFilePath);
					System.out.println("删除成功!");
				}*/
                ftpClient.logout();
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                    ftpClient = null;
                }
            } catch (IOException e) {
                logger.error("最后操作ftp期间发生异常,", e);
            }
        }
        return result;
    }

    /**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return
     */
    public FTPClient getFTPClient(String ftpHost, String ftpPassword, String ftpUserName, int ftpPort) {
        FTPClient ftpClient = null;
        try {
            // 连接FTP服务器
            ftpClient = new FTPClient();

            logger.info("start connect ftp server.");
            ftpClient.connect(ftpHost, ftpPort);

            //登录到ftp服务器
            ftpClient.login(ftpUserName, ftpPassword);
            ftpClient.setBufferSize(UPLOAD_BUFFER_SIZE);

            //超时时间
            int defaultTimeoutSecond = 30 * 60 * 1000;
            ftpClient.setDefaultTimeout(defaultTimeoutSecond);
            ftpClient.setConnectTimeout(defaultTimeoutSecond);
            ftpClient.setDataTimeout(defaultTimeoutSecond);
            ftpClient.setControlEncoding("utf-8");
            logger.info("connect and login ftp server success.");

            // 设置每次上传的大小
            /*ftpClient.setBufferSize(UPLOAD_BUFFER_SIZE);*/

            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.info("未连接到FTP，用户名或密码错误。");
                ftpClient.logout();
                ftpClient.disconnect();
                ftpClient = null;
            } else {
                System.out.println("FTP connect success!");
            }
        } catch (SocketException e) {
            logger.error("FTP的IP地址可能错误，请正确配置。", e);
        } catch (IOException e) {
            logger.error("FTP的端口错误,请正确配置。", e);
        }
        return ftpClient;
    }

    /**
     * 监控ftpclient超时守护线程
     *
     * @author zzj
     * @date Jun 3, 2016 6:19:18 PM
     */
    private class DemaonThread extends Thread {
        private FTPClient ftpClient;
        private String fileName;
        int num = 0;
        Long start = System.currentTimeMillis() / 1000;

        /**
         * @param ftpClient2
         * @param fileName
         */
        public DemaonThread(FTPClient ftpClient2, String fileName) {
            this.ftpClient = ftpClient2;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            while (num < SCAN_NUMBER && ftpClient.isConnected()) {
                try {
                    System.out.println(fileName + ",monitor ftpclient start..." + (System.currentTimeMillis() / 1000 - start) + " S.");
                    Thread.sleep(ONE_SECOND * SCAN_SECOND);
                    num++;
                    System.out.println(fileName + ",monitor ftpclient timeout...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                System.out.println(fileName + ",monitor ftpclient timeout finish...");
                ftpClient.logout();
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                    ftpClient = null;
                }
            } catch (Exception e) {
                System.out.println(fileName + ",**********monitor happend error," + e.getMessage());
            }
        }
    }

    /**
     * 上传进度监听器（可能导致阻塞）
     *
     * @return 监听器对象
     * @author zzj
     */
    public CopyStreamListener createListener() {
        return new CopyStreamListener() {
            private long start = System.currentTimeMillis() / 1000;

            @Override
            public void bytesTransferred(CopyStreamEvent event) {
                System.out.println("transfeerred");
                bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
            }

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                System.out.println("Spended time: " + (System.currentTimeMillis() / 1000 - start) + " seconds.");
                System.out.println("transfered total bytes:" + totalBytesTransferred + ",per transfeerred types:" + bytesTransferred + ",stream size=" + streamSize);
            }
        };
    }

    /**
     * 创建指定的目录
     *
     * @param ftpClient        ftp客户端
     * @param remoteUpLoadPath ftp服务器目录
     * @throws IOException
     * @author zzj
     */
    public static void createDirs(FTPClient ftpClient, String remoteUpLoadPath) throws IOException {

        //根据路径逐层判断目录是否存在，如果不存在则创建
        //1.首先进入ftp的根目录
        ftpClient.changeWorkingDirectory("/");
        String[] dirs = remoteUpLoadPath.split("/");
        for (String dir : dirs) {
            //2.创建并进入不存在的目录
            if (!ftpClient.changeWorkingDirectory(dir)) {
                int num = ftpClient.mkd(dir);
                System.out.println(num);
                ftpClient.changeWorkingDirectory(dir);
                System.out.println("进入目录成功:" + dir);
            }
        }
    }

    /**
     * 从ftp下载文件
     *
     * @param sourceFtpFileDir 文件所在ftp目录
     * @param //version        所要取得文件的版本号
     * @return 下载后的目录名字
     * @throws Exception 异常
     * @author zzj
     */
    public FTPFile[] listFtpFiles(FTPClient ftpClient, String sourceFtpFileDir) throws Exception {
        logger.info("start downloading file from ftp...");
        FTPFile[] ftpFiles = null;
        try {
            // 设置ftp上传进度监听器
            //ftpClient.setCopyStreamListener(createListener("downloading... "));

            // 设置PassiveMode被动模式-向服务发送传输请求
            ftpClient.enterLocalPassiveMode();

            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            //添加超时监控线程
            new DemaonThread(ftpClient, "downloading... ").start();

            //得到指定目录下所有的文件,指定需要获取的文件的后缀名
            ftpFiles = ftpClient.listFiles(sourceFtpFileDir, new FTPFileSuffixFilter(".rar,.zip"));
            logger.info("list file size is:" + ftpFiles == null ? 0 : ftpFiles.length);
        } catch (Exception e) {
            logger.error("download file failed,", e);
            throw new Exception(e);
        } finally {
        }
        return ftpFiles;
    }

    /**
     * 需要下载的文件的文件流
     *
     * @param //ftpClient ftp客户端对象
     * @param //ftpFiles  获取到的所有文件
     * @param version     版本还
     * @return 文件流
     * @throws IOException
     * @author zzj
     */
    public Map<String, Object> downloadFtpFile(String fileDir, String version, FtpTransferFileVo transferFileVo) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        InputStream inputStream = null;
        FTPClient ftpClient = null;
        try {
            ftpClient = this.getFTPClient(this.getFtpHost(), this.getFtpPassword(), this.getFtpUserName(), this.getFtpPort());
            ;
            FTPFile[] ftpFiles = this.listFtpFiles(ftpClient, this.getFtpRootDir());

            int num = 0;
            String fileName = null;
            for (FTPFile file : ftpFiles) {
                String tn = file.getName();
                tn = tn.substring(0, tn.lastIndexOf("."));
                if (file.isFile() && tn.equals(version)) {
                    fileName = file.getName();
                    num++;
                }
            }
            if (num == 0) {
                throw new Exception("没有找到对应版本【" + version + "】的文件号.");
            } else if (num > 1) {
                throw new Exception("对应版本" + version + "的文件大于1个，个数为：" + num + ",请人工处理");
            }

            //设置文件路径
            transferFileVo.setFileName(fileName);
            transferFileVo.setHttpFileUrl(fileDir + fileName);

            boolean flag = ftpClient.changeWorkingDirectory(fileDir);
            if (!flag) {
                throw new Exception("指定的目录不存在或ftp无法打开，路径为：" + fileDir);
            }
            System.out.println("进入目录:" + fileDir + "结果：" + flag);

            //执行下载文件
            inputStream = ftpClient.retrieveFileStream(fileDir + fileName);
            map.put("error", "false");
        } catch (Exception e) {
            logger.error("发生异常,", e);
            map.put("error", e.getMessage());
        } finally {
            map.put("stream", inputStream);
            map.put("ftpClient", ftpClient);
        }
        return map;
    }

    /**
     * 所有工厂对应的ftp目录列表
     */
    public static Map<String, String> factoryMap = new HashMap<String, String>();

    static {
        factoryMap.put("ss", "ss");
    }

    /**
     * 根据工厂名得到对应的目录
     *
     * @param factory 工厂名
     * @return ftp子目录
     * @author zzj
     */
    public static String getFactoryDir(String factory) {
        String dirName = null;
        for (Map.Entry<String, String> entry : factoryMap.entrySet()) {
            String cname = entry.getKey();
            if (factory.contains(cname)) {
                dirName = entry.getValue();
                break;
            }
        }
        return dirName;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public String getFtpUserName() {
        return ftpUserName;
    }

    public void setFtpUserName(String ftpUserName) {
        this.ftpUserName = ftpUserName;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getFtpRootDir() {
        return ftpRootDir;
    }

    public void setFtpRootDir(String ftpRootDir) {
        this.ftpRootDir = ftpRootDir;
    }

    public int getMaxStoreDays() {
        return maxStoreDays;
    }

    public void setMaxStoreDays(int maxStoreDays) {
        this.maxStoreDays = maxStoreDays;
    }
}
