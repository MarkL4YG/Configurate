package de.mlessmann.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Life4YourGames on 22.08.16.
 */
public class MD5 {

    public static byte[] md5File(String file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = new FileInputStream(new File(file));
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] _b = new byte[1024];
                while (dis.read(_b) != -1) {
                }
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            //Masquerade to IOE due to laziness ;)
            throw new IOException("Unable to generate MD5 -> MD5 algorithm unknown!");
        }
    }

    public static String digestToString(byte[] _b) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < _b.length; i++) {

            if ((0xff & _b[i]) < 0x10) {
                b.append("0").append(Integer.toHexString(0xff & _b[i]));
            } else {
                b.append(Integer.toHexString(0xff & _b[i]));
            }
        }
        return b.toString();
    }
}

