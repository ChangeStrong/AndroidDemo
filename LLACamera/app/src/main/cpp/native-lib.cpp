#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdio.h>

#define LLIsWriteFile 1
#define is_start_code(code)	(((code) & 0x0ffffff) == 0x01)
extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/opt.h"
#include "libavutil/imgutils.h"
#include "libavutil/time.h"
#include "libavutil/mathematics.h"
}
#define TAG "Audio-" // 这个是自定义的LOG的标识
#define LLog(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类

static AVPixelFormat mDefaultPixelFormat;
static  AVCodecContext * mVideoCodecContext;
static  AVCodecID  mDefaultVideoCodecId;
static AVFormatContext *mOutFormatctx;
static  AVStream *mOutVideoStream;
static  char *mPushUrl;
FILE *mFph264;
int64_t mFirstTime;


// sps and pps data
uint8_t *headerData;
int headerSize;
//将sps和pps单独获取出来
void parseH264SequenceHeader(uint8_t* in_pBuffer, uint32_t in_ui32Size,
                             uint8_t** inout_pBufferSPS, int& inout_ui32SizeSPS,
                             uint8_t** inout_pBufferPPS, int& inout_ui32SizePPS);

static int XError(int errNum)
{
    char buf[1024] = { 0 };
    av_strerror(errNum, buf, sizeof(buf));
    LLog("Camera2-  ffmpeg- error:%s",buf);
    getchar();
    return -1;
}

void initVideoEncoder(void){
//  AVCodec *codec2 = avcodec_find_encoder_by_name("h264_mediacodec");
//    if(codec2 == NULL){
//        LLog("Video- Not find mediacodec h264!!!");
//    }
    //avcodec_find_decoder_by_name("h264_mediacodec")
    AVCodec *codec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (codec == NULL) {
        LLog("Video- Not get codec h264!!!");
        return;
    }
    //设置编码器上下文 码率 宽、高、时基、帧率
    mVideoCodecContext = avcodec_alloc_context3(codec);
    mVideoCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    mVideoCodecContext->codec_id = mDefaultVideoCodecId;
//    _videoCodecContext->bit_rate = self.videoParam.bitrate;//400000
    mVideoCodecContext->width = 480;
    mVideoCodecContext->height = 640;
    mVideoCodecContext->time_base = (AVRational){1,25};
    mVideoCodecContext->framerate = (AVRational){25,1};
    mVideoCodecContext->gop_size = 20;//硬编码后默认就是20帧为一个I帧循环  如果为0编码出来的pact的dts和pts值会相等
    mVideoCodecContext->max_b_frames = 0;
    mVideoCodecContext->pix_fmt = AV_PIX_FMT_NV12;

    AVDictionary *param = 0;
    int ret = avcodec_open2(mVideoCodecContext, codec, &param);
    if ( ret < 0) {
        LLog("Video- open codec failture.!!!");
        XError(ret);
        return;
    }

    mOutVideoStream = avformat_new_stream(mOutFormatctx, codec);
    mOutVideoStream->time_base = mVideoCodecContext->time_base;

    //将视频的分辨率、码率、像素格式、编码格式复制到流信息里面
    ret = avcodec_parameters_from_context(mOutVideoStream->codecpar, mVideoCodecContext);
//    _outVideoSream->codec->codec_tag = 0;
    mOutVideoStream->codecpar->codec_tag = 0;
    if (ret < 0) {
        LLog("Video- copy codec context to stream parameter.failture!!!");
        return;
    }

    //创建AVIOContext 打开rtmp网络输出IO 且已绑定到AVFormatContext
    ret = avio_open(&mOutFormatctx->pb,mPushUrl , AVIO_FLAG_WRITE);
    if (!mOutFormatctx->pb)
    {
        XError(ret);
        return;
    }

//    ret = avformat_write_header(mOutFormatctx, 0);//经过这句之后steam0 的timebase为1/1000
//    if (ret < 0)
//    {
//        XError(ret);
//        return ;
//    }

    LLog("Camera2- init ffmpeg finish");

}

