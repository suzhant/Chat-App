package com.sushant.whatsapp.Utils;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class Encryption {
    private static AesCbcWithIntegrity.SecretKeys secretKeys;

    static {
        try {
            secretKeys = getKey();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


    public static String encryptMessage(String plainText) {
        String ciphertextString = null;
        try {
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(plainText, secretKeys);
            //store or send to server
            ciphertextString = cipherTextIvMac.toString();
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ciphertextString;
    }

    public static String decryptMessage(String cipherText) throws GeneralSecurityException, UnsupportedEncodingException {
        //Use the constructor to re-create the CipherTextIvMac class from the string:
        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(cipherText);
        return AesCbcWithIntegrity.decryptString(cipherTextIvMac, secretKeys);
    }

    public static AesCbcWithIntegrity.SecretKeys getKey() throws GeneralSecurityException {
        String EXAMPLE_PASSWORD = "7h!s 1s H3l&";
//        String salt = AesCbcWithIntegrity.saltString(generateSalt());
        return AesCbcWithIntegrity.generateKeyFromPassword(EXAMPLE_PASSWORD, "123");
        //   return AesCbcWithIntegrity.generateKey();
    }

    public static String getAudioLast() {
        return ("Recorded Audio");
    }

    public static String getPhotoLast() {
        return ("photo.jpg");
    }

}
