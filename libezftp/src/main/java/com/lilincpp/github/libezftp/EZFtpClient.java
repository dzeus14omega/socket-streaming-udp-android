package com.lilincpp.github.libezftp;

import com.lilincpp.github.libezftp.callback.OnEZFtpCallBack;
import com.lilincpp.github.libezftp.callback.OnEZFtpDataTransferCallback;

import java.util.List;

/**
 * Proxy for {@link EZFtpClientImpl}
 *
 * @author lilin
 */
public final class EZFtpClient implements IEZFtpClient {

    private static final String TAG = "EZFtpClient";

    private IEZFtpClient ftpClientIml;

    public EZFtpClient() {
        ftpClientIml = new EZFtpClientImpl();
    }

    @Override
    public void connect(String serverIp, int port, String userName, String password) {
        connect(serverIp, port, userName, password, null);
    }

    @Override
    public void connect(String serverIp, int port, String userName, String password, OnEZFtpCallBack<Void> callBack) {
        ftpClientIml.connect(serverIp, port, userName, password, callBack);
    }

    @Override
    public void disconnect() {
        ftpClientIml.disconnect();
    }

    @Override
    public void disconnect(OnEZFtpCallBack<Void> callBack) {
        ftpClientIml.disconnect(callBack);
    }

    @Override
    public boolean isConnected() {
        return ftpClientIml.isConnected();
    }

    @Override
    public void getCurDirFileList(OnEZFtpCallBack<List<EZFtpFile>> callBack) {
        ftpClientIml.getCurDirFileList(callBack);
    }

    @Override
    public void getCurDirPath(OnEZFtpCallBack<String> callBack) {
        ftpClientIml.getCurDirPath(callBack);
    }

    @Override
    public void changeDirectory(String path, OnEZFtpCallBack<String> callBack) {
        ftpClientIml.changeDirectory(path, callBack);
    }

    @Override
    public void backup(OnEZFtpCallBack<String> callBack) {
        ftpClientIml.backup(callBack);
    }

    @Override
    public void downloadFile(EZFtpFile remoteFile, String localFilePath, OnEZFtpDataTransferCallback callback) {
        ftpClientIml.downloadFile(remoteFile, localFilePath, callback);
    }

    @Override
    public void uploadFile(String localFilePath, OnEZFtpDataTransferCallback callback) {
        ftpClientIml.uploadFile(localFilePath, callback);
    }

    @Override
    public boolean curDirIsHomeDir() {
        return ftpClientIml != null && ftpClientIml.curDirIsHomeDir();
    }

    @Override
    public void backToHomeDir(OnEZFtpCallBack<String> callBack) {
        ftpClientIml.backToHomeDir(callBack);
    }

    @Override
    public void release() {
        ftpClientIml.release();
    }
}
