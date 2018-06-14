#include <jni.h>
#include <string>
#include <assert.h>
#include <pthread.h>
#include <sys/types.h>
#include <malloc.h>

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>

extern "C"
{
#include "libavformat/avformat.h"
#include "audio_encoder.h"
}

#define TAG "Audio-" // 这个是自定义的LOG的标识
#define LLog(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类

FILE *pcmFile;


//输出混合对象接口 ---实例化后叫(混合对象)
static SLObjectItf outputMixObject = NULL;
//引擎接口
static SLEngineItf engineEngine;
//用来判断是否有播放权限等
static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
        SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

// 文件播放对象
static SLObjectItf fdPlayerObject = NULL;
static SLPlayItf fdPlayerPlay;
static SLSeekItf fdPlayerSeek;
static SLMuteSoloItf fdPlayerMuteSolo;
static SLVolumeItf fdPlayerVolume;

//************录音相关*******
// recorder interfaces
static SLObjectItf recorderObject = NULL;//录音对象
static SLRecordItf recorderRecord;//录音接口
static SLAndroidSimpleBufferQueueItf recorderBufferQueue;//录音队列

//----------录音播放相关
static SLmilliHertz bqPlayerSampleRate = 0;
static jint   bqPlayerBufSize = 0;//录音一个buff的大小
static SLObjectItf bqPlayerObject = NULL; //录音时用来播放的播放器对象
static SLPlayItf bqPlayerPlay;//播放器接口
static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;//声音队列接口

static short *nextBuffer;
static unsigned nextSize;
static int nextCount;

static pthread_mutex_t  audioEngineLock = PTHREAD_MUTEX_INITIALIZER;

// 5 seconds of recorded audio at 16 kHz mono, 16-bit signed little endian
#define RECORDER_FRAMES (16000 * 5)
static short recorderBuffer[RECORDER_FRAMES];
static unsigned recorderSize = 0;

static short *resampleBuf = NULL;
static SLEffectSendItf bqPlayerEffectSend;
static SLVolumeItf bqPlayerVolume;



//*end

//实列化某个接口
SLresult RealizeObjet(SLObjectItf object){
    return (*object)->Realize(object,SL_BOOLEAN_FALSE);
}

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "开始播放";
    return env->NewStringUTF(hello.c_str());
}
void releaseResampleBuf(void) {
    if( 0 == bqPlayerSampleRate) {
        /*
         * we are not using fast path, so we were not creating buffers, nothing to do
         */
        return;
    }

    free(resampleBuf);
    resampleBuf = NULL;
}




// 创建对象接口(类)-->实例化对象--->可以获取接口
extern "C" JNIEXPORT void

JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_createEngine(JNIEnv *env, jclass type) {

    pcmFile = fopen("/data/data/com.example.luoluo.audioplayer/files/LLrecodePcm48Hz1Channel.pcm","wb+");
    if(pcmFile == NULL){
        LLog("open file failture.");
    } else{
        LLog("open file success.");
    }
    SLresult result;
    // TODO
    //1.初始化引擎对象接口
    SLObjectItf engineObject;//调所有音频相关接口必须通过此(对象接口) ---类似于c++中的类
    SLEngineOption engineOptions[] = {{(SLuint32)SL_ENGINEOPTION_THREADSAFE,(SLuint32)SL_BOOLEAN_TRUE}};
    slCreateEngine(&engineObject, sizeof(engineOptions)/ sizeof(SLEngineOption),engineOptions,0,0,0);

    //2.实例化对象接口
    RealizeObjet(engineObject);//既然是接口必须实例化后才可以使用

    //3.获取引擎对象的接口    (引擎接口)

    (*engineObject)->GetInterface(engineObject,SL_IID_ENGINE,&engineEngine);

    //创建混合对象  环境混响非必须
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    //实例化对象接口
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    //打开音频输出设备
    // the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                              &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
        (void)result;
    }

    printf("Audio- create engine complete.");

}
#pragma mark 录音播放
//接收的音频数据放入播放队列回调
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    assert(bq == bqPlayerBufferQueue);
    assert(NULL == context);
    // for streaming playback, replace this test by logic to find and fill the next buffer
    if (--nextCount > 0 && NULL != nextBuffer && 0 != nextSize) {
        SLresult result;
        //将录制并裁剪后的数据放入播放队列
        result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer, nextSize);
        // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
        // which for this code example would indicate a programming error
        if (SL_RESULT_SUCCESS != result) {
            pthread_mutex_unlock(&audioEngineLock);
        }
        (void)result;
    } else {
        releaseResampleBuf();
        pthread_mutex_unlock(&audioEngineLock);
    }
}
//初始化录音播放的相关参数
extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_createBufferQueueAudioPlayer(JNIEnv *env,
                                                                              jclass type,
                                                                              jint sampleRate,
                                                                              jint bufSize) {

    // TODO
    SLresult result;
    if (sampleRate >= 0 && bufSize >= 0 ) {
        bqPlayerSampleRate = sampleRate * 1000;
        /*
         * device native buffer size is another factor to minimize audio latency, not used in this
         * sample: we only play one giant buffer here
         */
        bqPlayerBufSize = bufSize;
    }

    //如果没有设置采样率则使用如下格式播放pcm数据
    //配置录制的声音格式和播放时使用的格式
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    //格式pcm 1通道 8000采样率 采样点保存格式16位
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
    /*
     * Enable Fast Audio when possible:  once we set the same rate to be the native, fast audio path
     * will be triggered
     */
    if(bqPlayerSampleRate) {
        //此处修改采样率
        format_pcm.samplesPerSec = bqPlayerSampleRate;       //sample rate in mili second
    }
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    /*
     * create audio player:
     *     fast audio does not support when SL_IID_EFFECTSEND is required, skip it
     *     for fast audio case
     */
    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_EFFECTSEND,
            /*SL_IID_MUTESOLO,*/};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE,
            /*SL_BOOLEAN_TRUE,*/ };

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc, &audioSnk,
                                                bqPlayerSampleRate? 2 : 3, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // realize the player
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    //创建播放器接口
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    //创建buff池接口
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                             &bqPlayerBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    //设置buff队列接收数据回调
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the effect send interface
    bqPlayerEffectSend = NULL;
    if( 0 == bqPlayerSampleRate) {
        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_EFFECTSEND,
                                                 &bqPlayerEffectSend);
        assert(SL_RESULT_SUCCESS == result);
        (void)result;
    }

