package com.example.luoluo.llacamera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class LLCamreaActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView mPreviewView;
    private Handler mHandler;
    private HandlerThread mThreadHandler;
    private Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequest;
    private static final String TAG = "LLCamreaActivity";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);//ROTATION_0-->0 设备方向是0 对应传感器方向是90
        ORIENTATIONS.append(Surface.ROTATION_90, 0);//ROTATION_90-->1
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llcamera);

        initView();
        initLooper();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //很多过程都变成了异步的了，所以这里需要一个子线程的looper
    private void initLooper() {
        mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
    }

    //可以通过TextureView或者SurfaceView
    private void initView() {
        mPreviewView = (TextureView) findViewById(R.id.textureview);
        mPreviewView.setSurfaceTextureListener(this);
    }

private String mCameraId;
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {//1080 1692


        try {
            mCameraId = "1";//默认使用后置摄像头  0-->后置  1---前置
            //获得所有摄像头的管理者CameraManager
            CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
            //获得某个摄像头的特征，支持的参数
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraId);
            //支持的STREAM CONFIGURATION
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //摄像头支持的预览Size数组 1440x1080
            List list =   Arrays.asList(map.getOutputSizes(ImageFormat.YV12));

            Size size4 = new Size(640,480);
            if (list.contains(size4) == false){
                mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];//640x480
            }else {
                mPreviewSize = size4;
            }

            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics characteristic
                        = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristic.get(CameraCharacteristics.LENS_FACING);
                Log.d(TAG, "onSurfaceTextureAvailable: "+cameraId);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.d(TAG, "onSurfaceTextureAvailable: front camera is cameraid="+cameraId);
                    break;
                }
            }
            //打开相机
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //打开此id摄像头并保存相应对象
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public  CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            try {
                mCameraDevice = camera;//保存此摄像头对象
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };


public ImageReader mImageReader;
//public SurfaceView mSurfaceView;//未赋值

    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
//      这里设置的就是预览大小
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                Surface surface = new Surface(texture);
        try {
            // 设置捕获请求为预览，这里还有拍照啊，录像等
            mCaptureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
//      就是在这里，通过这个set(key,value)方法，设置曝光啊，自动聚焦等参数！！ 如下举例：
//        mCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

        //设置读取的图片分辨率
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics
                = manager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //小米不支持NV21  支持YV12
        Size largest = Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.YV12)),//YUV_420_888
                new CompareSizesByArea());
       List list =   Arrays.asList(map.getOutputSizes(ImageFormat.YV12));

       Size size4 = new Size(640,480);
       if (list.contains(size4) == false){
          size4 = largest;
       }
        /*此处还有很多格式，比如我所用到YUV等 最大的图片数， 此处设置的就是输出分辨率mImageReader里能获取到图片数，但是实际中是2+1张图片，就是多一张*/
        mImageReader = ImageReader.newInstance(size4.getWidth(), size4.getHeight(), ImageFormat.YV12,2);
        //监听数据回调
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);

        //设置方向
        //摄像头传感器方向90
        mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        // 获取设备方向
        int rotation = getWindowManager().getDefaultDisplay().getRotation();//0
        // 根据设备方向计算设置摄像头的方向
        mCaptureRequest.set(CaptureRequest.JPEG_ORIENTATION
                , getOrientation(rotation));

        // 这里一定分别add两个surface，一个Textureview的，一个ImageReader的，如果没add，会造成没摄像头预览，或者没有ImageReader的那个回调！！
        mCaptureRequest.addTarget(surface);
        mCaptureRequest.addTarget(mImageReader.getSurface());
        mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),mSessionStateCallback, mHandler);

    }

    private Date mLastDate;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        /**
         *  当有一张图片可用时会回调此方法，但有一点一定要注意：
         *  一定要调用 reader.acquireNextImage()和close()方法，否则画面就会卡住！！！！！我被这个坑坑了好久！！！
         *    很多人可能写Demo就在这里打一个Log，结果卡住了，或者方法不能一直被回调。
         **/
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = reader.acquireNextImage();
            //YUV_420_888格式 ---->获取到的三个通道分别对应YUV (已验证过)
            int width =  image.getWidth();
            int height = image.getHeight();
            // 从image里获取三个plane
            Image.Plane[] planes = image.getPlanes();

