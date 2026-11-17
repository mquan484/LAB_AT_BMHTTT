package week_02;

import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256 chế độ GCM (vừa mã hóa vừa chống sửa bản mã).
 * Khóa AES 256 bit được tạo từ mật khẩu (Key) bằng PBKDF2 với salt ngẫu nhiên.
 * Bản mã = Base64( salt 16 byte || nonce 12 byte || dữ liệu mã hóa + tag 16 byte ).
 */
public final class AESCipher {

    private static final int ITERATIONS = 120_000;

    private AESCipher() {
    }

    public static String encrypt(String plaintext, String password) throws Exception {
        CipherUtil.requireKey(password);
        byte[] salt = CipherUtil.randomBytes(16);
        byte[] nonce = CipherUtil.randomBytes(12);
        byte[] key = CipherUtil.pbkdf2(password.toCharArray(), salt, ITERATIONS, 256);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        byte[] encrypted = cipher.doFinal(CipherUtil.utf8(plaintext));
        return CipherUtil.b64(CipherUtil.concat(CipherUtil.concat(salt, nonce), encrypted));
    }

    public static String decrypt(String ciphertext, String password) throws Exception {
        CipherUtil.requireKey(password);
        byte[] all = CipherUtil.fromB64(ciphertext);
        if (all.length < 16 + 12 + 16) {
            throw new IllegalArgumentException("Bản mã AES quá ngắn hoặc bị hỏng.");
        }
        byte[] salt = Arrays.copyOfRange(all, 0, 16);
        byte[] nonce = Arrays.copyOfRange(all, 16, 28);
        byte[] data = Arrays.copyOfRange(all, 28, all.length);
        byte[] key = CipherUtil.pbkdf2(password.toCharArray(), salt, ITERATIONS, 256);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        return CipherUtil.text(cipher.doFinal(data)); // sai khóa / bản mã bị sửa -> AEADBadTagException
    }
}
