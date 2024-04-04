package com.viettel.generaltestandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.viettel.generaltestandroid.ftpserver.FTPServer;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static String FTP_HOME_DIRECTORY = "";
    private FTPServer ftpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String homePath = this.getCacheDir().getPath();

        // Check if permission is not granted
        if (!checkPermission()) {
            // Request permission
            requestPermission();
        } else {
            // Permission already granted, proceed with your FTP server setup or other functionality
            startFTPServer();
        }
    }

    // Method to request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your FTP server setup or other functionality
                startFTPServer();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }

    private void startFTPServer() {
        ftpServer = new FTPServer();
        ftpServer.startFTPServer(FTP_HOME_DIRECTORY);
    }

    private void stopFTPServer() {
        if (ftpServer != null) {
            ftpServer.stopFTPServer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopFTPServer();
    }
}