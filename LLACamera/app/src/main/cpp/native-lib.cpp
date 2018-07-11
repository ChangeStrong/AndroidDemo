#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdio.h>

#define LLIsWriteFile 1

extern "C"
{
#include "libavformat/avformat.h"
}
#define TAG "Audio-" // 这个是自定义的LOG的标识
#define LLog(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类

static AVPixelFormat mDefaultPixelFormat;
static  int  mCurrentFrameCount;
static  AVCodecContext * mVideoCodecContext;
static  AVCodecID  mDefaultVideoCodecId;
static AVFormatContext *mOutFormatctx;
FILE *mFph264;

static int XError(int errNum)
{
    char buf[1024] = { 0 };
    av_strerror(errNum, buf, sizeof(buf));
    LLog("error:%s",buf);
    getchar();
    return -1;
}

void initVideoEncoder(void){
    AVCodec *codec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (codec == NULL) {
        LLog("Video- Not get codec h264!!!");
        return;
    }
    //设置编码器上下文 码率 宽、高、时基、帧率
    AVCodecContext * mVideoCodecContext = avcodec_alloc_context3(codec);
    mVideoCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    mVideoCodecContext->codec_id = mDefaultVideoCodecId;
//    _videoCodecContext->bit_rate = self.videoParam.bitrate;//400000
    mVideoCodecContext->width = 480;
    mVideoCodecContext->height = 640;
    mVideoCodecContext->time_base = (AVRational){1,20};
    mVideoCodecContext->framerate = (AVRational){20,1};
    mVideoCodecContext->gop_size = 10;//10帧为一个I帧循环  如果为0编码出来的pact的dts和pts值会相等
    mVideoCodecContext->max_b_frames = 3;
}

void initffmpeg(void){

    mFph264=fopen("/data/data/com.example.luoluo.audioplayer/files/output_480x640.x264","wb+");
    av_register_all();
    avcodec_register_all();

    avformat_network_init();
    //创建输出封装上下文
    mOutFormatctx = NULL;
    char *pushurl = "/data/data/com.example.luoluo.audioplayer/files/output_480x640.flv";
    int ret = avformat_alloc_output_context2(&mOutFormatctx, 0, "flv", pushurl);
    if (!mOutFormatctx)
    {
        XError(ret);
        return;
    }
    //初始化视频编码器和编码器上下文
     initVideoEncoder();

    av_dump_format(mOutFormatctx, 0, pushurl, 1);
}

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_luoluo_llacamera_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    mDefaultPixelFormat = AV_PIX_FMT_YUV420P;
    mCurrentFrameCount = 0;
    initffmpeg();

    return env->NewStringUTF(hello.c_str());
}



AVPacket  encodeAVFrame(AVFrame *frame){

    AVPacket pkt ;
    av_init_packet(&pkt);

    int ret=  avcodec_send_frame(mVideoCodecContext, frame);
    if (ret < 0) {
        LLog("Video- encode frame failture.");
        XError(ret);
    }
    ret = avcodec_receive_packet(mVideoCodecContext, &pkt);//此处会对 frame释放
    if (ret < 0 ) {
        LLog("Video- get packet failture.!");
        XError(ret);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_llacamera_LLCamreaActivity_handleAframeData(JNIEnv *env, jobject instance,
                                                                    jbyteArray data_, jint width,
                                                                    jint height) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    // TODO
    AVFrame *frame =  av_frame_alloc();
    frame->format= mDefaultPixelFormat;//像素格式
    frame->width = (int)width;
    frame->height = (int)height;
    int ret = av_frame_get_buffer(frame, 32);//如果linesize为空会进行赋值
    if (ret < 0) {
        LLog("Video- Could not allocate the video frame data.");
        XError(ret);
        return;
    }
    //确保此帧可以写数据
    ret = av_frame_make_writable(frame);
    if (ret < 0) {
        LLog("Video- this frame is trouble,cann't using.");
        XError(ret);
        return;
    }

    size_t y_size = width*height;
    unsigned char *bufY = (unsigned char *)malloc(width*height);
    memcpy(bufY, data, y_size);
    size_t u_size = y_size/4;
    unsigned char *bufU = (unsigned char *)malloc(u_size);
    memcpy(bufU, data+y_size, u_size);

    size_t v_size = y_size/4;
    unsigned char *bufV = (unsigned char *)malloc(v_size);
    memcpy(bufV, data+y_size*5/4, v_size);

    frame->data[0] =  bufY;
    frame->data[1] = bufU;
    frame->data[2] = bufV;
    frame->linesize[0] =  (int)width;
    frame->linesize[1] = (int)(width/2.0);//u和v都是隔行扫描 占用的高度也是一半
    frame->linesize[2] = (int)(width/2.0);
    frame->pts = mCurrentFrameCount;
    mCurrentFrameCount ++;
    AVPacket packet = encodeAVFrame(frame);
    if(packet.size > 0){
        if (LLIsWriteFile) {
            fwrite(packet.data, 1, packet.size, mFph264);
        }
    }

//  av_interleaved_write_frame(mOutFormatctx, &packet);

    free(frame->data[0]);
    free(frame->data[1]);
    free(frame->data[2]);
    av_frame_free(&frame);
    av_packet_unref(&packet);

    env->ReleaseByteArrayElements(data_, data, 0);
}

