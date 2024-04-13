package com.viettel.generaltestandroid.testRTPv2;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class RTPPacketBuilder {

    private DatagramSocket socket;
    private InetAddress destinationAddress;
    private int destinationPort;
    private int sequenceNumber;
    private long timestamp;

    public RTPPacketBuilder(DatagramSocket socket, InetAddress address, int port) throws SocketException, UnknownHostException {
        this.socket = socket;
        this.destinationAddress = address;
        this.destinationPort = port;
    }

    public void sendH264RtpPacket(byte[] h264Frame, long timestamp, int seqNum) throws IOException {
        int payloadType = 96; // H.264 payload type (can be changed based on RTP profile)

        // RTP header
        byte[] rtpHeader = createRtpHeader(payloadType);
        Log.d("DebugStreamP2P", "debug rtp header: " + byteArrayToHexString(rtpHeader));

        // Concatenate RTP header and H.264 frame
        byte[] rtpPacket = new byte[rtpHeader.length + h264Frame.length];
        System.arraycopy(rtpHeader, 0, rtpPacket, 0, rtpHeader.length);
        System.arraycopy(h264Frame, 0, rtpPacket, rtpHeader.length, h264Frame.length);

        // Send RTP packet
        Log.d("DebugStreamP2P", "start send package seqNum = " + seqNum + " data size = " + rtpPacket.length);
        DatagramPacket packet = new DatagramPacket(rtpPacket, rtpPacket.length, destinationAddress, destinationPort);
        socket.send(packet);

        // Update sequence number and timestamp
        sequenceNumber = seqNum;
        this.timestamp = timestamp; // Assuming 30 FPS, adjust as per your frame rate
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // Convert byte to unsigned integer (0-255) and then to hexadecimal string
            sb.append(String.format("%02X ", b & 0xFF));
        }
        // Trim the trailing space
        return sb.toString().trim();
    }

    private byte[] createRtpHeader(int payloadType) {
        byte[] header = new byte[12];

        // RTP version (2), padding (0), extension (0), CC (0)
        header[0] = (byte) 0x80; // Assuming version 2

        // Payload type (H.264)
        header[1] = (byte) (payloadType & 0x7F);

        // Sequence number (incremented for each packet)
        header[2] = (byte) (sequenceNumber >> 8);
        header[3] = (byte) (sequenceNumber);

        // Timestamp (incremented based on frame rate)
        header[4] = (byte) (timestamp >> 24);
        header[5] = (byte) (timestamp >> 16);
        header[6] = (byte) (timestamp >> 8);
        header[7] = (byte) (timestamp);

        // SSRC identifier (random for now)
        header[8] = 0;
        header[9] = 0;
        header[10] = 0;
        header[11] = 0;

        return header;
    }

}