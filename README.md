# AndroidDemo
简单安卓项目搭建
AudioPlayer这个项目需要替换变量值：替换文件CMakeLists.text中的set(ARM_DIR /Users/luoluo/AndroidStudioProjects/Github/AudioPlayer/app/src/main/jniLibs)
为你自己项目的jniLibs文件夹的路径。
#LLACamra 介绍:
#android Camera2使用
前言:由于有关camera2使用和对数据处理的比较少所以笔者也有着乐于助人心所以有了后面的内容。咋们废话不多说先把流程和目的说下。首先是获取到相关摄像头id、然后打开摄像、接收摄像头数据回调、将y、u、v拼接成完整的yuv、对数据进行旋转生成正常用户看到的画面，以及对yuv数据编码为h264数据。
##Camera2 API介绍


####1.获取前或者后置摄像头 摄像头都有对应的摄像头id、获取到摄像头id后面有相应的接口打开此id
```java
CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics characteristic
                        = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristic.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.d(TAG, "onSurfaceTextureAvailable: front camera is cameraid="+cameraId);
                    break;
                }
            }

```

####2.打开摄像头以及打开状态回调
```java
cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
//回调
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
```

####3.设置分辨率 可根据你显示的view大小进行设置、设置回调数据格式、以及添加预览和回调数据监听
```java
 SurfaceTexture texture = mPreviewView.getSurfaceTexture();
//      这里设置的就是预览大小
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                Surface surface = new Surface(texture);
                

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
        // 这里一定分别add两个surface，一个Textureview的，一个ImageReader的，如果没add，会造成没摄像头预览，或者没有ImageReader的那个回调！！
        mCaptureRequest.addTarget(surface);
        mCaptureRequest.addTarget(mImageReader.getSurface());
```

##分析回调数据yuv数据和存储为yuv420p
####格式讲解
	I420:YYYYYYYY UU VV =>YUV420P
 	YV12: YYYYYYYY VV UU =>YUV420P
 	NV12: YYYYYYYY UVUV =>YUV420SP
 	NV21: YYYYYYYY VUVU =>YUV420SP
###数据按y、u、v分开获取与存储、由于之前选择的格式有可能为yuv420_888、NV21、YV12但是经过如下取出后一定是yuv420p
####方法一
```java
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
            }
```
####方法二
```java
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
```
####目前看到的效果为默认的横屏数据:
![HorizontalCamera2](https://github.com/ChangeStrong/AndroidDemo/blob/master/Camera2Horizontal.png?raw=true)


##前后摄像头数据旋转
前置摄像头需要旋转顺时针270度 后置摄像头只需旋转90度、旋转算法如下

```java
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

```

####旋转后的效果为竖屏数据:
![PortraitCamera2](https://github.com/ChangeStrong/AndroidDemo/blob/master/Camera2Portrait.png?raw=true)
###传送门:
[源码](https://github.com/ChangeStrong/AndroidDemo)
下载后的LLACamera有相关代码

####目前正在持续更新中有喜欢的小伙伴可以fork一下、顺便给个star谢谢、你的赞赏是我持续的动力。
有疑问的小伙伴欢迎加交流讨论QQ：206931384 

###有钱的小伙伴们走如下通道：
![wechat](https://github.com/ChangeStrong/AndroidDemo/blob/master/wechatPay2.jpg?raw=true)
