package yaycrawler.common.utils;

/**
 * Created by bill on 2017/5/3.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/**
 * FTP进行文件上传和下载；
 * 支持断点续传；
 */
public final class FTPUtils {
    private final FTPClient ftp = new FTPClient();
    private static volatile FTPUtils instance;

    private FTPUtils() {

    }

    public static FTPUtils getInstance() {
        synchronized (HttpUtil.class) {
            if (FTPUtils.instance == null) {
                instance = new FTPUtils();
            }
            return instance;
        }
    }

    /**
     * @param hostname 如：IP
     * @param port
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public boolean connect(String hostname, int port, String username,
                           String password) throws IOException {
        boolean debug = false;
        if (debug) {
            // 设置将过程中使用到的命令输出到控制台
            this.ftp.addProtocolCommandListener(new PrintCommandListener(
                    new PrintWriter(System.out), true));
        }
        //设置系统类型
        final FTPClientConfig config = new FTPClientConfig(
                FTPClientConfig.SYST_UNIX);
        this.ftp.setControlEncoding("utf-8");
        this.ftp.configure(config);
        try {
            this.ftp.connect(hostname, port);
            if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode())) {
                this.ftp.disconnect();
                System.err.println("FTP server refused connection.");
                return false;
            }
        } catch (IOException e) {
            if (this.ftp.isConnected()) {
                try {
                    this.ftp.disconnect();
                } catch (IOException f) {
                }
            }
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            return false;
        }
        if (!this.ftp.login(username, password)) {
            this.ftp.logout();
            System.err.println("Could not login to server.");
            return false;
        }
        return true;
    }

    public void disconnect() throws IOException {
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {
            }
        }
    }

    /**
     * @param absSrcFileName
     * @param destDir
     * @param destFileName
     * @throws IOException
     */
    public void upLoadByFtp(String absSrcFileName, String destDir,
                            String destFileName) throws IOException {
        // 创建并转到工作目录
//        String absDstDir = this.ftp.printWorkingDirectory() + "/" + destDir;
//        absDstDir = absDstDir.replaceAll("//", "/");
        makeDirectory(ftp, destDir);
        System.out.println("q************" + ftp.printWorkingDirectory() + "*******");
        ftp.changeWorkingDirectory(destDir);
        System.out.println("h************" + ftp.printWorkingDirectory() + "*******");
        // 设置各种属性
        this.ftp.setFileType(FTP.BINARY_FILE_TYPE);
        // Use passive mode as default because most of us are behind firewalls these days.
        this.ftp.enterLocalPassiveMode();
        this.ftp.setControlEncoding("utf-8");
        this.ftp.setBufferSize(1024);
        // 进度监听
        File srcFile = new File(absSrcFileName);
        this.ftp.setCopyStreamListener(new MyCopyStreamListener(srcFile.length()));
        FTPFile[] files = this.ftp.listFiles(destFileName);
        if (files.length == 1) {// 断点续传
            long dstFileSize = files[0].getSize();
            if (srcFile.length() <= dstFileSize) {// 文件已存在
                return;
            }
            boolean b = uploadFile(destFileName, srcFile, this.ftp, dstFileSize);
            if (!b) {// 如果断点续传没有成功，则删除服务器上文件，重新上传
                if (this.ftp.deleteFile(destFileName)) {
                    uploadFile(destFileName, srcFile, this.ftp, 0);
                } else {
                    System.err.println("Delete file fail.");
                }
            }
        } else {
            uploadFile(destFileName, srcFile, this.ftp, 0);
        }
    }

