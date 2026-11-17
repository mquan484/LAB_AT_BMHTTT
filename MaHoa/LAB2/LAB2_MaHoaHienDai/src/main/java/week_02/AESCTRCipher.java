package week_02;

import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Bài nâng cao 01: AES-256 chế độ CTR (biến AES thành mã dòng, không cần đệm).
 * Khóa 32 byte và nonce 12 byte sinh ngẫu nhiên, hiển thị Base64. Bộ đếm 4 byte cuối bắt đầu từ 1.
 * KHÔNG được dùng lại cùng (khóa, nonce) cho hai thông điệp khác nhau.
 */
public final class AESCTRCipher {

    private AESCTRCipher() {
    }

    public static String newKey() {
        return CipherUtil.b64(CipherUtil.randomBytes(32));
    }

    public static String newNonce() {
        return CipherUtil.b64(CipherUtil.randomBytes(12));
    }

    private static byte[] crypt(int mode, byte[] input, String keyB64, String nonceB64) throws Exception {
        byte[] key = CipherUtil.fromB64(keyB64);
        byte[] nonce = CipherUtil.fromB64(nonceB64);
        if (key.length != 32 || nonce.length != 12) {
            throw new IllegalArgumentException("Cần Key 32 byte và Nonce 12 byte (Base64). Bấm Generate Key / Generate Nonce.");
        }
        byte[] counterBlock = Arrays.copyOf(nonce, 16); // nonce 12 byte || bộ đếm 4 byte
        counterBlock[15] = 1;
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(mode, new SecretKeySpec(key, "AES"), new IvParameterSpec(counterBlock));
        return cipher.doFinal(input);
    }

    public static String encrypt(String plaintext, String keyB64, String nonceB64) throws Exception
{
        return CipherUtil.b64(crypt(Cipher.ENCRYPT_MODE, CipherUtil.utf8(plaintext), keyB64, nonceB64));
    }

    public static String decrypt(String ciphertext, String keyB64, String nonceB64) throws Exception
{
        return CipherUtil.text(crypt(Cipher.DECRYPT_MODE, CipherUtil.fromB64(ciphertext), keyB64, nonceB64));
    }

    /** Nội dung tệp lưu: dòng 1 = NONCE, dòng 2 = bản mã. KHÔNG BAO GIỜ lưu Key chung tệp với bản mã. */
    public static String toFile(String nonceB64, String ciphertext) {
        return "NONCE=" + nonceB64.trim() + "\n" + ciphertext.trim() + "\n";
    }

    /** Tách tệp đã lưu thành {nonce, bản mã}. */
    public static String[] fromFile(String content) {
        String[] lines = content.trim().split("\\R", 2);
        if (lines.length < 2 || !lines[0].startsWith("NONCE=")) {
            throw new IllegalArgumentException("Tệp không đúng định dạng: dòng 1 phải là NONCE=..., dòng 2 là bản mã.");
        }
        return new String[] {lines[0].substring(6).trim(), lines[1].trim()};
    }
}