package com.example.luoluo.llacamera;

import android.app.Application;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;

public class LLAHardEncode {

    private final static String TAG = "MeidaCodec";

    private int TIMEOUT_USEC = 0;
    public boolean isRuning = false;

    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    int m_framerate;

    private static int yuvqueuesize = 10;
    //存放yuv420p数据队列 此队列在多线程下会保持同步
    public  ArrayBlockingQueue<byte[]> myuvQueue;
    private static int h264QueueSize = 10;
    public  ArrayBlockingQueue<byte []> mH264Queue;

    private  Context mContext;

    public  LLAHardEncode(int width, int height, int framerate,Context context){

        m_width  = width;
        m_height = height;
        m_framerate = framerate;
        myuvQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
        mH264Queue = new ArrayBlockingQueue<byte[]>(h264QueueSize);
        mContext = context;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        //接收的数据类型为yuv任何格式
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width*height*5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_framerate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔时间 单位s

        try {
            //创建h264类型的编码器
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //硬编码
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }

    public byte[] configbyte;
    public void startEncoderThread(){
        Thread encoderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                isRuning = true;
                long pts =  0;
                long frameCount = 0;
                if (myuvQueue.size() == 0){
                    Log.d(TAG, "HardEncode- no enough yuv data to encode.!");
                }
                Log.d(TAG, "HardEncode- out format is "+mediaCodec.getOutputFormat());
                while (isRuning){
                    //取出队列第一个元素并删除队列中第一个 如果队列为空返回NUll
                    byte[] inputData = myuvQueue.poll();
                    if (inputData != null){
                        try {
                            //开始编码
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >=0){
                                //有可放入数据的队列
                                pts = computePresentationTime(frameCount);//转成为妙
                                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                                inputBuffer.clear();
                                inputBuffer.put(inputData,0,inputData.length);
//                                inputBuffer.put(inputData);//放入yuv到硬编码输入队列
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputData.length, pts, 0);
                                frameCount +=1;
                            }else {
                                Log.d(TAG, "HardEncode- can't get input queue.");
                            }

                            //取出编码好的h264
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            //
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            while (outputBufferIndex >= 0){
                                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                                //取出数据
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
//                                BUFFER_FLAG_END_OF_STREAM
                                if(bufferInfo.flags == BUFFER_FLAG_CODEC_CONFIG){
                                    //保存pps和sps 只有刚开始第一帧里面有
                                    configbyte = new byte[bufferInfo.size];
                                    configbyte = outData;

                                }else if(bufferInfo.flags == BUFFER_FLAG_KEY_FRAME){
                                    //关键帧- 都要加上pps和sps
                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                                    Log.d(TAG, "HardEncode- encode a keyfame data. size="+keyframe.length);
                                    for (int i=0;i<10;i++){
                                        Log.d(TAG, "HardEncode- i=" +i +"value="+keyframe[i]);
                                    }

                                    dumpFile("hardEncoder_"+m_width+m_height+".h264",keyframe);
                                }else{
                                    Log.d(TAG, "HardEncode- encode a frame.size="+outData.length);
                                    for (int i=0;i<10;i++){
                                        Log.d(TAG, "HardEncode- ii=" +i +"value="+outData[i]);
                                    }
                                    dumpFile("hardEncoder_"+m_width+m_height+".h264",outData);
                                }

                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            }
                        }catch (Throwable t){
                            //编码异常
                            t.printStackTrace();
                        }
                    }else {
                        //没有获取到yuv数据不需要编码
                        try {
                            Log.d(TAG, "HardEncode- this yuv data is nul.!!");
                            Thread.sleep(500);//微秒
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        encoderThread.start();
    }


    private void StopEncoder() {
        try {
            isRuning = false;
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {

        return  frameIndex * 1000000 / m_framerate;//转微秒
    }

    //写入文件
    FileOutputStream mFos;
    private  void dumpFile(String fileName, byte[] data) {

        File file = new File(mContext.getFilesDir(), fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ;
        try {
            if (mFos == null){
                mFos = new FileOutputStream(file);
            }

        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file " + fileName, ioe);
        }
        try {
            mFos.write(data);
//            mFos.close();
        } catch (IOException ioe) {
            throw new RuntimeException("failed writing data to file " + fileName, ioe);
        }
    }

    //获取支持的硬编码类型
    private int getSupportColorFormat() {
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    System.out.println("found");
                    found = true;
                }
            }
            if (!found)
                continue;
            codecInfo = info;
        }

        Log.e("AvcEncoder", "Found " + codecInfo.getName() + " supporting " + "video/avc");

        // Find a color profile that the codec supports
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        Log.e("AvcEncoder",
                "length-" + capabilities.colorFormats.length + "==" + Arrays.toString(capabilities.colorFormats));

        for (int i = 0; i < capabilities.colorFormats.length; i++) {

            switch (capabilities.colorFormats[i]) {
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
                case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:

                    Log.e("AvcEncoder", "supported color format::" + capabilities.colorFormats[i]);
                    return capabilities.colorFormats[i];
                default:
                    Log.e("AvcEncoder", "unsupported color format " + capabilities.colorFormats[i]);
                    break;
            }
        }

        return -1;
    }

}
