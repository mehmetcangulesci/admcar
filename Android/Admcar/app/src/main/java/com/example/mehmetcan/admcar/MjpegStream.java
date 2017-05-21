package com.example.mehmetcan.admcar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

public class MjpegStream implements Runnable {

    // Typical max length of header data. (Maximum header length)
    private final static int HEADER_MAX_LENGTH = 100;

    // Expected length of an mjpeg frame. (Max frame length (100kB))
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    //private final String CONTENT_TYPE_PREFIX = "multipart/x-mixed-replace;boundary=";

    // Name of content length header.
    // Optional MJPEG frame header key used to indicate bytes of jpeg file data.
    // This header is optional and depends on the API call used with the camera.
    private final String CONTENT_LENGTH = "Content-Length";

    // The first two bytes of every JPEG stream are the Start Of Image (SOI) marker values FFh D8h.
    // Start Of Image marker. Size: 2 bytes The first two bytes of every image.
    private final byte[] SOI_MARKER = {(byte) 0xFF, (byte) 0xD8};

    // End Of Image (EOF), size: 2 bytes The last two bytes of every JPEG image. (FFh D9h)
    private final byte[] EOF_MARKER = {(byte) 0xFF, (byte) 0xD9};
    //private String mBoundary;
    private int mContentLength = -1;
    private String mUrl;
    private boolean mRun;
    private Callback onFrameReadCallback;

    public MjpegStream(String url) {
        mUrl = url;
        mRun = false;
        onFrameReadCallback = null;
    }

    public void start() {
        mRun = true;
        new Thread(this, "MJPEG").start();
    }

    public void stop() {
        mRun = false;
    }

    public void setCallback(Callback callback) {
        onFrameReadCallback = callback;
    }

    /**
     * @param in
     * @param sequence (ID)
     * @return The index of the first byte after the given sequence, or -1 if not found
     * @throws IOException
     */
    private int getEndOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0; //tracks number of sequence chars found
        byte c;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte(); //read next byte
            if (c == sequence[seqIndex]) {
                seqIndex++; //increment seq char found index
                //check if we have the whole sequence
                if (seqIndex == sequence.length)
                    return i + 1;
            } else
                //reset index if we don't find all sequence characters before breaking
                seqIndex = 0;
        }
        return -1;
    }

    /**
     * @param in
     * @param sequence (ID)
     * @return Get the index of of the beginning of the sequence
     * @throws IOException
     */
    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSequence(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }


    /**
     * Get the content length from the input stream.
     * Parse the content length string for a MJPEG frame from the given bytes. The string is parsed into an int and returned.
     *
     * @param headerBytes
     * @return int
     * @throws IOException
     * @throws NumberFormatException
     */
    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);

        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }


    /**
     * Read the next MjpegFrame from the stream.
     *
     * @return The next MJPEG frame.
     * @throws IOException (If there is an error.)
     */
    public Bitmap readFrame(DataInputStream in) throws IOException {
        //int mContentLength = -1;

        in.mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(in, SOI_MARKER);
        in.reset();
        byte[] header = new byte[headerLen];
        in.readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            mContentLength = getEndOfSequence(in, EOF_MARKER);
        }
        in.reset();
        byte[] frameData = new byte[mContentLength];
        in.skipBytes(headerLen);
        in.readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }

    public void run() {
        // Uniform Resource Identifier (URI)
        // Uniform Resource Locator (URL)
        URI uri = URI.create(mUrl);
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(new HttpGet(uri));
        } catch (IOException e) {

            return;
        }

        HttpEntity httpEntity = httpResponse.getEntity();

        // Bir seferde bir çok byte'ı gerektiği şekilde yeniden doldurulur
        /**
         * BufferedInputStream:
         *
         * As bytes from the stream are read or skipped, the internal buffer is refilled as
         * necessary from the contained input stream, many bytes at a time.
         */
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(httpEntity.getContent(), FRAME_MAX_LENGTH);
        } catch (IOException e) {

            return;
        }

        // Girdi akışından Java veri türlerini makineye bağımsız bir şekilde okutmaya izin verir.
        /**
         * DataInputStream:
         *
         * It lets an application read primitive Java data types from an underlying
         * input stream in a machine-independent way.
         */
        DataInputStream mjpeg = new DataInputStream(in);

        while (mRun) {
            Bitmap bitmap = null;

            try {
                bitmap = readFrame(mjpeg);
            } catch (IOException e) {

                break;
            }

            if (onFrameReadCallback != null) {
                onFrameReadCallback.onFrameRead(bitmap);
            }
        }

        try {
            mjpeg.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        public void onFrameRead(Bitmap bitmap);
    }
}
