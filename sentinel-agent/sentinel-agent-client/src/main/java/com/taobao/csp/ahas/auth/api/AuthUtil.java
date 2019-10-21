package com.taobao.csp.ahas.auth.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

public class AuthUtil {
   public static final String CHARSET_NAME = "UTF-8";
   public static final String AHAS_CERT_ENV = "ahas.cert";
   public static final String ACCESS_KEY = "AK";
   public static final String SECRET_KEY = "SK";
   private static final String CR_LF_UNIX = "\n";
   private static final String CR_LF_WIN = "\r\n";
   private static final String DEFAULT_CREDENTIAL_PATH = System.getProperty("user.home", "/home") + "/.ahas.cert";
   private static String accessKey;
   private static String secretKey;

   public static String generateAccessKey() {
      return UUID.randomUUID().toString().replaceAll("-", "");
   }

   public static String generateSecretKey() {
      return UUID.randomUUID().toString().replaceAll("-", "");
   }

   public static String getAccessKey() {
      if (accessKey != null) {
         return accessKey;
      } else {
         readCredentialKey();
         return accessKey;
      }
   }

   public static String getSecretKey() {
      if (secretKey != null) {
         return secretKey;
      } else {
         readCredentialKey();
         return secretKey;
      }
   }

   private static void readCredentialKey() {
      File credentialFile = getCredentialFile();
      Properties properties = new Properties();
      FileInputStream inStream = null;

      try {
         inStream = new FileInputStream(credentialFile);
         properties.load(inStream);
         accessKey = properties.getProperty("AK");
         secretKey = properties.getProperty("SK");
      } catch (IOException var11) {
         throw new RuntimeException("Load credential file exception, file: " + credentialFile.getAbsolutePath());
      } finally {
         if (inStream != null) {
            try {
               inStream.close();
            } catch (IOException var10) {
            }
         }

      }

   }

   public static File getCredentialFile() {
      String certFile = System.getProperty("ahas.cert", DEFAULT_CREDENTIAL_PATH);
      File file = new File(certFile);
      if (!file.exists()) {
         throw new RuntimeException("Cannot find ahas credential file, please specify -Dahas.cert=/path/file jvm opts.");
      } else {
         return file;
      }
   }

   public static void reloadCredentialFile() {
      readCredentialKey();
   }

   public static void recordKeyToFile(String accessKey, String secretKey) throws Exception {
      if (!isEmpty(accessKey) && !isEmpty(secretKey)) {
         File credentialFile = null;

         try {
            credentialFile = getCredentialFile();
         } catch (Exception var9) {
         }

         if (credentialFile == null) {
            credentialFile = new File(System.getProperty("ahas.cert", DEFAULT_CREDENTIAL_PATH));
         }

         if (!credentialFile.exists()) {
            boolean newFile = credentialFile.createNewFile();
            if (!newFile) {
               throw new Exception("Create file failed. file: " + credentialFile.getAbsolutePath());
            }
         }

         FileWriter fileWriter = null;

         try {
            fileWriter = new FileWriter(credentialFile, false);
            String key = "AK=" + accessKey;
            String separator = System.getProperty("line.separator");
            fileWriter.write(key + separator);
            key = "SK=" + secretKey;
            fileWriter.write(key + separator);
            fileWriter.flush();
         } finally {
            if (fileWriter != null) {
               fileWriter.close();
            }

         }

         reloadCredentialFile();
      } else {
         throw new Exception("accessKey or secretKey is empty");
      }
   }

   public static boolean auth(String sign, String secretKey, String signData) {
      try {
         String expectSign = sign(secretKey, signData);
         return expectSign.equals(sign);
      } catch (Exception var4) {
         return false;
      }
   }

   public static String sign(String sk, String signData) throws Exception {
      if (!isEmpty(sk) && !isEmpty(signData)) {
         String sha256 = sha256(signData + sk);
         return base64(sha256.getBytes("UTF-8"), false);
      } else {
         throw new IllegalArgumentException("the parameter is empty");
      }
   }

