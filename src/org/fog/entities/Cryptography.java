package org.fog.entities;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class Cryptography {

    //Keys for the stored method
    static String storedSymmetricKey = "t2NDM0Ah1pUSjgK4EwJUSw==";
    static String storedAsymmetricPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwUJdwOjOZ2RgQIfQyskQQBC44r6vTRcQLnkHOiiTpZ62ZT3pIFpcOYtDb8eOY5Cs/wHa7AMp14AYY9dDbAuLZbM/I5DR4iGuZhqK4a07TuK0N0cmLMxTzH+0JtVl2OMeP2SYe7XvSR/xwjipAcYP3McdaUWxlTPseILmwJUHhSlUQ72Cpijs52ocGH16bMAolkALbNKIWpFRenyCnraLgotff1K2a3GS2ae5QdVs6fzZ16QoSq5kMwY3QSAGLDDZNL3ad8KS+y4JLOLSjXhpUmmtozfSOiJ26Bg0WgsA0EdnQjow6Y16lPm/V4BnHVILfe5Ukk7iORy1vC7dhmYa5wIDAQAB";
    static String storedAsymmetricPrivateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDBQl3A6M5nZGBAh9DKyRBAELjivq9NFxAueQc6KJOlnrZlPekgWlw5i0Nvx45jkKz/AdrsAynXgBhj10NsC4tlsz8jkNHiIa5mGorhrTtO4rQ3RyYszFPMf7Qm1WXY4x4/ZJh7te9JH/HCOKkBxg/cxx1pRbGVM+x4gubAlQeFKVRDvYKmKOznahwYfXpswCiWQAts0ohakVF6fIKetouCi19/UrZrcZLZp7lB1Wzp/NnXpChKrmQzBjdBIAYsMNk0vdp3wpL7Lgks4tKNeGlSaa2jN9I6InboGDRaCwDQR2dCOjDpjXqU+b9XgGcdUgt97lSSTuI5HLW8Lt2GZhrnAgMBAAECggEAT7719ttaCHn36cjhVDxBc14zHIV4ysPAjpysachCnK3wL7irUQlYUvpvWlmrHNSfPh7i2k633k/iQJicHqJo5A9h92gT407oqwM62cLk3TlZChJW41dxIMp1aSzXePbHDXuSHEJHuhHvbah5vNygwQZ80WrLy3iAlH3XgBI+5ygdMpoUJ0xADrchDtWcJtEE4C/N2IvM1+0NzoAQuGVCIJHABAUWIN4kvJ5EQmhIjtnyEev+YhhqU8PAXdG5fzVWrfZqibvgp34uUniT6AyuJw+xnTz74N60q8GJScjw/1RAwpBMzmvldesJafzdG9fAh0P3olbP/tTXP5eolR/tSQKBgQD2TkivJPpDHjGHUcHtfKqR6QUsB8wuK0X0T8dN1CN3dE0zdge1Y38x7j89Lk4H4RGRf4Kw3Q/GqmiyEqfqcaLS/pkKs6tW9kJZy44Q//GzBhAQCT7oj7WpMFdxugwE+OxaVqDDouDbBMb0ysmYxgGv2x1B9B0O6cFjKHoHi3crQwKBgQDI3ZkwYD6tUTuJbSoljRobYl9nNx24nV7ntj5t3CKJaYwpAlzT7vs26O/4EgJVz8P/bQDonf6tK6KydqF2y9K0P55nH3dOKa4lTncpskAU0p99gGNv5paEVHBfAcglnSnOLb//u8RgKLdnJvUCRF945+0c+KbAVU2dTPlrFm2tjQKBgCJrEhEXc7lJUNZ+AEF1eEhnbYeXl1qkRWQ8ebdQe5xHrjoQtkcMnkuV5VRhUhobw163mMWqPngee/7WSIykUIJ3fV9XeAvw4Zi2ryWE/ntrz3bOM/I/kr0Pukh0HRXcyvpcvJ5Z9l3krwCqDb3BfZ7bWZ1LxXu+An4eu96uCHsNAoGBAJYJeJi6+3ZIvDkN0rKT8FFOLMcTU18IJdDcv0seaKnNrkUfn6kq1HkSi9T31si0VcngaRJYmdhORMwbcsYiv3OmJbMPvPUkixEQyE/b9qPXJA0BrsTyL7IOxJUi5pa6C3UXlVf3sFrHbx9c6RxVaB8O1n2SejQmQ8Bgs9m6uhgtAoGBAJu8s0Qw/2ZZmVyx5MAdJMCEWhGIqZKBs6Qed4kcOTPwbPRvAX0n7jcPqieCzD9KnqcOMqWHJLNy6R9i+Jk8Ptyh1/WXs79JitKUUis8LRRgpO+2EqMRv9C24XSgCRe4l9vr38JClxpyqf7M1hL6pWZLuRIZ2AiwG17hneu71Mw/";

    //Asymmetric keys
    static String asymmetricPublicKey;
    static String asymmetricPrivateKey;

    public static String getAsymmetricPublicKey(){
        return asymmetricPublicKey;
    }
    public static String getAsymmetricPrivateKey(){
        return asymmetricPrivateKey;
    }

    //Method to generate the symmetric keys
    public static void generateSymmetricKey(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // for example
            SecretKey secretKey = keyGen.generateKey();
            storedSymmetricKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());


        }catch (Exception e){
            throw new IllegalStateException(e);
        }
    }
    public static String getStoredSymmetricKey() {
      return storedSymmetricKey;
    }

    //Method to generate the asymmetric keys
    public static  void generateAsymmetricKeys(){
        try {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, new SecureRandom());
            KeyPair kp = keyGen.generateKeyPair();
            asymmetricPublicKey = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            asymmetricPrivateKey = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());

        }catch (Exception e){
            throw new IllegalStateException(e);
        }
    }
    public static String getStoredAsymmetricPublicKey() {
        return storedAsymmetricPublicKey;
    }
    public static String getStoredAsymmetricPrivateKey(){
        return storedAsymmetricPrivateKey;
    }


    //Generic method to encrypt the data depending on the type of encryption
    public static byte[] Encrypt(byte[] data, String key, EncryptionType type){
        return switch (type) {
            case AES -> AESEncryption(data, key);
            case RSA -> RSAEncryption(data, key);
            case NONE -> data;
        };
    }
    //Generic method to decrypt the data depending on the type of encryption
    public static byte[] Decrypt(byte[] data, String key, EncryptionType type){
        return switch (type) {
            case AES -> AESDecryption(data, key);
            case RSA -> RSADecryption(data, key);
            case NONE -> data;
        };
    }

    //Method for AES encryption
    private static byte[] AESEncryption(byte[] data, String key) {

        try{
            Cipher cipher = Cipher.getInstance("AES");
            Key AesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, AesKey);
            return cipher.doFinal(data);

        }catch (GeneralSecurityException e){
            throw new IllegalStateException(e);
        }
    }

    //Method for AES decryption
    private static byte[] AESDecryption(byte[] data, String key){

        try{
            Cipher cipher = Cipher.getInstance("AES");
            Key AesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, AesKey);
            return cipher.doFinal(data);

        }catch (GeneralSecurityException e){
            throw new IllegalStateException(e);
        }
    }

    //Method for RSA encryption
    private static byte[] RSAEncryption(byte[] data, String key) {

        try{
            byte[] keyBytes = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey RSAKey = keyFactory.generatePublic(spec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, RSAKey);
            return cipher.doFinal(data);

        }catch (GeneralSecurityException e){
            throw new IllegalStateException(e);
        }

    }

    //Method for RSA decryption
    private static byte[] RSADecryption(byte[] data, String key){

        try{
            byte[] keyBytes = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey RSAKey = fact.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, RSAKey);
            return cipher.doFinal(data);

        }catch (GeneralSecurityException e){
            throw new IllegalStateException( e);
        }

    }

    //Method to digitally sign data
    public static byte[] sign(byte[] data, String key){
        try{
            byte[] keyBytes = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey RSAKey = fact.generatePrivate(keySpec);

            Signature rsaSignature = Signature.getInstance("SHA256withRSA");
            rsaSignature.initSign(RSAKey);

            rsaSignature.update(data);

            return rsaSignature.sign();

        }catch (GeneralSecurityException e){
            throw new IllegalStateException(e);
        }

    }

    //Method to verify digital signature
    public static boolean verify(byte[] data, byte[] signature, String key){
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey RSAKey = keyFactory.generatePublic(spec);

            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(RSAKey);
            publicSignature.update(data);

            return publicSignature.verify(signature);

        }catch (Exception e){
            throw new IllegalStateException(e);
        }

    }

}
