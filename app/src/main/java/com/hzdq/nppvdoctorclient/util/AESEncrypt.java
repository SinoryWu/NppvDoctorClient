package com.hzdq.nppvdoctorclient.util;


import android.util.Base64;
import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time:2023/10/11
 * Author:Sinory
 * Description:
 */
public class AESEncrypt {

    private static final String TAG = AESEncrypt.class.getSimpleName() + " --> ";

    /**
     * 加密算法
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * AES 的 密钥长度，32 字节，范围：16 - 32 字节
     */
    public static final int SECRET_KEY_LENGTH = 32;

    /**
     * 字符编码
     */
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    /**
     * 秘钥长度不足 16 个字节时，默认填充位数
     */
    private static final String DEFAULT_VALUE = "0";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";



    /**
     * AES 解密
     *
     * @param base64Data 加密的密文 Base64 字符串
     * @param secretKey  解密的密钥，长度：16 或 32 个字符
     */
    public static String decrypt(String base64Data, String secretKey) {
        try {
            byte[] data = base64Decode(base64Data);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey));
            //执行解密操作
            byte[] result = cipher.doFinal(data);
            return new String(result, CHARSET_UTF8);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * 使用密码获取 AES 秘钥
     */
    public static SecretKeySpec getSecretKey(String secretKey) {
        secretKey = toMakeKey(secretKey, SECRET_KEY_LENGTH, DEFAULT_VALUE);
        return new SecretKeySpec(secretKey.getBytes(CHARSET_UTF8), KEY_ALGORITHM);
    }

    /**
     * 如果 AES 的密钥小于 {@code length} 的长度，就对秘钥进行补位，保证秘钥安全。
     *
     * @param secretKey 密钥 key
     * @param length    密钥应有的长度
     * @param text      默认补的文本
     * @return 密钥
     */
    private static String toMakeKey(String secretKey, int length, String text) {
        // 获取密钥长度
        int strLen = secretKey.length();
        // 判断长度是否小于应有的长度
        if (strLen < length) {
            // 补全位数
            StringBuilder builder = new StringBuilder();
            // 将key添加至builder中
            builder.append(secretKey);
            // 遍历添加默认文本
            for (int i = 0; i < length - strLen; i++) {
                builder.append(text);
            }
            // 赋值
            secretKey = builder.toString();
        }
        return secretKey;
    }

    /**
     * 将 Base64 字符串 解码成 字节数组
     */
    public static byte[] base64Decode(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }

    /**
     * 将 字节数组 转换成 Base64 编码
     */
    public static String base64Encode(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * 处理异常
     */
    private static void handleException(Exception e) {
        e.printStackTrace();
        Log.e(TAG, TAG + e);
    }

    /**
     * AES 加密
     *
     * @param data      待加密内容
     * @param secretKey 加密密码，长度：16 或 32 个字符
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String data, String secretKey) {
        try {
            //创建密码器
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            //初始化为加密密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey));
            byte[] encryptByte = cipher.doFinal(data.getBytes(CHARSET_UTF8));
            // 将加密以后的数据进行 Base64 编码
            String s = new String(encryptByte);
            return s;

        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }


    // AES 加密
    public static String encryptAES(String plaintext, String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            Key aesKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
//    public static String encrypt2(final String key, final String plaintext) {
//        try {
//            final Cipher encryptCipher = Cipher.getInstance("AES");
//            encryptCipher.init(Cipher.ENCRYPT_MODE, generateMySQLAESKey(key, "UTF-8"));
//            return StringUtils.valueOf(Hex.encodeHex(encryptCipher.doFinal(plaintext.getBytes("UTF-8"))));
//        } catch (Exception e) {
//            log.error("数据库加密失败,{}", e.getMessage());
//        }
//        return null;
//    }
//
//    public static SecretKeySpec generateMySQLAESKey(final String key, final String encoding) {
//        try {
//            final byte[] finalKey = new byte[16];
//            int i = 0;
//            for (byte b : key.getBytes(encoding))
//                finalKey[i++ % 16] ^= b;
//            return new SecretKeySpec(finalKey, "AES");
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//    }


}