/*
            //取出Y数据
            ByteBuffer Ybuffer = image.getPlanes()[0].getBuffer();
            int ysize = Ybuffer.remaining();
            byte[] yData = new byte[width*height];
            Ybuffer.get(yData);

            //拼接两段uv数据
            byte[] uvData = new  byte[width*height/2];
            int uSize = width*height/4;
            int vSize = uSize;

            //取出planes[1]中的数据  此中的数据一定为U
            ByteBuffer uvBuffer1 = image.getPlanes()[1].getBuffer();
            int uvsize1 = uvBuffer1.remaining();//此处大小为width*height/2.0  因为步幅为2
            byte[] uvBuffData1 = new byte[uvsize1];
            uvBuffer1.get(uvBuffData1);
            for (int i=0;i<uSize;i++){
                uvData[i] = uvBuffData1[i*planes[1].getPixelStride()];
            }

            //取出planes[2]中的数据 此中的数据一定为V
            ByteBuffer uvBuffer2 = image.getPlanes()[2].getBuffer();
            int uvsize2 = uvBuffer2.remaining();//此处大小为width*height/2.0  因为步幅为2
            byte[] uvBuffData2 = new byte[uvsize2];
            uvBuffer2.get(uvBuffData2);
            for (int i=0;i<vSize;i++){
                uvData[uSize+i] = uvBuffData2[i*planes[2].getPixelStride()];
            }

            try {
                saveTofile("yuv420_"+width+"_"+height+".yuv",yData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                saveTofile("yuv420_"+width+"_"+height+".yuv",uvData);
            } catch (IOException e) {
                e.printStackTrace();
            }*/


            //将底层数据以I420 YYYYVVUU 方式保存
            byte[] data=  getDataFromImage(image,COLOR_FormatI420);
            //将I420旋转
            byte[] data2 = new byte[width*height*3/2];
            if (mCameraId.equals("0")){
                //后置摄像头
                yuv_rotate_90(data2,data,width,height);
            }else {
                //前置摄像头
                yuv_rotate_270(data2,data,width,height);
            }
            if (mLastDate==null){
                mLastDate = new Date(System.currentTimeMillis());
            }

            //计算这一帧时间和上一帧的时间间隔
            Date currentdate = new Date(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mLastDate);
            long time1 = calendar.getTimeInMillis();

            calendar.setTime(currentdate);
            long time2 = calendar.getTimeInMillis();
            long betweenDays = (time2 - time1)/(1000*3600*24);
            Log.d(TAG, "timeinteval="+betweenDays);

            //编码一帧数据 旋转后高和宽替换
            handleAframeData(data2,height,width);
