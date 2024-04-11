package com.viettel.generaltestandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 100;

    private final static String SP_CAM_WIDTH = "cam_width";
    private final static String SP_CAM_HEIGHT = "cam_height";
    private final static String SP_DEST_IP = "dest_ip";
    private final static String SP_DEST_PORT = "dest_port";

    private final static int DEFAULT_FRAME_RATE = 15;
    private final static int DEFAULT_BIT_RATE = 500000;

    Camera camera;
    SurfaceHolder previewHolder;
    byte[] previewBuffer;
    boolean isStreaming = false;
    AvcEncoder encoder;
    DatagramSocket udpSocket;
    InetAddress address;
    int port;
    ArrayList<byte[]> encDataList = new ArrayList<byte[]>();
    ArrayList<Integer> encDataLengthList = new ArrayList<Integer>();

    Runnable senderRun = new Runnable() {
        @Override
        public void run() {
            while (isStreaming) {
                boolean empty = false;
                byte[] encData = null;

                synchronized(encDataList) {
                    if (encDataList.size() == 0)
                    {
                        empty = true;
                    }
                    else
                        encData = encDataList.remove(0);
                }
                if (empty) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                try {
                    Log.d("DebugStreamP2P", "Send UDP packet: data size:" + encData.length);
                    DatagramPacket packet = new DatagramPacket(encData, encData.length, address, port);
                    udpSocket.send(packet);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //TODO:
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_main);

        this.findViewById(R.id.btnCamSize).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showSettingsDlg();
                    }
                });

        this.findViewById(R.id.btnStream).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isStreaming)
                        {
                            ((Button)v).setText("Stream");
                            stopStream();
                        }
                        else
                        {
                            showStreamDlg();
                        }
                    }
                });

        SurfaceView svCameraPreview = (SurfaceView) this.findViewById(R.id.svCameraPreview);
        this.previewHolder = svCameraPreview.getHolder();
        this.previewHolder.addCallback(this);

        //setContentView(R.layout.activity_main);

        // Check if permission is not granted