    /**
     * @param remoteFileName
     * @param localFileName
     * @throws IOException
     */
    public void downLoadByFtp(String remoteFileName, String localFileName)
            throws IOException {
        InputStream input = null;
        FileOutputStream fos = null;
        // 设置各种属性
        this.ftp.setBufferSize(1024);
        this.ftp.setDataTimeout(1000 * 10);
        this.ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
        this.ftp.enterLocalPassiveMode();

        // 判断远程文件是否存在
        FTPFile[] files = this.ftp.listFiles(remoteFileName);
        if (files.length != 1) {
            System.err.println("Remote file not exist.");
            return;
        }
        //进度监听
        long remoteSize = files[0].getSize();
        this.ftp.setCopyStreamListener(new MyCopyStreamListener(remoteSize));
        File file = new File(localFileName);
        if (file.exists()) {
            long localSize = file.length();
            if (localSize >= remoteSize) {
                return;
            }
            System.out.println("@@@Break point download.@@@");
            fos = new FileOutputStream(file, true);// append模式
            this.ftp.setRestartOffset(localSize);
        } else {
            fos = new FileOutputStream(file); // override模式
        }
        input = this.ftp.retrieveFileStream(remoteFileName);
        byte[] b = new byte[8192];
        int n = 0;
        while (-1 != (n = input.read(b))) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            fos.write(b, 0, n);
        }
        if (input != null) {
            input.close();
        }
        if (fos != null) {
            fos.flush();
            fos.close();
        }
        if (!this.ftp.completePendingCommand()) {
            System.err.println("Download file fail.");
            this.ftp.logout();
            this.ftp.disconnect();
        }
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
     * @param destFileName
     * @param srcFile
     * @param ftpClient
     * @param dstFileSize  文件写入的起始位置； >0:表示断点续传，<=0:表示上传新文件
     * @return
     * @throws IOException
     */
    private boolean uploadFile(String destFileName, File srcFile,
                               FTPClient ftpClient, long dstFileSize) throws IOException {
        RandomAccessFile input = null;
        OutputStream fout = null;

        input = new RandomAccessFile(srcFile, "r"); // 只读模式
        if (dstFileSize > 0) {// 断点续传
            fout = ftpClient.appendFileStream(destFileName);
            input.seek(dstFileSize);
            ftpClient.setRestartOffset(dstFileSize);
        } else {
            fout = ftpClient.storeFileStream(destFileName);
        }
        byte[] b = new byte[8192]; // 缓存大小
        int n = 0;
        while (-1 != (n = input.read(b))) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            fout.write(b, 0, n);
        }
        if (input != null) {
            input.close();
        }
        if (fout != null) {
            fout.flush();
            fout.close();
        }

        if (!ftpClient.completePendingCommand()) {
            System.err.println("Upload file fail.");
            ftpClient.logout();
            ftpClient.disconnect();
            return false;
        }
        return true;
    }

    /**
     * 在FTP服务器上创建并转到工作目录
     *
     * @param relativePath 相对工作路径，不包含文件名：如 dd/11/22/33
     * @param ftpClient    录创建是否成功
     * @return
     * @throws IOException
     */
    private boolean createDirectory(String relativePath, FTPClient ftpClient)
            throws IOException {
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        String dir = (ftpClient.printWorkingDirectory().equals("/") ? ""
                : ftpClient.printWorkingDirectory()) + relativePath;
        if (!ftpClient.changeWorkingDirectory(dir)) {
            //目录不存在，则创建各级目录
            for (String subDir : relativePath.split("/")) {
                if (!subDir.equals("")) {
                    String newDir = ftpClient.printWorkingDirectory() + "/"
                            + subDir;
                    ftpClient.mkd(newDir);
                    if (!ftpClient.changeWorkingDirectory(newDir)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 进度监听器
     */
    private class MyCopyStreamListener implements CopyStreamListener {
        private long totalSize = 0;
        private long percent = -1; // 进度

        /**
         * 文件的总大小
         *
         * @param totalSize
         */
        public MyCopyStreamListener(long totalSize) {
            super();
            this.totalSize = totalSize;
        }

        @Override
        public void bytesTransferred(CopyStreamEvent event) {
            bytesTransferred(event.getTotalBytesTransferred(),
                    event.getBytesTransferred(), event.getStreamSize());
        }

        //totalBytesTransferred:当前总共已传输字节数;
        //bytesTransferred:最近一次传输字节数
        @Override
        public void bytesTransferred(long totalBytesTransferred,
                                     int bytesTransferred, long streamSize) {
            if (percent >= totalBytesTransferred * 100 / totalSize) {
                return;
            }
            percent = totalBytesTransferred * 100 / totalSize;
            System.out.println("Completed " + totalBytesTransferred + "("
                    + percent + "%) out of " + totalSize + ".");
        }
    }

    public static void main(String[] args) throws IOException {
        String hostname = "192.168.1.36";
        String username = "admin";
        String password = "UcsmyBigdata2015";
        int port = 2121;

        FTPUtils ftp = new FTPUtils();

        //上传文件
        String absSrcFileName = "E:\\至尊召唤师-启蒙书网(1).txt";
        String destDir = "/www.qisuu.com/5d0dca48d0fe43c250922ff79bff53cc7e2185cb/";
        String destFileName = "至尊召唤师.txt";
        ftp.connect(hostname, port, username, password);
        ftp.upLoadByFtp(absSrcFileName, destDir, destFileName);
        ftp.disconnect();
        // 下载文件
        String localFileName = "E:\\m2eclipse-download332233.txt";
        String remoteFileName = "www.qisuu.com/5d0dca48d0fe43c250922ff79bff53cc7e2185cb/至尊召唤师.txt";
        ftp.connect(hostname, port, username, password);
        ftp.downLoadByFtp(remoteFileName, localFileName);
        ftp.disconnect();
    }
}
