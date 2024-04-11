package com.viettel.generaltestandroid.ftpserver;

import android.util.Log;

import com.lilincpp.github.libezftp.EZFtpServer;
import com.lilincpp.github.libezftp.user.EZFtpUser;
import com.lilincpp.github.libezftp.user.EZFtpUserPermission;
import com.blankj.utilcode.util.NetworkUtils;

public class FTPServerController {
    private static final String TAG = "FtpServerController";
    private EZFtpServer ftpServer;

    public void startFtpServer(String name, String pw, String sharePath, int port) {
        Log.d(TAG, "startFtpServer at thread: " + Thread.currentThread().getName());
        if (ftpServer == null) {
            ftpServer = new EZFtpServer.Builder()
                    .addUser(new EZFtpUser(name, pw, sharePath, EZFtpUserPermission.WRITE))
                    .setListenPort(port)
                    .create();
            ftpServer.start();
        } else {
            if (ftpServer.isStopped()) {
                ftpServer.start();
            }
        }

        final String serverIp = NetworkUtils.getIPAddress(true) + ":" + port;


        Log.d(TAG, "username=" + name + "\n"
                            + "pw=" + pw + "\n"
                            + "share path=" + sharePath + "\n"
                            + "serverIp=" + serverIp + "\n\n"
                            + "Ftp Server is running!" + "\n"
                            + "1.Browser open url: ftp://" + serverIp + "\n"
                            + "2.Use this or other ftp client connect server\n");
    }


    public void stopFtpServer() {
        if (ftpServer != null) {
            ftpServer.stop();
            Log.d(TAG, "stopFtpServer!");

        }
    }
}
