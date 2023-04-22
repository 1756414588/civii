/**
 *
 */
package com.game.util;

import java.nio.ByteBuffer;

/**
 * 加密/解密工具类
 *
 * @author wk.dai
 */
public class EncryptionAndDecryptionUtil {
    /**
     * 默认提供的自定义解密方式使用的密钥
     */
    private static final int[] DEFAULT_CUSTOME_DECRYPTION_KEYS = new int[]{
            0xae, 0xbf, 0x56, 0x78, 0xab, 0xcd, 0xef, 0xf1};
    /**
     * 默认提供的自定义加密方式使用的密钥
     */
    private static final int[] DEFAULT_CUSTOME_ENCRYPTION_KEYS = new int[]{
            0xae, 0xbf, 0x56, 0x78, 0xab, 0xcd, 0xef, 0xf1};

    /**
     * 获取自定义默认解密密钥
     *
     * @return 默认解密密钥
     */
    public static int[] getDefaultCustomDecryptionKeys() {
        int[] keys = new int[DEFAULT_CUSTOME_DECRYPTION_KEYS.length];
        System.arraycopy(DEFAULT_CUSTOME_DECRYPTION_KEYS, 0, keys, 0,
                keys.length);
        return keys;
    }

    /**
     * 获取自定义默认加密密钥
     *
     * @return 默认加密密钥
     */
    public static int[] getDefaultCustomEncryptionKeys() {
        int[] keys = new int[DEFAULT_CUSTOME_ENCRYPTION_KEYS.length];
        System.arraycopy(DEFAULT_CUSTOME_ENCRYPTION_KEYS, 0, keys, 0,
                keys.length);
        return keys;
    }

    /**
     * 解密
     *
     * @param data       加密数据
     * @param decryptKey 密钥
     * @return 解密后数据
     * @throws Exception
     */
    public static byte[] decryptCustom(byte[] data, int[] decryptKey)
            throws Exception {
        return decryptCustom(data, 0, data.length, decryptKey);
    }

    /**
     * 解密
     *
     * @param data       数据
     * @param startIndex 加密数据起始索引
     * @param length     数据长度
     * @param decryptKey 密钥
     * @return 解密后数据
     * @throws Exception
     */
    public static byte[] decryptCustom(byte[] data, int startIndex, int length,
                                       int[] decryptKey) throws Exception {
        if (length == 0)
            return data;

        if (decryptKey.length < 8)
            throw new Exception(
                    "The decryptKey must be 64bits length!");

        int lastCipherByte = 0;
        int oldVal = 0;

        int tmpIndex = 0;
        int keyIndex = 0;
        for (int index = 0; index < length; index++) {
            tmpIndex = startIndex + index;
            keyIndex = index & 0x7;
            oldVal = data[tmpIndex];
            decryptKey[keyIndex] = ((decryptKey[keyIndex] + lastCipherByte) ^ index);
            data[tmpIndex] = (byte) ((((data[index] & 0xff) - lastCipherByte) ^ decryptKey[keyIndex]) & 0xff);
            lastCipherByte = oldVal;
        }

        return data;
    }

    /**
     * 解密，解密后索引回滚到方法调用钱的标记位
     *
     * @param buffer     加密数据缓冲对象
     * @param decryptKey 密钥
     * @return 解密后数据
     * @throws Exception
     */
    public static ByteBuffer decryptCustom(ByteBuffer buffer, int[] decryptKey)
            throws Exception {
        return decryptCustom(buffer, buffer.remaining(), decryptKey);
    }

    /**
     * 解密，解密后索引回滚到方法调用钱的标记位
     *
     * @param buffer     数据缓冲对象
     * @param startIndex 加密数据起始索引
     * @param length     数据长度
     * @param decryptKey 密钥
     * @return 解密后数据
     * @throws Exception
     */
    public static ByteBuffer decryptCustom(ByteBuffer buffer, int startIndex,
                                           int length, int[] decryptKey) throws Exception {
        int indexMark = buffer.position();
        try {
            buffer.position(startIndex);
            return decryptCustom(buffer, length, decryptKey);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            buffer.position(indexMark);
        }
    }

    /**
     * 解密，解密后索引回滚到方法调用钱的标记位
     *
     * @param buffer     数据缓冲对象
     * @param length     数据长度
     * @param decryptKey 密钥
     * @return 解密后数据
     * @throws Exception
     */
    public static ByteBuffer decryptCustom(ByteBuffer buffer, int length,
                                           int[] decryptKey) throws Exception {
        if (length == 0)
            return buffer;

        if (decryptKey.length < 8)
            throw new Exception(
                    "The decryptKey must be 64bits length!");

        int lastCipherByte = 0;

        int indexMark = buffer.position();
        try {
            int oldVal = 0;
            int value = 0;
            int keyIndex = 0;
            for (int index = 0; index < length; index++) {
                keyIndex = index & 0x7;
                oldVal = buffer.get();
                decryptKey[keyIndex] = ((decryptKey[keyIndex] + lastCipherByte) ^ index);
                value = (byte) ((((oldVal & 0xff) - lastCipherByte) ^ decryptKey[keyIndex]) & 0xff);
                lastCipherByte = oldVal;
                putToPrevPosition(buffer, (byte) value);
            }

            return buffer;
        } finally {
            buffer.position(indexMark);
        }
    }

    private static void putToPrevPosition(ByteBuffer buffer, int value) {
        buffer.position(buffer.position() - 1);
        buffer.put((byte) value);
    }

    public static void encrypt(ByteBuffer buffer, int[] keys) {
        encryptCustom(buffer, keys, buffer.remaining());
    }

    public static void encryptCustom(ByteBuffer buffer, int[] keys, int startIndex,
                                     int length) {
        int indexMark = buffer.position();
        try {
            buffer.position(startIndex);
            encryptCustom(buffer, keys, length);
        } finally {
            buffer.position(indexMark);
        }
    }

    public static void encryptCustom(ByteBuffer buffer, int[] keys, int length) {
        int lastCipherByte = 0;
        int keyIndex = 0;
        byte tmpVal = 0;
        int indexMark = buffer.position();
        try {
            for (int i = 0; i < length; i++) {
                keyIndex = i & 0x7;
                tmpVal = buffer.get();
                keys[keyIndex] = ((keys[keyIndex] + lastCipherByte) ^ i) & 0xff;
                lastCipherByte = (((tmpVal ^ keys[keyIndex]) & 0xff) + lastCipherByte) & 0xff;
                putToPrevPosition(buffer, lastCipherByte);
            }
        } finally {
            buffer.position(indexMark);
        }
    }

    public static void encryptCustom(byte[] data, int[] keys) {
        encryptCustom(data, keys, 0, data.length);
    }

    public static void encryptCustom(byte[] data, int[] keys, int startIndex,
                                     int length) {
        int lastCipherByte = 0;

        // 循环加密
        int keyIndex = 0;
        int tempIndex = 0;
        for (int i = 0; i < length; i++) {
            keyIndex = i & 0x7;
            tempIndex = i + startIndex;
            keys[keyIndex] = ((keys[keyIndex] + lastCipherByte) ^ i) & 0xff;
            lastCipherByte = (((data[tempIndex] ^ keys[keyIndex]) & 0xff) + lastCipherByte) & 0xff;
            data[tempIndex] = (byte) lastCipherByte;
        }
    }

    public static void main(String[] args) {

    }
}