void initffmpeg(void){
    mFph264=fopen("/data/data/com.example.luoluo.llacamera/files/output_480x640.h264","wb+");
    av_register_all();
    avcodec_register_all();

    avformat_network_init();
    //创建输出封装上下文
    mOutFormatctx = NULL;
//    mPushUrl = "rtmp://192.168.1.202:1935/mytv/room";
//   mPushUrl = "/data/data/com.example.luoluo.llacamera/files/output_480x640.flv";
    LLog("Camera2- pushUrl=%s",mPushUrl);
    mFirstTime = 0;
    int ret = avformat_alloc_output_context2(&mOutFormatctx, 0, "flv", mPushUrl);
    if (!mOutFormatctx)
    {
        XError(ret);
        return;
    }
    //初始化视频编码器和编码器上下文
     initVideoEncoder();

    av_dump_format(mOutFormatctx, 0, mPushUrl, 1);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_llacamera_MainActivity_initFFmpegUrl(JNIEnv *env, jobject instance,
                                                             jstring pushUrl_, jint size) {
    const char *pushUrl = env->GetStringUTFChars(pushUrl_, 0);

    // TODO
    mDefaultPixelFormat = AV_PIX_FMT_YUV420P;
    mPushUrl = (char *)malloc(sizeof(char)*size);
    memcpy(mPushUrl,pushUrl,size);
    initffmpeg();
    env->ReleaseStringUTFChars(pushUrl_, pushUrl);
}


extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_luoluo_llacamera_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";


    return env->NewStringUTF(hello.c_str());
}


//找到开始码00 00 00 01
uint32_t findStartCode(uint8_t* in_pBuffer, uint32_t in_ui32BufferSize,
                                               uint32_t in_ui32Code, uint32_t& out_ui32ProcessedBytes) {
    uint32_t ui32Code = in_ui32Code;//

    const uint8_t * ptr = in_pBuffer;
    //一个一个的找 直到找到起始码的位置
    while (ptr < in_pBuffer + in_ui32BufferSize) {
        ui32Code = *ptr++ + (ui32Code << 8);// 初始值为0xff 左移动8位后为0000ff00  到第四个字节会变为00000000
        if (is_start_code(ui32Code))
            break;
    }

    out_ui32ProcessedBytes = (uint32_t)(ptr - in_pBuffer);//找到一帧信息后记录占的字节数

    return ui32Code;
}

//将sps和pps单独获取出来
void parseH264SequenceHeader(uint8_t* in_pBuffer, uint32_t in_ui32Size,
                                                     uint8_t** inout_pBufferSPS, int& inout_ui32SizeSPS,
                                                     uint8_t** inout_pBufferPPS, int& inout_ui32SizePPS) {
    uint32_t ui32StartCode = 0x0ff;

    uint8_t* pBuffer = in_pBuffer;
    uint32_t ui32BufferSize = in_ui32Size;

    uint32_t sps = 0;
    uint32_t pps = 0;

    uint32_t idr = in_ui32Size;

    do {
        uint32_t ui32ProcessedBytes = 0;
        ui32StartCode = findStartCode(pBuffer, ui32BufferSize, ui32StartCode,
                                      ui32ProcessedBytes);//返回 0x01
        pBuffer += ui32ProcessedBytes;
        ui32BufferSize -= ui32ProcessedBytes;

        if (ui32BufferSize < 1)
            break;

        uint8_t val = (*pBuffer & 0x1f);

        if (val == 5)
            idr = pps + ui32ProcessedBytes - 4;

        if (val == 7)
            sps = ui32ProcessedBytes;

        if (val == 8)
            pps = sps + ui32ProcessedBytes;

    } while (ui32BufferSize > 0);

    *inout_pBufferSPS = in_pBuffer + sps - 4;
    inout_ui32SizeSPS = pps - sps;

    *inout_pBufferPPS = in_pBuffer + pps - 4;
    inout_ui32SizePPS = idr - pps + 4;
}

void pushStop(){
    free(headerData);
}

