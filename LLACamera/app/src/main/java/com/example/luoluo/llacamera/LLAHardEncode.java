package com.example.luoluo.llacamera;

import android.app.Application;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.example.luoluo.llacamera.Model.VideoFrameModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;

public class LLAHardEncode {

    private final static String TAG = "MeidaCodec";

    private int TIMEOUT_USEC = 0;
    private boolean mIsWiteH264File;
    public boolean isRuning = false;

    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    int m_framerate;

    private static int yuvqueuesize = 10;
    //存放yuv420p数据队列 此队列在多线程下会保持同步
    public  ArrayBlockingQueue<byte[]> myuvQueue;
    private static int h264QueueSize = 10;
    public  ArrayBlockingQueue<VideoFrameModel> mH264Queue;

    private  Context mContext;

    //flags 帧类型 1代码关键帧 0代表p或者b帧
//    public native void sendH264Data2(byte[] packByte,int lenght ,int flags,long packtCount);
    public  native  void sendPactedToNetwork(byte[] packByte,int lenght,int flags,long packtCounts);


    public  LLAHardEncode(int width, int height, int framerate,Context context){

        m_width  = width;
        m_height = height;
        m_framerate = framerate;
        myuvQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
        mH264Queue = new ArrayBlockingQueue<VideoFrameModel>(h264QueueSize);
        mIsWiteH264File = false;
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

        final Semaphore mSendSemphore = new Semaphore(0);
    public void startSendPacketThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Camera2- start send thread.");
               sendPacket();

            }
        }).start();
    }

    public  long mSendPacketCount = 0;
    private void  sendPacket(){

        try {
            if (mH264Queue.size() > 0){
                VideoFrameModel videoFrameModel = mH264Queue.take();//如果队列已空则会阻塞线程  当队列中有了又不会阻塞了
                //底层ffmpeg去发送  ---此处应该从sps中分析出pts
                Log.d(TAG, "Camera2- send packt size = "
                        +videoFrameModel.datas.length
                        +" suplus count="+mH264Queue.size()+" sendPacketCount="+mSendPacketCount);
                sendPactedToNetwork(videoFrameModel.datas,videoFrameModel.datas.length,videoFrameModel.type,mSendPacketCount);
                mSendPacketCount++;
                //继续发送下一包
                sendPacket();

            }else {
                //等待更多包
                Log.d(TAG, "Camera2- need more H264 packt.!");
                mSendSemphore.acquire();
                sendPacket();
            }

        }catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public byte[] configbyte;
    public void startEncoderThread(){
        Thread encoderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                isRuning = true;
                long pts =  0;
                long frameCount = 0;
                //mediaCodec.getOutputFormat()
                Log.d(TAG, "HardEncode- start encode Thread."+Thread.currentThread());
                while (isRuning){

                    //取出yuv队列第一个元素并删除队列中第一个 如果队列为空返回NUll
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
                                Log.d(TAG, "HardEncode- add a frame need encode.");
                            }else {
                                Log.d(TAG, "HardEncode- can't get input queue.");
                            }

                            //取出编码好的h264
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            //
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            //取出所有已编码好的包
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
                                    for (int i = 0;i<bufferInfo.size;i++){
                                        Log.d(TAG, "HardEncode- spsAndPpsInfo i="+i+" value="+configbyte[i]);
                                    }
                                    VideoFrameModel videoFrameModel = new VideoFrameModel();
                                    videoFrameModel.datas = configbyte;
                                    videoFrameModel.type = VideoFrameModel.spsOrppsType;
                                    mH264Queue.put(videoFrameModel);

                                }else if(bufferInfo.flags == BUFFER_FLAG_KEY_FRAME){
                                    //关键帧- 都要加上pps和sps  所以此处得到的是pps+sps+I帧
                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);

                                    Log.d(TAG, "HardEncode- h264QueueSize="+mH264Queue.size());
                                    if (mH264Queue.size() >= 2){
                                        //已经有过多的包了可以发送了
                                        Log.d(TAG, "HardEncode- need send packt");
                                        mSendSemphore.release();
                                    }
                                    VideoFrameModel videoFrameModel = new VideoFrameModel();
                                    videoFrameModel.datas = keyframe;
                                    videoFrameModel.type = VideoFrameModel.keyFrameType;
                                    mH264Queue.put(videoFrameModel);//如果队列已满将租塞当前线程
                                    for (int i = 0;i<5.0;i++){
                                        Log.d(TAG, "HardEncode- 关键帧Five i="+i+" value="+outData[i]);
                                    }

                                    if (mIsWiteH264File){
                                        dumpFile("hardEncoder_"+m_width+m_height+".h264",keyframe);
                                    }

                                }else{
                                    //非关键帧--->此处得到的是P帧

//                                    Log.d(TAG, "HardEncode- h264QueueSize="+mH264Queue.size());
                                    if (mH264Queue.size() >= 2){
                                        //已经有过多的包了可以发送了
                                        Log.d(TAG, "HardEncode- need send packt");
                                        mSendSemphore.release();
                                    }
                                    VideoFrameModel videoFrameModel = new VideoFrameModel();
                                    videoFrameModel.datas = outData;
                                    videoFrameModel.type = VideoFrameModel.bOrPframeType;
                                    mH264Queue.put(videoFrameModel);//队列满了自动阻塞当前线程
                                    for (int i = 0;i<5.0;i++){
                                        Log.d(TAG, "HardEncode- forwordFive i="+i+" value="+outData[i]);
                                    }
                                    if (mIsWiteH264File){
                                        dumpFile("hardEncoder_"+m_width+m_height+".h264",outData);
                                    }
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
//                            Log.d(TAG, "HardEncode- need more yuv. yuvQueue size ="+myuvQueue.size());
                            Thread.sleep(10);//毫秒
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
