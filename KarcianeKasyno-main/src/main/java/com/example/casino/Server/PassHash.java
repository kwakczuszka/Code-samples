package com.example.casino.Server;

import javafx.util.Pair;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.Callable;

public class PassHash implements Callable<Pair<String, String>> {
    public PassHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        prehash = password;

    }

    public static boolean comparePasswd(String psd, String salt, String hash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] slt = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(psd.toCharArray(), slt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash1 = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash1).equals(hash);
    }

    private String prehash;

    @Override
    public Pair<String, String> call() throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(prehash.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return new Pair<>(Base64.getEncoder().encodeToString(hash), Base64.getEncoder().encodeToString(salt));
    }
}