//获取时间戳
static double r2d(AVRational r)
{
    return r.num == 0 || r.den == 0 ? 0. : (double)r.num / (double)r.den;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_llacamera_LLAHardEncode_sendPactedToNetwork(JNIEnv *env, jobject instance,
                                                                    jbyteArray packByte_,
                                                                    jint lenght, jint flags,
                                                                    jlong packtCounts) {
    jbyte *packByte = env->GetByteArrayElements(packByte_, NULL);

    // TODO
    if (mFirstTime == 0){
        mFirstTime = av_gettime();
    }
    uint8_t *packet = (unsigned char *)malloc(lenght);
    memcpy(packet,packByte,lenght);
    if (flags ==2){
        //sps和pps信息
        LLog("Camera2- get sps pps info.");
        headerSize = lenght;
        headerData = new uint8_t[lenght];
        memcpy(headerData, packByte, lenght);
        uint8_t* spsFrame = 0;
        uint8_t* ppsFrame = 0;

        int spsFrameLen = 0;
        int ppsFrameLen = 0;

        parseH264SequenceHeader(headerData, headerSize, &spsFrame, spsFrameLen,
                                &ppsFrame, ppsFrameLen);

        // Extradata contains PPS & SPS for AVCC format
        int extradata_len = 8 + spsFrameLen - 4 + 1 + 2 + ppsFrameLen - 4;
        mVideoCodecContext->extradata = (uint8_t*) av_mallocz(extradata_len);
        mVideoCodecContext->extradata_size = extradata_len;
        mVideoCodecContext->extradata[0] = 0x01;
        mVideoCodecContext->extradata[1] = spsFrame[4 + 1];
        mVideoCodecContext->extradata[2] = spsFrame[4 + 2];
        mVideoCodecContext->extradata[3] = spsFrame[4 + 3];
        mVideoCodecContext->extradata[4] = 0xFC | 3;
        mVideoCodecContext->extradata[5] = 0xE0 | 1;
        int tmp = spsFrameLen - 4;
        mVideoCodecContext->extradata[6] = (tmp >> 8) & 0x00ff;
        mVideoCodecContext->extradata[7] = tmp & 0x00ff;
        int i = 0;
        for (i = 0; i < tmp; i++)
            mVideoCodecContext->extradata[8 + i] = spsFrame[4 + i];
        mVideoCodecContext->extradata[8 + tmp] = 0x01;
        int tmp2 = ppsFrameLen - 4;
        mVideoCodecContext->extradata[8 + tmp + 1] = (tmp2 >> 8) & 0x00ff;
        mVideoCodecContext->extradata[8 + tmp + 2] = tmp2 & 0x00ff;
        for (i = 0; i < tmp2; i++)
            mVideoCodecContext->extradata[8 + tmp + 3 + i] = ppsFrame[4 + i];

        int ret = avformat_write_header(mOutFormatctx, NULL);
        if (ret < 0) {

            LLog("Error occurred when opening output file: %s\n", av_err2str(ret));
        } else{
            LLog("Camera2- wirite head success.");
//            isWriteHeaderSuccess = true;
        }

    } else{
        AVPacket pkt ;
        av_init_packet(&pkt);
        pkt.size = lenght;
        pkt.data = packet;

        //修改每帧的起始码(0001)为这个包的数据大小--(即：除去起始码的大小)  ---此大小必须为大端模式
//        int length2 = lenght;
//        if(pkt.data[0] == 0x00 && pkt.data[1] == 0x00 &&
//           pkt.data[2] == 0x00 && pkt.data[3] == 0x01){
//            length2 -= 4;
//            pkt.data[0] = ((length2 ) >> 24) & 0x00ff;
//            pkt.data[1] = ((length2 ) >> 16) & 0x00ff;
//            pkt.data[2] = ((length2 ) >> 8) & 0x00ff;
//            pkt.data[3] = ((length2 )) & 0x00ff;
//        }
        //end
        pkt.pos = -1;//表示不知道此包在流中的索引位置
        pkt.stream_index = 0;
        AVRational itime = mVideoCodecContext->time_base;
        AVRational otime = mOutFormatctx->streams[pkt.stream_index]->time_base;

        pkt.pts = av_rescale_q_rnd(packtCounts, itime, otime, AV_ROUND_NEAR_INF);
        pkt.dts = pkt.pts;
        pkt.duration = av_rescale_q_rnd(1, itime, otime, AV_ROUND_NEAR_INF);

        pkt.flags = flags == 1? AV_PKT_FLAG_KEY:0;//1代表关键帧 0代表b或者p帧
        mVideoCodecContext->frame_number++;
        LLog("Camera2- ffmpeg pts= %lld duration=%lld packtCounts=%lld packetSize=%d",pkt.pts,pkt.duration,packtCounts,pkt.size);
        int64_t  currentTime = av_gettime();
        int64_t intevalTime = currentTime - mFirstTime;
        int64_t  ptsTime = pkt.dts*r2d(otime)*(1000*1000);
        if (ptsTime > intevalTime){
            av_usleep(ptsTime-intevalTime);//微妙
        }
//        fwrite(pkt.data,1,lenght,mFph264);
        int  ret = av_interleaved_write_frame(mOutFormatctx, &pkt);
        if (ret != 0){
            XError(ret);
        }

        av_packet_unref(&pkt);

    }
    env->ReleaseByteArrayElements(packByte_, packByte, 0);
}

