//
// Created by luo luo on 08/06/2018.
//

#ifndef AUDIOPLAYER_AUDIO_ENCODER_H
#define AUDIOPLAYER_AUDIO_ENCODER_H
//初始化编码器和编码器上下文
void initFfmpegAll(void);

int encodePcmData(short *pcmdata , unsigned int frameSize, unsigned char *pOut);
#endif //AUDIOPLAYER_AUDIO_ENCODER_H
