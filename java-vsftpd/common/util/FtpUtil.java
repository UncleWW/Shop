package com.ch.evaluation.common.vsftpd.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.chainsaw.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * @author 011336
 * @date 2018/11/04
 * ftp服务器文件上传下载
 */
public class FtpUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(FtpUtil.class);
    private static String host;
    private static String port;
    private static String username;
    private static String password;
    private static String basePath;
    private static String filePath;
    private static String localPath;

    /**
     *读取配置文件信息
     * @return
     */
    public static  void getPropertity(){
        Properties properties = new Properties();
        ClassLoader load = Main.class.getClassLoader();
        InputStream is = load.getResourceAsStream("conf/vsftpd.properties");
        try {
            properties.load(is);
            host=properties.getProperty("vsftpd.ip");//通用
            port=properties.getProperty("vsftpd.port");//通用
            username=properties.getProperty("vsftpd.user");//通用
            password=properties.getProperty("vsftpd.pwd");//通用
            basePath=properties.getProperty("vsftpd.remote.base.path");//服务器 基路径
            filePath=properties.getProperty("vsftpd.remote.file.path");//服务器 文件路径
            localPath=properties.getProperty("vsftpd.local.file.path");//本地 下载到本地的目录
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传重载
     * @param filename  上传到服务器端口重命名
     * @param sourceFilePathName    源文件【路径+文件】
     * @return
     */
    public static boolean uploadFile(String filename, String sourceFilePathName) {
        getPropertity();
       return uploadFile( host,  port,  username,  password,  basePath,
                 filePath,  filename,  sourceFilePathName);
    }

    /**
     * 上传重载
     * @param filePath  上传到服务器的相对路径
     * @param filename  上传到服务器端口重命名
     * @param sourceFilePathName    源文件【路径+文件】
     * @return
     */
    public static boolean uploadFile(String filePath,String filename, String sourceFilePathName) {
        getPropertity();
       return uploadFile( host,  port,  username,  password,  basePath,
                 filePath,  filename,  sourceFilePathName);
    }

    /**
     *下载重载
     * @param fileName  要下载的文件名
     * @return
     */
    public static boolean downloadFile(String fileName) {
        getPropertity();
        return downloadFile( host,  port,  username,  password,  basePath,
                filePath, fileName,  localPath, null);
    }

    /**
     *下载重载
     * @param filePath  要下载的文件所在服务器的相对路径
     * @param fileName  要下载的文件名
     * @return
     */
    public static boolean downloadFile(String filePath,String fileName) {
        getPropertity();
        return downloadFile( host,  port,  username,  password,  basePath,
                filePath, fileName,  localPath, null);
    }

    /**
     * 下载重载
     * @param fileName   要下载的文件名
     * @param filePath  要下载的文件所在服务器的相对路径
     * @param rename    下载后重命名[null或空不进行重命名]
     * @return
     */
    public static boolean downloadFile(String filePath,String fileName, String rename) {
        getPropertity();
        return downloadFile( host,  port,  username,  password,  basePath,
                filePath, fileName,  localPath, rename);
    }

    /**
     * 下载重载
     * @param fileName  要下载的文件名
     * @param localPath 下载时，到本地的路径
     * @param filePath  要下载的文件所在服务器的相对路径
     * @param rename    下载后重命名[null或空不进行重命名]
     * @return
     */
    public static boolean downloadFile(String filePath,String fileName,String localPath, String rename) {
        getPropertity();
        return downloadFile( host,  port,  username,  password,  basePath,
                 filePath, fileName,  localPath, rename);
    }

    /**
     * Description: 向FTP服务器上传文件
     * @param host FTP服务器hostname
     * @param port FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param basePath FTP服务器基础目录
     * @param filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
     * @param filename 上传到FTP服务器上的文件名
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String host, String port, String username, String password, String basePath,
                                     String filePath, String filename, String sourceFilePathName) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            int portNum = Integer.parseInt(port);
            int reply;
            ftp.connect(host, portNum);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            //切换到上传目录
            if (!ftp.changeWorkingDirectory(basePath+filePath)) {
                //如果目录不存在创建目录
                String[] dirs = filePath.split("/");
                String tempPath = basePath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) continue;
                    tempPath += "/" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {
                        if (!ftp.makeDirectory(tempPath)) {
                            return result;
                        } else {
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
            //为了加大上传文件速度，将InputStream转成BufferInputStream  , InputStream input
            FileInputStream input = new FileInputStream(new File(sourceFilePathName));
            BufferedInputStream in = new BufferedInputStream(input);
            //加大缓存区
            ftp.setBufferSize(1024*1024);
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //上传文件
            if (!ftp.storeFile(filename, in)) {
                return result;
            }
            in.close();
            ftp.logout();
            result = true;
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
        return result;
    }

    /**
     * Description: 从FTP服务器下载文件
     * @param host FTP服务器hostname
     * @param port FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param basePath FTP服务器上的相对路径
     * @param fileName 要下载的文件名
     * @param localPath 下载后保存到本地的路径
     * @return
     */
    public static boolean downloadFile(String host, String port, String username, String password, String basePath,
                                       String filePath, String fileName, String localPath, String rename) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            int portNum = Integer.parseInt(port);
            int reply;
            ftp.connect(host, portNum);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            //ftp.changeWorkingDirectory(basePath);// 转移到FTP服务器目录
            if (!ftp.changeWorkingDirectory(basePath+filePath)) {
                LOGGER.info("服务器端目录不存在...");
            }
            FTPFile[] fs = ftp.listFiles();
            boolean flag = true;
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    flag = false;
                    rename = StringUtils.isEmpty(rename)?ff.getName():rename;
                    File localFile = new File(localPath + "/" + rename);
                    OutputStream is = new FileOutputStream(localFile);
                    ftp.retrieveFile(ff.getName(), is);
                    is.close();
                }
            }
            if(flag) LOGGER.info("服务器端文件不存在...");
            ftp.logout();
            result = true;
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }




   /* public static void main(String[] args) {
        try {
            FileInputStream in=new FileInputStream(new File("F:\\ftp\\handSome.JPG"));
            boolean flag = uploadFile("192.168.43.98", 21, "wangwei", "123456", "/var/ftp/wangwei","/images", "handsome2.jpg", in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
      *//*  boolean flag = downloadFile("192.168.43.98", "21", "wangwei", "123456", "/var/ftp/wangwei/images",
                "handsome2.jpg", "F:/ftp/");*//*
        System.out.println(flag);
    }*/
}
