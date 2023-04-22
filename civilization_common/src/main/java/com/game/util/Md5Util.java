package com.game.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5工具类
 *
 * @author wk.dai
 */
public class Md5Util {

  public static final String KEY = "halo.server.uc.verification";

  /** * MD5加码 生成32位md5码 */
  /*
   * public static String string2MD5(String inStr) { MessageDigest md5 = null; try
   * { md5 = MessageDigest.getInstance("MD5"); } catch (Exception e) {
   * e.printStackTrace(); return ""; } char[] charArray = inStr.toCharArray();
   * byte[] byteArray = new byte[charArray.length];
   *
   * for (int i = 0; i < charArray.length; i++) byteArray[i] = (byte)
   * charArray[i]; byte[] md5Bytes = md5.digest(byteArray); StringBuffer hexValue
   * = new StringBuffer(); for (int i = 0; i < md5Bytes.length; i++) { int val =
   * ((int) md5Bytes[i]) & 0xff; if (val < 16) hexValue.append("0");
   * hexValue.append(Integer.toHexString(val)); } return hexValue.toString();
   *
   * }
   */

  /** 加密解密算法 执行一次加密，两次解密 */
  public static String convertMD5(String inStr) {

    char[] a = inStr.toCharArray();
    for (int i = 0; i < a.length; i++) {
      a[i] = (char) (a[i] ^ 't');
    }
    String s = new String(a);
    return s;
  }

  /**
   * @param plainText
   * @return
   */
  public static String string2MD5(String plainText) {
    byte[] secretBytes = null;
    try {
      secretBytes = MessageDigest.getInstance("md5").digest(plainText.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("没有md5这个算法！");
    }
    String md5code = new BigInteger(1, secretBytes).toString(16); // 16进制数字
    // 如果生成数字未满32位，需要前面补0
    int length = md5code.length();
    for (int i = 0; i < 32 - length; i++) {
      md5code = "0" + md5code;
    }
    return md5code;
  }
}
