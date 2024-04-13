package com.viettel.generaltestandroid.testRTPv1;


/**
 * RtpPacket implements a RTP packet.
 */
public class RTPPacket {

    // |0       0       1       2       |
    // |0.......8.......6.......4.......|
    // |V PXC   MT      Seqnum (16)     |
    // |................................|
    // |Timestamp (32)                  |
    // |                                |
    // |................................|
    // | SSRC (32)                      |
    // |                                |
    // |................................|
    // | CSRC list (16 items x 32 bits) |
    // |                                |
    // |................................|
    // V: version, 2 bits
    // P: padding, 1 bit
    // X: extension, 1 bit
    // C: CSRC count, 4 bits
    // M: marker, 1 bit
    // T: payload type: 7 bits

    private byte[] packet; // RTP header + payload
    private int packetLength;

    private long timestamp;

    public RTPPacket(byte[] buffer, int seqNum, long timestamp, String deviceId) {
        packet = buffer;
        setVersion(2);
        setPayloadType(0x0F);
        setSequenceNumber(seqNum);
        int hash = 0;
        for (int i = 0; i < deviceId.length(); i++) {
            hash = 31 * hash + deviceId.charAt(i);
        }
        setSscr(hash);
        setTimestamp(timestamp);
    }

    /** Returns the RTP packet in raw bytes. */
    public byte[] getRawPacket() {
        return packet;
    }

    public int getPacketLength() {
        return packetLength;
    }

    public int getHeaderLength() {
        return (12 + 4 * getCscrCount());
    }

    public int getPayloadLength() {
        return (packetLength - getHeaderLength());
    }

    public void setPayloadLength(int length) {
        packetLength = getHeaderLength() + length;
    }

    public int getVersion() {
        return ((packet[0] >> 6) & 0x03);
    }

    public void setVersion(int v) {
        if (v > 3) throw new RuntimeException("illegal version: " + v);
        packet[0] = (byte) ((packet[0] & 0x3F) | ((v & 0x03) << 6));
    }

    int getCscrCount() {
        return (packet[0] & 0x0F);
    }

    public int getPayloadType() {
        return (packet[1] & 0x7F);
    }

    public void setPayloadType(int pt) {
        packet[1] = (byte) ((packet[1] & 0x80) | (pt & 0x7F));
    }

    public int getSequenceNumber() {
        return (int) get(2, 2);
    }

    public void setSequenceNumber(int sn) {
        set((long) sn, 2, 2);
    }

    public long getTimestamp() {
        return get(4, 4);
    }

    public void setTimestamp(long timestamp) {
        set(timestamp, 4, 4);
    }

    void setSscr(long ssrc) {
        set(ssrc, 8, 4);
    }

    private long get(int begin, int length) {
        long n = 0;
        for (int i = begin, end = i + length; i < end; i++) {
            n = (n << 8) | ((long) packet[i] & 0xFF);
        }
        return n;
    }

    private void set(long n, int begin, int length) {
        for (int i = begin + length - 1; i >= begin; i--) {
            packet[i] = (byte) (n & 0x0FFL);
            n >>= 8;
        }
    }

    /*private byte[] createRtpHeader(int payloadType) {
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
    }*/
}