//        if (!checkPermission()) {
//            // Request permission
//            requestPermission();
//        } else {
//        }
    }

    // Method to request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, PERMISSION_REQUEST_CODE);
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
            } else {
            }
        }
    }

    @Override
    protected void onPause()
    {
        this.stopStream();


        if (encoder != null)
            encoder.close();

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        this.camera.addCallbackBuffer(this.previewBuffer);

        if (this.isStreaming)
        {
            if (this.encDataLengthList.size() > 100)
            {
                Log.e(TAG, "OUT OF BUFFER");
                return;
            }

            byte[] encData = this.encoder.offerEncoder(data);
            if (encData.length > 0)
            {
                synchronized(this.encDataList)
                {
                    this.encDataList.add(encData);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopCamera();
    }

    private void startStream(String ip, int port) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        int width = sp.getInt(SP_CAM_WIDTH, 0);
        int height = sp.getInt(SP_CAM_HEIGHT, 0);

        this.encoder = new AvcEncoder();
        this.encoder.init(width, height, DEFAULT_FRAME_RATE, DEFAULT_BIT_RATE);

        Log.d("DebugStreamP2P", "Start stream udp for ip: {" + ip + ":" + port +"}");

        try
        {
            this.udpSocket = new DatagramSocket();
            this.address = InetAddress.getByName(ip);
            this.port = port;
        }
        catch (SocketException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("DebugStreamP2P", "Socket exception: " + e.toString());

            return;
        }
        catch (UnknownHostException e)
        {
            // TODO Auto-generated catch block
            Log.d("DebugStreamP2P", "UnknownHost exception: " + e.toString());
            e.printStackTrace();
            return;
        }
        sp.edit().putString(SP_DEST_IP, ip).commit();
        sp.edit().putInt(SP_DEST_PORT, port).commit();

        this.isStreaming = true;
        Thread thrd = new Thread(senderRun);
        thrd.start();

        ((Button)this.findViewById(R.id.btnStream)).setText("Stop");
        this.findViewById(R.id.btnCamSize).setEnabled(false);
    }

    private void stopStream() {
        this.isStreaming = false;

        if (this.encoder != null)
            this.encoder.close();
        this.encoder = null;

        this.findViewById(R.id.btnCamSize).setEnabled(true);
    }

    private void startCamera() {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        int width = sp.getInt(SP_CAM_WIDTH, 0);
        int height = sp.getInt(SP_CAM_HEIGHT, 0);
        if (width == 0)
        {
            Camera tmpCam = Camera.open();
            Camera.Parameters params = tmpCam.getParameters();
            final List<Size> prevSizes = params.getSupportedPreviewSizes();
            int i = prevSizes.size()-1;
            width = prevSizes.get(i).width;
            height = prevSizes.get(i).height;
            sp.edit().putInt(SP_CAM_WIDTH, width).commit();
            sp.edit().putInt(SP_CAM_HEIGHT, height).commit();
            tmpCam.release();
            tmpCam = null;
        }

        this.previewHolder.setFixedSize(width, height);

        int stride = (int) Math.ceil(width/16.0f) * 16;
        int cStride = (int) Math.ceil(width/32.0f)  * 16;
        final int frameSize = stride * height;
        final int qFrameSize = cStride * height / 2;

        this.previewBuffer = new byte[frameSize + qFrameSize * 2];

        try
        {
            camera = Camera.open();
            camera.setPreviewDisplay(this.previewHolder);
            Camera.Parameters params = camera.getParameters();
            params.setPreviewSize(width, height);
            params.setPreviewFormat(ImageFormat.YV12);
            camera.setParameters(params);
            camera.addCallbackBuffer(previewBuffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        }
        catch (IOException e)
        {
            //TODO:
        }
        catch (RuntimeException e)
        {
            //TODO:
        }
    }

    private void stopCamera() {
        if (camera != null)
        {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void showStreamDlg()
    {
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.stream_dlg_view, null);

        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        String ip = sp.getString(SP_DEST_IP, "");
        int port = sp.getInt(SP_DEST_PORT, -1);
        if (ip.length() > 0)
        {
            EditText etIP = (EditText)content.findViewById(R.id.etIP);
            etIP.setText(ip);
            EditText etPort = (EditText)content.findViewById(R.id.etPort);
            etPort.setText(String.valueOf(port));
        }

        AlertDialog.Builder dlgBld = new AlertDialog.Builder(this);
        dlgBld.setTitle(R.string.app_name);
        dlgBld.setView(content);
        dlgBld.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EditText etIP = (EditText) ((AlertDialog)dialog).findViewById(R.id.etIP);
                        EditText etPort = (EditText) ((AlertDialog)dialog).findViewById(R.id.etPort);
                        String ip = etIP.getText().toString();
                        int port = Integer.valueOf(etPort.getText().toString());
                        if (ip.length() > 0 && (port >=0 && port <= 65535))
                        {
                            startStream(ip, port);
                        }
                        else
                        {
                            //TODO:
                        }
                    }
                });
        dlgBld.setNegativeButton(android.R.string.cancel, null);
        dlgBld.show();
    }

    private void showSettingsDlg()
    {
        Camera.Parameters params = camera.getParameters();
        final List<Size> prevSizes = params.getSupportedPreviewSizes();
        String[] choiceStrItems = new String[prevSizes.size()];
        ArrayList<String> choiceItems = new ArrayList<String>();
        for (Size s : prevSizes)
        {
            choiceItems.add(s.width + "x" + s.height);
        }
        choiceItems.toArray(choiceStrItems);

        AlertDialog.Builder dlgBld = new AlertDialog.Builder(this);
        dlgBld.setTitle(R.string.app_name);
        dlgBld.setSingleChoiceItems(choiceStrItems, 0, null);
        dlgBld.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        int pos = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        Size s = prevSizes.get(pos);
                        SharedPreferences sp = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                        sp.edit().putInt(SP_CAM_WIDTH, s.width).commit();
                        sp.edit().putInt(SP_CAM_HEIGHT, s.height).commit();

                        stopCamera();
                        startCamera();
                    }
                });
        dlgBld.setNegativeButton(android.R.string.cancel, null);
        dlgBld.show();
    }
}