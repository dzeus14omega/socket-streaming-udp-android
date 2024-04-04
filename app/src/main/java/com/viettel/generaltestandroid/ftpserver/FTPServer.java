package com.viettel.generaltestandroid.ftpserver;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;

import java.io.File;

public class FTPServer {
    private org.apache.ftpserver.FtpServer ftpServer;

    public void startFTPServer(String path) {
        FtpServerFactory serverFactory = new FtpServerFactory();

        // Define the listener
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2121); // Use a port number above 1024
        serverFactory.addListener("default", factory.createListener());

        // Set the home directory for the FTP server
        File homeDirectory = new File(path); // This is the path you want to serve
        serverFactory.setUserManager(new UserManager(homeDirectory));

        // Start the server
        try {
            ftpServer = serverFactory.createServer();
            ftpServer.start();
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    public void stopFTPServer() {
        if (ftpServer != null && !ftpServer.isStopped()) {
            ftpServer.stop();
        }
    }
}
