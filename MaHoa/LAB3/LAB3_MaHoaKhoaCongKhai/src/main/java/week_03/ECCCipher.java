package week_03;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.KeyAgreement;
/**
* Mã hóa ECC kiểu ECIES: ECDH trên đường cong secp256r1 + HKDF-SHA256 + AES-256-GCM.
* Mỗi lần mã hóa sinh một cặp khóa tạm (ephemeral); bí mật chung ECDH -> khóa AES.
* Bản mã = Base64( khóa công khai tạm 91 byte || salt 16 || nonce 12 || dữ liệu AES-GCM ).
*/
public final class ECCCipher {
 private static final byte[] INFO = CryptoSupport.utf8("LAB3-ECC-AESGCM");
 private static final int EPH = 91; // độ dài khóa công khai secp256r1 dạng X.509
 private PublicKey publicKey;
 private PrivateKey privateKey;
 private static KeyPair newPair() throws Exception {
 KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
 gen.initialize(new ECGenParameterSpec("secp256r1"));
 return gen.generateKeyPair();
 }
 public void generateKeys() throws Exception {
 KeyPair p = newPair();
 publicKey = p.getPublic();
 privateKey = p.getPrivate();
 }
 public String encrypt(String plaintext) throws Exception {
 if (publicKey == null) {
 throw new IllegalStateException("Chưa có khóa: bấm Generate Keys hoặc Load Keys.");
 }
 KeyPair eph = newPair(); // 1) cặp khóa tạm
 KeyAgreement ka = KeyAgreement.getInstance("ECDH");
 ka.init(eph.getPrivate());
 ka.doPhase(publicKey, true); // 2) bí mật chung = d_tạm * Q_người_nhận
 byte[] salt = CryptoSupport.randomBytes(16);
 byte[] nonce = CryptoSupport.randomBytes(12);
 byte[] aesKey = CryptoSupport.hkdf(ka.generateSecret(), salt, INFO); // 3) khóa AES
 byte[] data = CryptoSupport.gcmEncrypt(aesKey, nonce, CryptoSupport.utf8(plaintext));
 return CryptoSupport.b64(CryptoSupport.concat(eph.getPublic().getEncoded(), salt, nonce, data));
 }
 public String decrypt(String ciphertext) throws Exception {
 if (privateKey == null) {
 throw new IllegalStateException("Chưa có khóa riêng.");
 }
 byte[] all = CryptoSupport.unb64(ciphertext);
 PublicKey eph = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(CryptoSupport.slice(all, 0, EPH)));
 byte[] salt = CryptoSupport.slice(all, EPH, EPH + 16);
 byte[] nonce = CryptoSupport.slice(all, EPH + 16, EPH + 28);
 byte[] data = CryptoSupport.slice(all, EPH + 28, all.length);
 KeyAgreement ka = KeyAgreement.getInstance("ECDH");
 ka.init(privateKey);
 ka.doPhase(eph, true); // cùng bí mật chung = d_người_nhận * Q_tạm
 byte[] aesKey = CryptoSupport.hkdf(ka.generateSecret(), salt, INFO);
 return CryptoSupport.text(CryptoSupport.gcmDecrypt(aesKey, nonce, data));
 }
 public String getPublicKeyBase64() {
 return CryptoSupport.b64(publicKey.getEncoded());
 }
 public String getPrivateKeyBase64() {
 return CryptoSupport.b64(privateKey.getEncoded());
 }
 public void loadKeys(String publicB64, String privateB64) throws Exception {
 KeyFactory kf = KeyFactory.getInstance("EC");
 publicKey = kf.generatePublic(new X509EncodedKeySpec(CryptoSupport.unb64(publicB64)));
 privateKey = privateB64.isBlank() ? null : kf.generatePrivate(new PKCS8EncodedKeySpec(CryptoSupport.unb64(privateB64)));
 }
}