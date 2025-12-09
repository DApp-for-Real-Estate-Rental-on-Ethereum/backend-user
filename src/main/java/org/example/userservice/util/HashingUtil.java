package org.example.userservice.util;

import com.google.common.hash.Hashing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class HashingUtil {

    PasswordEncoder encoder;

    Integer encoderStrength = 16;

    public HashingUtil() {
        this.encoder = new BCryptPasswordEncoder(encoderStrength);
    }

    public String encode(String plainText) {
        return encoder.encode(plainText);
    }

    public boolean checkHash(String plainText, String hashedText) {
        return encoder.matches(plainText, hashedText);
    }

    public static String hashToken(String token) {
        return Hashing.sha256()
                .hashString(token, StandardCharsets.UTF_8)
                .toString();
    }
}