//            dumpFile("yuv420_"+width+"_"+height+".yuv",data2);
            image.close();

        }
    };

    public native void handleAframeData(byte[] data,int width,int height);

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                updatePreview(session);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mCaptureRequest.build(), null, mHandler);
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    FileOutputStream mFos;
    public  void saveTofile(String fileName, byte[] mbyte) throws IOException {
        File file = new File(getFilesDir(), fileName);
        if(!file.exists()){
            file.createNewFile();
        }

        if (mFos == null){
            mFos  = new FileOutputStream(file);
        }

        mFos.write(mbyte);
//        mFos.close();

    }


    //别人转换写法
    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private static Boolean VERBOSE = true;

    private  boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888://是指yuv420这一系列中的某一个
            case ImageFormat.NV21:
            case ImageFormat.YV12://就是指NV12
                return true;
        }
        return false;
    }

    private  byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }

        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        int totaolLength = ImageFormat.getBitsPerPixel(format);//12
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];//yuv一帧数据
        byte[] rowData = new byte[planes[0].getRowStride()];//一行数据
        if (VERBOSE) Log.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();//1440
            int pixelStride = planes[i].getPixelStride();//Y-->1 U/V-->2
            if (VERBOSE) {
                Log.v(TAG, "pixelStride " + pixelStride);
                Log.v(TAG, "rowStride " + rowStride);
                Log.v(TAG, "width " + width);
                Log.v(TAG, "height " + height);
                Log.v(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;//720
            int h = height >> shift;//540
            int position = rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift);//0
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));//buff位置从0开始
            //一行一行添加像素
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    //存入Y数据
                    length = w;//一行数据的长度
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;//1439 偶数位都是像素 所以只要到长度的最后一个字节即可
                    buffer.get(rowData, 0, length);//保存这一行数据 取了lenght长后 下次取数据的时候将从lenht位置开始
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];//也就是说每隔一个字节才是像素  也就是偶数位置的都是像素
                        channelOffset += outputStride;//下一个像素
                    }
                }
                if (row < h - 1) {
                    int position2 = buffer.position();
                    //下一次取出数据从那个位置开始
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

    //写入文件
    private  void dumpFile(String fileName, byte[] data) {

        File file = new File(getFilesDir(), fileName);
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




    ////用户后置摄像头 将yuv420p旋转90度
    void yuv_rotate_90(byte[] des,byte[] src,int width,int height)
    {
        int n = 0;
        int hw = width / 2;//u 应该看成只有Y的1/4的小长方形
        int hh = height / 2;
        //copy y
        for(int j = 0; j < width;j++)
        {
            for(int i = height - 1; i >= 0; i--)
            {
                des[n++] = src[width * i + j];
            }
        }

        //copy u
        for(int j = 0;j < hw;j++)
        {
            for(int i = hh - 1;i >= 0;i--)
            {

                des[n++] = src[width * height + hw*i + j ];//ptemp[ hw*i + j ];
            }
        }

        //copy v
        for(int j = 0; j < hw; j++)
        {
            for(int i = hh - 1;i >= 0;i--)
            {

                des[n++] = src[width * height + width * height / 4 + hw*i + j];//ptemp[hw*i + j];
            }
        }
    }

    //前置摄像头需要逆时针旋转90即 270顺
    void yuv_rotate_270(byte[] des,byte[] src,int width,int height)
    {
        int n = 0;
        int hw = width / 2;
        int hh = height / 2;
        //copy y
        for(int j = width; j > 0; j--)
        {
            for(int i = 0; i < height;i++)
            {
                des[n++] = src[width*i + j];
            }
        }

        //copy u
        for(int j = hw-1; j >=0;j--)
        {
            for(int i = 0; i < hh;i++)
            {
                des[n++] = src[width * height + hw * i + j]; //ptemp[hw * i + j];
            }
        }

        //copy v
        for(int j = hw-1; j >=0;j--)
        {
            for(int i = 0; i < hh;i++)
            {
                des[n++] = src[width * height + width * height / 4 +hw * i + j];//ptemp[hw * i + j];
            }
        }
    }

    //水平镜像
    void yuv_flip_horizontal(byte[] des,byte[] src,int width,int height)
    {
        int n = 0;
        int hw = width / 2;
        int hh = height / 2;
        //copy y
        for(int j = 0; j < height; j++)
        {
            for(int i = width - 1;i >= 0;i--)
            {
                des[n++] = src[width * j + i];
            }
        }

        //copy u
        for(int j = 0; j < hh; j++)
        {
            for(int i = hw - 1;i >= 0;i--)
            {
                des[n++] = src[width * height + hw * j + i];//ptemp[hw * j + i];
            }
        }

        //copy v
        for(int j = 0; j < hh; j++)
        {
            for(int i = hw - 1;i >= 0;i--)
            {
                des[n++] = src[width*height + width * height / 4 + hw * j + i];//ptemp[hw * j + i];
            }
        }
    }


    }

/*****************8参考资料
 *
 I420: YYYYYYYY UU VV =>YUV420P
 YV12: YYYYYYYY VV UU =>YUV420P
 NV12: YYYYYYYY UVUV =>YUV420SP
 NV21: YYYYYYYY VUVU =>YUV420SP

 //用户后置摄像头
 private byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
 byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
 // Rotate the Y luma ---对输出的数据填充顺序从上到下
 int i = 0;
 for (int x = 0; x < imageWidth; x++) {
 for (int y = imageHeight - 1; y >= 0; y--) {
 yuv[i] = data[y * imageWidth + x];
 i++;
 }
 }


 // Rotate the U and V color components  ---- 填充顺序从下到上
 i = imageWidth * imageHeight * 3 / 2 - 1;//从最后一个数据开始  先把最后一排填满
 for (int x = imageWidth - 1; x > 0; x = x - 2) {
 for (int y = 0; y < imageHeight / 2; y++) {
 yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
 i--;
 yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
 i--;
 }
 }
 return yuv;
 }

 //用于前置摄像头 非镜像
 private byte[] rotateYUVDegree270(byte[] data, int imageWidth, int imageHeight) {
 byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
 // Rotate the Y luma
 int i = 0;
 for (int x = imageWidth - 1; x >= 0; x--) {
 for (int y = 0; y < imageHeight; y++) {
 yuv[i] = data[y * imageWidth + x];
 i++;
 }
 }




 // Rotate the U and V color components
 i = imageWidth * imageHeight;
 for (int x = imageWidth - 1; x > 0; x = x - 2) {
 for (int y = 0; y < imageHeight / 2; y++) {
 yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
 i++;
 yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
 i++;
 }
 }


 return yuv;
 }

 int rotate2YUV420Degree270(byte[] dstyuv,byte[] srcdata, int imageWidth, int imageHeight) {

 int i = 0, j = 0;

 int index = 0;
 int tempindex = 0;
 int div = 0;
 for (i = 0; i < imageHeight; i++) {
 div = i + 1;
 tempindex = 0;
 for (j = 0; j < imageWidth; j++) {

 tempindex += imageWidth;
 dstyuv[index++] = srcdata[tempindex - div];
 }
 }

 int start = imageWidth * imageHeight;
 int udiv = imageWidth * imageHeight / 4;

 int uWidth = imageWidth / 2;
 int uHeight = imageHeight / 2;
 index = start;
 for (i = 0; i < uHeight; i++) {
 div = i + 1;
 tempindex = start;
 for (j = 0; j < uWidth; j++) {
 tempindex += uWidth;
 dstyuv[index] = srcdata[tempindex - div];
 dstyuv[index + udiv] = srcdata[tempindex - div + udiv];
 index++;
 }
 }

 return 0;
 }

Image image = reader.acquireNextImage();
            //YUV_420_888格式 ---->获取到的三个通道分别对应YUV (已验证过)
            int width =  image.getWidth();
            int height = image.getHeight();
            // 从image里获取三个plane
            Image.Plane[] planes = image.getPlanes();


            //取出Y数据
            ByteBuffer Ybuffer = image.getPlanes()[0].getBuffer();
            int ysize = Ybuffer.remaining();
            byte[] yData = new byte[width*height];
            Ybuffer.get(yData);


            //拼接两段uv数据
            byte[] uvData = new  byte[width*height/2];
            int uSize = width*height/4;
            int vSize = uSize;

            //取出planes[1]中的数据
            ByteBuffer uvBuffer1 = image.getPlanes()[1].getBuffer();
            int uvsize1 = uvBuffer1.remaining();//此处大小为width*height/2.0  因为步幅为2
//            byte[] uvData1 = new byte[width*height/4];
            byte[] uvBuffData1 = new byte[uvsize1];
            uvBuffer1.get(uvBuffData1);
            for (int i=0;i<uSize;i++){
//                uvData1[i] = uvBuffData1[i*planes[1].getPixelStride()];
                uvData[i] = uvBuffData1[i*planes[1].getPixelStride()];
            }

            //取出planes[2]中的数据
            ByteBuffer uvBuffer2 = image.getPlanes()[2].getBuffer();
//            byte[] uvData2 = new byte[width*height/4];
            int uvsize2 = uvBuffer2.remaining();//此处大小为width*height/2.0  因为步幅为2
            byte[] uvBuffData2 = new byte[uvsize2];
            uvBuffer2.get(uvBuffData2);
            for (int i=0;i<vSize;i++){
//                uvData2[i] = uvBuffData2[i*planes[2].getPixelStride()];
                uvData[uSize+i] = uvBuffData2[i*planes[2].getPixelStride()];
            }

            try {
                saveTofile("yuv420_"+width+"_"+height+".yuv",yData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                saveTofile("yuv420_"+width+"_"+height+".yuv",uvData);
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.i(TAG,"image format: " +image.getFormat());//35 对应yuv420_888
            Log.i(TAG, "width  " +width );//1440
            Log.i(TAG, "height  " + height);//1080
            for (int i = 0; i < planes.length; i++) {
                ByteBuffer iBuffer = planes[i].getBuffer();
                int iSize = iBuffer.remaining();
                Log.i(TAG, "plane"+i+":"+ "pixelStride  " + planes[i].getPixelStride());//Y --> 1  u/v -->2
                Log.i(TAG, "plane"+i+":"+"rowStride   " + planes[i].getRowStride());//Y -->1440 u/v-->1440
                //行数
                Log.i(TAG, "plane"+i+":"+"buffsize: "+iSize);
                Log.i(TAG, "plane"+i+":"+"rows "+iSize/planes[i].getRowStride());//Y-->1080 u/v-->539(539.99)
            }
            int planesCount = image.getPlanes().length;//3

//单独存储u和v数据

            byte[] uData = new byte[uSize];
            byte[] vData = new  byte[uSize];
            for (int i = 0;i<uSize;i++){
                uData[i] = uvData[i*2];
                if (i< vData.length){
                    vData[i] = uvData[1+i*2];
                }
            }
            try {
                saveTofile("yuv420_"+width+"_"+height+".yuv",vData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                saveTofile("yuv420_"+width+"_"+height+".yuv",uData);
            } catch (IOException e) {
                e.printStackTrace();
            }
 */