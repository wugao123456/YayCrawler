package yaycrawler.common.utils;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * Created by ucs_guoguibiao on 6/13 0013.
 */
public class FtpClientUtils {
    /**
     * Description: 向FTP服务器上传文件
     *
     * @param url      FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     * @Version1.0 Jul 27, 2008 4:31:09 PM by 崔红保（cuihongbao@d-heaven.com）创建
     */
    public static boolean uploadFile(String url, int port, String username, String password, String path, String filename, InputStream input) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.setControlEncoding("gbk");
            ftp.connect(url, port);//连接FTP服务器
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {

                ftp.disconnect();
                return success;
            }
            makeDirectory(ftp, path);
            ftp.changeWorkingDirectory(path);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.setBufferSize(1024 * 1024 * 10);
            try {
                ftp.storeFile(filename, input);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    ftp.storeFile(PinyinHelper.convertToPinyinString(filename, "", PinyinFormat.WITHOUT_TONE), input);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

    private static void makeDirectory(FTPClient ftpClient, String path) throws IOException {
        String[] dsts = path.split("/");
        StringBuffer temp = new StringBuffer();
        for (String dst : dsts) {
            temp.append("/").append(dst);
            ftpClient.makeDirectory(temp.toString());
        }
    }

    /**
     * Description: 从FTP服务器下载文件
     *
     * @param url        FTP服务器hostname
     * @param port       FTP服务器端口
     * @param username   FTP登录账号
     * @param password   FTP登录密码
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param localPath  下载后保存到本地的路径
     * @return
     * @Version1.0 Jul 27, 2008 5:32:36 PM by 崔红保（cuihongbao@d-heaven.com）创建
     */
    public static boolean downFile(String url, int port, String username, String password, String remotePath, String fileName, String localPath) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        OutputStream is = null;
        try {
            int reply;
            ftp.connect(url, port);
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);//登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(remotePath);//转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    File localFile = new File(localPath + "/" + ff.getName());
                    try {
                        is = new FileOutputStream(localFile);
                        ftp.retrieveFile(ff.getName(), is);
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        is.close();
                    }

                }
            }

            ftp.logout();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    if (is != null)
                        is.close();
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }
}