   public static String encrypt(String sk, String message) throws Exception {
      if (message != null && !message.isEmpty()) {
         SecretKeySpec keySpec = new SecretKeySpec(DigestUtils.md5(sk), "AES");
         Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
         IvParameterSpec iv = new IvParameterSpec(DigestUtils.md5(sk));
         cipher.init(1, keySpec, iv);
         byte[] bytes = message.getBytes("utf-8");
      //   int i128 = true;
         byte[] result = new byte[(bytes.length + 128 - 1) / 128 * 128];

         for(int i = 0; i < bytes.length; i += 128) {
            int len = 128;
            if (i + len > bytes.length) {
               len = bytes.length - i;
            }

            byte[] en = encrypt128(bytes, i, len, cipher);
            System.arraycopy(en, 0, result, i, 128);
         }

         return new String(Base64.encodeBase64(result));
      } else {
         return message;
      }
   }

   private static byte[] encrypt128(byte[] bytes, int offset, int len, Cipher cipher) throws Exception {
     // int i128 = true;
      byte[] result;
      if (len == 128) {
         result = cipher.doFinal(bytes, offset, 128);
      } else {
         result = cipher.doFinal(Arrays.copyOfRange(bytes, offset, offset + 128));
      }

      return result;
   }

   public static String decrypt(String sk, String message) throws Exception {
      if (message != null && !message.isEmpty()) {
         SecretKeySpec keySpec = new SecretKeySpec(DigestUtils.md5(sk), "AES");
         Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
         IvParameterSpec iv = new IvParameterSpec(DigestUtils.md5(sk));
         cipher.init(2, keySpec, iv);
         byte[] bytes = Base64.decodeBase64(message.getBytes());
      //   int i128 = true;
         if (bytes.length % 128 != 0) {
            throw new RuntimeException("illegal message size, must mod 128");
         } else {
            byte[] result = new byte[bytes.length];

            for(int i = 0; i < bytes.length; i += 128) {
               byte[] decrypt = cipher.doFinal(bytes, i, 128);
               System.arraycopy(decrypt, 0, result, i, 128);
            }

            return (new String(result, "utf-8")).trim();
         }
      } else {
         return message;
      }
   }

   public static String sha256(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] sha256 = digest.digest(value.getBytes("UTF-8"));
      return new String(Hex.encodeHex(sha256));
   }

   public static String base64(byte[] bytes, boolean isChunked) {
      byte[] byte64 = Base64.encodeBase64(bytes, isChunked);
      String result = new String(byte64);
      String endChars = "\n";
      if (!result.endsWith("\n")) {
         endChars = "\r\n";
         if (!result.endsWith("\r\n")) {
            return result;
         }
      }

      result = result.substring(0, result.length() - endChars.length());
      return result;
   }

   public static String md5(File file) throws IOException {
      return DigestUtils.md5Hex(readFileToByteArray(file));
   }

   private static byte[] readFileToByteArray(File file) throws IOException {
      FileInputStream in = null;

      byte[] bytes;
      try {
         in = openInputStream(file);
         bytes = toByteArray(in, file.length());
      } finally {
         if (in != null) {
            in.close();
         }

      }

      return bytes;
   }

   private static byte[] toByteArray(InputStream input, long size) throws IOException {
      if (size > 2147483647L) {
         throw new IllegalArgumentException("Exceeding the maximum limit of integer, size: " + size);
      } else {
         int length = (int)size;
         if (length < 0) {
            throw new IllegalArgumentException("Size cannot be negative, size: " + length);
         } else if (length == 0) {
            return new byte[0];
         } else {
            byte[] data = new byte[length];

            int offset;
            int readed;
            for(offset = 0; offset < length && (readed = input.read(data, offset, length - offset)) != -1; offset += readed) {
            }

            if (offset != length) {
               throw new IOException("Readed offset not equal the file length. current: " + offset + ", excepted: " + length);
            } else {
               return data;
            }
         }
      }
   }

   private static FileInputStream openInputStream(File file) throws IOException {
      if (file.exists()) {
         if (file.isDirectory()) {
            throw new IOException("File '" + file + "' exists but is a directory");
         } else if (!file.canRead()) {
            throw new IOException("File '" + file + "' cannot be read");
         } else {
            return new FileInputStream(file);
         }
      } else {
         throw new FileNotFoundException("File '" + file + "' does not exist");
      }
   }

   private static boolean isEmpty(String value) {
      return value == null || value.trim().length() == 0;
   }
}
