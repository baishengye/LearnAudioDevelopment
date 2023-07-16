package com.baishengye.libaudio.recorder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 音频数据包装器
 */
public interface AudioChunk {
    // 获取最大峰值(振幅)
    double maxAmplitude();

    // 获取byte类型数据
    byte[] toBytes();

    // 获取short类型数据
    short[] toShorts();


    abstract class AbstractAudioChunk implements AudioChunk {
        private static final double REFERENCE = 0.6;

        @Override
        public double maxAmplitude() {
            int nMaxAmp = 0;
            for (short sh : toShorts()) {
                if (sh > nMaxAmp) {
                    nMaxAmp = sh;
                }
            }
            if (nMaxAmp > 0) {
                return Math.abs(20 * Math.log10(nMaxAmp / REFERENCE));
            } else {
                return 0;
            }
        }
    }

    /**
     * byte类型数据包装器
     */
    class Bytes extends AbstractAudioChunk {
        private final byte[] bytes;

        Bytes(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public byte[] toBytes() {
            return bytes;
        }

        @Override
        public short[] toShorts() {
            short[] shorts = new short[bytes.length / 2];
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            return shorts;
        }
    }
}