#if 0   // mute/solo is not supported for sources that are known to be mono, as this is
    // get the mute/solo interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_MUTESOLO, &bqPlayerMuteSolo);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
#endif

    // 获取音量接口--可用于获取当前音量
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // set the player's state to playing
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
}


#pragma mark 录音
// this callback handler is called every time a buffer finishes recording
void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    assert(bq == recorderBufferQueue);
    assert(NULL == context);
    // for streaming recording, here we would call Enqueue to give recorder the next buffer to fill
    // but instead, this is a one-time buffer so we stop recording
    SLresult result;
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    if (SL_RESULT_SUCCESS == result) {
        recorderSize = RECORDER_FRAMES * sizeof(short);
        LLog("Recoder finish");
    }
    pthread_mutex_unlock(&audioEngineLock);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_createAudioRecorder(JNIEnv *env, jclass type) {

    // TODO
    SLresult result;

    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    //录制的数据 格式pcm 1 通道 采样率16000 样本格式16
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_16,
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioRecorder(engineEngine, &recorderObject, &audioSrc,
                                                  &audioSnk, 1, id, req);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // realize the audio recorder
    result = (*recorderObject)->Realize(recorderObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return JNI_FALSE;
    }

    // get the record interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_RECORD, &recorderRecord);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the buffer queue interface
    result = (*recorderObject)->GetInterface(recorderObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                             &recorderBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // register callback on the buffer queue
    result = (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback,
                                                      NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_startRecording(JNIEnv *env, jclass type) {

    // TODO
    SLresult result;

    if (pthread_mutex_trylock(&audioEngineLock)) {
        return;
    }
    // in case already recording, stop recording and clear buffer queue
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_STOPPED);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
    result = (*recorderBufferQueue)->Clear(recorderBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // the buffer is not valid for playback yet
    recorderSize = 0;

    // enqueue an empty buffer to be filled by the recorder
    // (for streaming recording, we would enqueue at least 2 empty buffers to start things off)
    result = (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recorderBuffer,
                                             RECORDER_FRAMES * sizeof(short));
    // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
    // which for this code example would indicate a programming error
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // start recording
    result = (*recorderRecord)->SetRecordState(recorderRecord, SL_RECORDSTATE_RECORDING);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;
}

/*
 * Only support up-sampling
 */
short* createResampledBuf(uint32_t idx, uint32_t srcRate, unsigned *size) {
    short  *src = NULL;
    short  *workBuf;
    int    upSampleRate;
    int32_t srcSampleCount = 0;

    if(0 == bqPlayerSampleRate) {
        return NULL;
    }
    if(bqPlayerSampleRate % srcRate) {
        /*
         * simple up-sampling, must be divisible
         */
        return NULL;
    }
    upSampleRate = bqPlayerSampleRate / srcRate;

    switch (idx) {
        case 4: // captured frames
            srcSampleCount = recorderSize / sizeof(short);
            src =  recorderBuffer;//取出队列中录制的声音
            break;
        default:
            assert(0);
            return NULL;
    }

    resampleBuf = (short*) malloc((srcSampleCount * upSampleRate) << 1);
    if(resampleBuf == NULL) {
        return resampleBuf;
    }
    workBuf = resampleBuf;
    for(int sample=0; sample < srcSampleCount; sample++) {
        for(int dup = 0; dup  < upSampleRate; dup++) {
            *workBuf++ = src[sample];
        }
    }

    *size = (srcSampleCount * upSampleRate) << 1;     // sample format is 16 bit
    return resampleBuf;
}

#pragma mark 裁剪与插入播放队列
//裁剪录制的音频buff
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_selectClip(JNIEnv *env, jclass type, jint count) {

    // TODO
    if (pthread_mutex_trylock(&audioEngineLock)) {
        // If we could not acquire audio engine lock, reject this request and client should re-try
        return JNI_FALSE;
    }

    //将采样数据保存到本地  16hz 1channels 16bit
    //写入文件
//   size_t writeCount = fwrite(recorderBuffer,1,recorderSize,pcmFile);
//    LLog("write char count =%d needWriteCount=%d",writeCount,recorderSize);




    //创建重采样buff 将录制的声音采样从16kHz转为48kHz
    nextBuffer = createResampledBuf(4, SL_SAMPLINGRATE_16, &nextSize);
    // we recorded at 16 kHz, but are playing buffers at 8 Khz, so do a primitive down-sample
    if(!nextBuffer) {
        unsigned i;
        for (i = 0; i < recorderSize; i += 2 * sizeof(short)) {
            recorderBuffer[i >> 2] = recorderBuffer[i >> 1];
        }
        recorderSize >>= 1;
        nextBuffer = recorderBuffer;
        nextSize = recorderSize;
    }

    //将采样数据保存到本地  48hz 1channels 16bit
    //写入文件
    size_t writeCount = fwrite(nextBuffer,1,nextSize,pcmFile);
    LLog("write char count =%d needWriteCount=%d",writeCount,nextSize);

    nextCount = count;
    if (nextSize > 0) {
        // here we only enqueue one buffer because it is a long clip,
        // but for streaming playback we would typically enqueue at least 2 buffers to start
        //*******test 写成aac文件
        short  *tempBuff = nextBuffer;
        int  count = nextSize/1024;
        LLog("count=%d",count);
        for (int i = 0; i < nextSize/1024; ++i) {
            unsigned  char* poutData = (unsigned char *)malloc(sizeof(unsigned char)*2048);//用来接数据
            //只将1024字节转为aac
            encodePcmData(tempBuff,1024,poutData);
            free(poutData);
            tempBuff= tempBuff+(1024/ sizeof(short));
        }
        //end
        SLresult result;
        //放入播放队列
        result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, nextBuffer, nextSize);
        if (SL_RESULT_SUCCESS != result) {
            pthread_mutex_unlock(&audioEngineLock);
            return JNI_FALSE;
        }
    } else {
        pthread_mutex_unlock(&audioEngineLock);
    }

    return JNI_TRUE;
}

#pragma mark ---

//初始化播放器
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_createAssetAudioPlayer(JNIEnv *env, jclass type,
                                                                        jobject assetManager,
                                                                        jstring filename_) {
    SLresult result;
    const char *filename = env->GetStringUTFChars(filename_, 0);

    // TODO
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    //获取资源文件
    AAsset* asset = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
    if (NULL == asset) {
        printf("Audio- get audio asset file failtue!");
        return JNI_FALSE;
    }
    //释放c字符串
    env->ReleaseStringUTFChars(filename_, filename);
    //打开资源文件
    off_t start, length;
    int fd = AAsset_openFileDescriptor(asset, &start, &length);
    assert(0 <= fd);
    AAsset_close(asset);

    // 配置播放的类型 mime 为不确定的任意格式
    SLDataLocator_AndroidFD loc_fd = {SL_DATALOCATOR_ANDROIDFD, fd, start, length};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_fd, &format_mime};
//    SLDataFormat_PCM --此类型可指定pcm格式

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &fdPlayerObject, &audioSrc, &audioSnk,
                                                3, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // realize the player
    result = (*fdPlayerObject)->Realize(fdPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the play interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_PLAY, &fdPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the seek interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_SEEK, &fdPlayerSeek);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the mute/solo interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_MUTESOLO, &fdPlayerMuteSolo);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the volume interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_VOLUME, &fdPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // enable whole file looping
    result = (*fdPlayerSeek)->SetLoop(fdPlayerSeek, SL_BOOLEAN_TRUE, 0, SL_TIME_UNKNOWN);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_setPlayingAssetAudioPlayer(JNIEnv *env,
                                                                            jclass type,
                                                                            jboolean isPlaying) {

    // TODO
    SLresult result;

    // make sure the asset audio player was created
    if (NULL != fdPlayerPlay) {

        // 开始播放
        result = (*fdPlayerPlay)->SetPlayState(fdPlayerPlay, isPlaying ?
                                                             SL_PLAYSTATE_PLAYING : SL_PLAYSTATE_PAUSED);
        assert(SL_RESULT_SUCCESS == result);
        (void)result;

        __android_log_print(ANDROID_LOG_INFO, "JNI", "%s resultstr=%d","Audio-",isPlaying);

    }
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_shutDown(JNIEnv *env, jobject instance) {

    // TODO

}



extern "C"
JNIEXPORT void JNICALL
Java_com_example_luoluo_audioplayer_MainActivity_initffmpeg(JNIEnv *env, jclass type) {

    // TODO
    initFfmpegAll();
}