package com.insoline.pnd.utils;

import android.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.security.SecureRandom;
import java.security.MessageDigest;

public class AES256Util {

//    public static void main(String[] args) throws Exception {
//        String text = "rlHVFyZZiUF9LYD46O0eNaS9PLrPpmLUoTU6RQlj-alIOIJPY1Vm--bVcU4bDNtOoaYZ8ESTEy48egIfJfA4cEaasWM1o3ZjKTg6Jg==";
//        String skey = "nopassword";
//        System.out.println(decode(text, skey));
//        String enc = encode("자바로 암호화 복호화 성공!!!", skey);
//        System.out.println(enc);
//        System.out.println(decode(enc, skey));
//
//    String text = "rlHVFyZZiUF9LYD46O0eNaS9PLrPpmLUoTU6RQlj-alIOIJPY1Vm--bVcU4bDNtOoaYZ8ESTEy48egIfJfA4cEaasWM1o3ZjKTg6Jg==";
//    String skey = "nopassword";
//
//    String strDecode = null;
//        try {
//        strDecode = AES256Util.decode(text, skey);
//    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
//                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
//        e.printStackTrace();
//    }
//    String strEncode = null;
//        try {
//        strEncode = AES256Util.encode("자바로 암호화 복호화 성공!!!", skey);
//    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
//                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
//        e.printStackTrace();
//    }
//    String strEnDe = null;
//        try {
//        strEnDe = AES256Util.decode(strEncode, skey);
//    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
//                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
//        e.printStackTrace();
//    }
//
//        LogHelper.e("strDecode : " + strDecode + "\n" + "strEncode : " + strEncode + "\n" + "strEnDe : " + strEnDe);
//
//    }

    // base64 문자열을 aes복호화하여 문자열 리턴
    public static String decode(String base64Text, String skey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] key = skey.getBytes();
//        byte[] inputArr = Base64.getUrlDecoder().decode(base64Text);
        byte[] inputArr = Base64.decode(base64Text, Base64.URL_SAFE);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(key);
		byte[] tmp = md.digest();
		byte[] newKey = new byte[16];
        System.arraycopy(tmp, 0, newKey, 0, 16);
        
        //SecretKeySpec skSpec = new SecretKeySpec(key, "AES");
		SecretKeySpec skSpec = new SecretKeySpec(newKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        int blockSize = cipher.getBlockSize();

        IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(inputArr, blockSize));
        byte[] dataToDecrypt = Arrays.copyOfRange(inputArr, blockSize, inputArr.length);
        cipher.init(Cipher.DECRYPT_MODE, skSpec, iv);
        byte[] result = cipher.doFinal(dataToDecrypt);
        return new String(result, StandardCharsets.UTF_8);
    }

    //// 문자열을 aes 암호화하여 base64 스트링으로 반환 
    public static String encode(String oriText, String skey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
    {
        // key sha256해시값중 앞 16바이트를 key값으로 사용       
        byte[] key = skey.getBytes();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(key);
		byte[] tmp = md.digest();
		byte[] newKey = new byte[16];
        System.arraycopy(tmp, 0, newKey, 0, 16);
        
        SecureRandom rand = new SecureRandom();
        SecretKeySpec key_spec = new SecretKeySpec(newKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        byte[] encoded_payload = oriText.getBytes();
        int block_size = cipher.getBlockSize();
        byte[] buffer = new byte[block_size];
        rand.nextBytes(buffer);
        IvParameterSpec iv = new IvParameterSpec (buffer);
        buffer = Arrays.copyOf (buffer, block_size+encoded_payload.length);
        cipher.init(Cipher.ENCRYPT_MODE, key_spec, iv);
        try {
            cipher.doFinal(encoded_payload,0,encoded_payload.length, buffer,block_size);
        } catch (Exception e) {

        }
//        String encoded = Base64.getUrlEncoder().encodeToString(buffer);
        String encoded = Base64.encodeToString(buffer, Base64.URL_SAFE);

        return encoded;    
    }
}