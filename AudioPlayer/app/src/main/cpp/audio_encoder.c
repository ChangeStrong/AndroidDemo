//
// Created by luo luo on 08/06/2018.
//

#include "audio_encoder.h"
#include <stdio.h>
#include <jni.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include <android/log.h>

#define TAG "Encode-" // 这个是自定义的LOG的标识
#define LLog(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类
//aac头部长度
#define ADTS_HEADER_LENGTH 7

static  AVCodecContext *mAudioCodecContext;
 AVFrame *mAVFrame;
uint8_t* mEncoderData;
FILE *maacFile;

void addADTSheader(uint8_t * in, int packet_size);

static int XError(int errNum)
{
    char buf[1024] = { 0 };
    av_strerror(errNum, buf, sizeof(buf));
    __android_log_print(ANDROID_LOG_ERROR,"Encode-","error:%s",buf);
    getchar();
    return -1;
}

void initFfmpegAll(void){

    maacFile = fopen("/data/data/com.example.luoluo.audioplayer/files/LLrecodeAAc.aac","wb+");
    if(maacFile == NULL){
        LLog("open file failture.");
    }
    av_register_all();
    avcodec_register_all();
    AVCodec *codec = avcodec_find_encoder(AV_CODEC_ID_AAC);
    if(codec == NULL){
        __android_log_print(ANDROID_LOG_INFO,"Encode-","Not find aac encoder.");
//        printf("");
        return;
    }
    //初始化编码器上下文
    mAudioCodecContext= avcodec_alloc_context3(codec);
    mAudioCodecContext->codec_type = AVMEDIA_TYPE_AUDIO;
    mAudioCodecContext->bit_rate = 32000;//12Kbit/s  22Kbits 32Kbit
    //android 录制的时候声音默认格式就是16位存储
    mAudioCodecContext->sample_fmt = AV_SAMPLE_FMT_FLTP;//float planar(存完一个声道再存另一个)
    mAudioCodecContext->sample_rate = 48000;
    mAudioCodecContext->channel_layout   = AV_CH_LAYOUT_MONO;
    int  channels = av_get_channel_layout_nb_channels(mAudioCodecContext->channel_layout);
    mAudioCodecContext->channels         = channels;
    __android_log_print(ANDROID_LOG_INFO,"Encode-","encode channels is %d",channels);

    //打开编码器
    int  ret = avcodec_open2(mAudioCodecContext,codec,NULL);
    if (ret < 0){
        __android_log_print(ANDROID_LOG_INFO,"Encode-","open codec failture.!");
        XError(ret);
    }

    mAVFrame = av_frame_alloc();

    mAVFrame->nb_samples = mAudioCodecContext->frame_size;
    mAVFrame->format = mAudioCodecContext->sample_fmt;
    mAVFrame->channel_layout = mAudioCodecContext->channel_layout;
    //获取一帧需要存放的音频大小
    int  mAudioFrameBuffsize = av_samples_get_buffer_size(NULL
            , mAudioCodecContext->channels
            , mAudioCodecContext->frame_size
            , mAudioCodecContext->sample_fmt, 0);

     mEncoderData = av_malloc(mAudioFrameBuffsize);

    //帮音频帧内部data和linsize数组赋值
    avcodec_fill_audio_frame(mAVFrame, mAudioCodecContext->channels, mAudioCodecContext->sample_fmt
            , (const uint8_t*)mEncoderData, mAudioFrameBuffsize, 0);

    //linesize 4096=1024*4  4代表float类型4个字节
        LLog("frmaeSize=%d linesize=%d",mAudioCodecContext->frame_size,mAVFrame->linesize[0]);//此处默认为1024字节

}

//copy内存并 转float类型
static  void short2float(short* in, void* out, int len){
    register int i;
    for(i = 0; i < len; i++)
        //short 类型转float类型
        ((float*)out)[i] = ((float)(in[i])) / 32767.0;
}

//传一帧数据大小的数据过来编码 1024
int encodePcmData(short *pcmdata , unsigned int frameSize, unsigned char *pOut){

    int  encode_ret = -1;
    int  got_packet_ptr = 0;
    AVPacket pkt;
    av_init_packet(&pkt);
    pkt.data = NULL;
    pkt.size = 0;

    if(mAudioCodecContext && mAVFrame){
        //copy一帧数据到帧buff中  对于short类型的pcm数据 一帧的尺寸的长度为1024字节 用short类型存储长度只需1024/2
        short2float((int16_t *)pcmdata, mEncoderData, frameSize/2);

        mAVFrame->data[0] = mEncoderData;
        mAVFrame->pts = 0;
        //音频编码
        encode_ret = avcodec_encode_audio2(mAudioCodecContext, &pkt, mAVFrame, &got_packet_ptr);
        if(encode_ret < 0){
            LLog("Failed to encode!\n");
            return encode_ret;
        }
        if(pkt.size > 0){

            int length = pkt.size + ADTS_HEADER_LENGTH;
            void *adts = malloc(ADTS_HEADER_LENGTH);
            //添加adts header 可以正常播放。
            addADTSheader((uint8_t *)adts, pkt.size+ADTS_HEADER_LENGTH);
//            LLog("header ---- =%s",adts);
            memcpy(pOut,adts,  ADTS_HEADER_LENGTH);
            free(adts);

            memcpy(pOut+ADTS_HEADER_LENGTH,pkt.data,pkt.size);
            LLog("wirite data length=%d",pkt.size+ADTS_HEADER_LENGTH);
            //写入文件
            fwrite(pOut,1,pkt.size+ADTS_HEADER_LENGTH,maacFile);

            av_free_packet(&pkt);
            return length;
        }
        av_free_packet(&pkt);
        return 0;
    }

}
//11111111 11111001 01010000 10000000 00010111 00111111 11111100
//  FF      F9       50       80        17        3F       FC

//11111111 11111001 01011100 01000000 00001011 00111111 11111100
//  FF      F9       4C       40        b       7F(数据的长) FC
void addADTSheader(uint8_t * in, int packet_size){
    int sampling_frequency_index = 3; //采样率下标 11 = 8000Hz 3=48000Hz 8=16000Hz
    int channel_configuration = mAudioCodecContext->channels; //声道数
    in[0] = 0xFF;//写死的代表一帧的开始 syncword
    in[1] = 0xF9;//此处9代表好几个值ID=1 Layer = 0 protection_absent=1
    in[2] = 0x40 | (sampling_frequency_index << 2) | (channel_configuration >> 2);//0x4c  0x6c;
    in[3] = (channel_configuration & 0x3) << 6;//0x40
    in[3] |= (packet_size & 0x1800) >> 11;
    in[4] = (packet_size & 0x1FF8) >> 3;
    in[5] = ((((unsigned char)packet_size) & 0x07) << 5) | (0xff >> 3);
    in[6] = 0xFC;

LLog("11=%x 22=%x 33=%x 44=%x 55=%x 66=%x",in[1],in[2],in[3],in[4],in[5],in[6]);

}

//aac头范围可以7-9字节