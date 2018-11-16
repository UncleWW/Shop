package com.ch.evaluation.common.vsftpd.main;

import com.ch.evaluation.common.vsftpd.util.FtpUtil;

public class ftpController {

    public static void main(String[] args) {

        /* boolean flag = FtpUtil.uploadFile("192.168.43.98", "21", "wangwei", "123456",
                    "/var/ftp/wangwei","/images", "handsome3.jpg", "F:\\ftp\\handSome.JPG");*/

        /* boolean flag = FtpUtil.uploadFile("/images1", "handsome.jpg", "F:\\ftp\\handSome.JPG");*/

        /* boolean flag = FtpUtil.uploadFile("handsome.jpg", "F:\\ftp\\handSome.JPG");*/

     /* boolean flag = FtpUtil.downloadFile("192.168.43.98", "21", "wangwei", "123456", "/var/ftp/wangwei/images",
                "handsome2.jpg", "F:/ftp/","");*/
        boolean flag = FtpUtil.downloadFile("handsome2.jpg", "F:/ftp/");

        System.out.println(flag);
    }
}
