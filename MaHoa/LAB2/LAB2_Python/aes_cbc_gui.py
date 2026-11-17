"""LAB 2 - Bài nâng cao 02: AES-256-CBC bằng Python (Tkinter).

Cài thư viện (một lần):   py -m pip install cryptography
Chạy giao diện:           py aes_cbc_gui.py
Tự kiểm thử:              py aes_cbc_gui.py --self-test
"""
import base64
import secrets
import sys
import tkinter as tk
from tkinter import filedialog, messagebox, ttk

from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes


# ------------------------- PHẦN XỬ LÝ (không phụ thuộc giao diện) -------------------------
def new_key() -> str:
    """Khóa AES-256 ngẫu nhiên: 32 byte, trả về Base64."""
    return base64.b64encode(secrets.token_bytes(32)).decode("ascii")


def new_iv() -> str:
    """IV cho CBC: 16 byte ngẫu nhiên, trả về Base64. Mỗi lần mã hóa nên dùng IV mới."""
    return base64.b64encode(secrets.token_bytes(16)).decode("ascii")


def _key_iv(key_b64: str, iv_b64: str):
    try:
        key = base64.b64decode(key_b64.strip(), validate=True)
        iv = base64.b64decode(iv_b64.strip(), validate=True)
    except Exception:
        raise ValueError("Key/IV phải là chuỗi Base64 hợp lệ.")
    if len(key) != 32 or len(iv) != 16:
        raise ValueError("Key phải 32 byte và IV phải 16 byte. Bấm Generate Key / Generate IV.")
    return key, iv


def encrypt_text(plaintext: str, key_b64: str, iv_b64: str) -> str:
    key, iv = _key_iv(key_b64, iv_b64)
    padder = padding.PKCS7(128).padder()                      # đệm PKCS7 cho đủ bội số 16 byte
    data = padder.update(plaintext.encode("utf-8")) + padder.finalize()
    enc = Cipher(algorithms.AES(key), modes.CBC(iv)).encryptor()
    return base64.b64encode(enc.update(data) + enc.finalize()).decode("ascii")


def decrypt_text(ciphertext_b64: str, key_b64: str, iv_b64: str) -> str:
    key, iv = _key_iv(key_b64, iv_b64)
    try:
        data = base64.b64decode(ciphertext_b64.strip(), validate=True)
    except Exception:
        raise ValueError("Ciphertext phải là Base64.")
    if len(data) == 0 or len(data) % 16 != 0:
        raise ValueError("Độ dài bản mã CBC phải là bội số của 16 byte.")
    dec = Cipher(algorithms.AES(key), modes.CBC(iv)).decryptor()
    padded = dec.update(data) + dec.finalize()
    try:
        unpadder = padding.PKCS7(128).unpadder()
        plain = unpadder.update(padded) + unpadder.finalize()
        return plain.decode("utf-8")

    except Exception:
        raise ValueError("Giải mã thất bại: sai Key/IV hoặc bản mã bị sửa.")


# ------------------------------------- GIAO DIỆN -------------------------------------
class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("AES-CBC Python - LAB 2")
        self.resizable(False, False)
        f = ttk.Frame(self, padding=14)
        f.grid()
        ttk.Label(f, text="BÀI NÂNG CAO 02 - AES-256-CBC (PYTHON)", font=("Segoe UI", 14,"bold")).grid(row=0, column=0, columnspan=3, sticky="w", pady=(0, 8))
        ttk.Label(f, text="Plaintext:").grid(row=1, column=0, sticky="nw")
        self.txt_plain = tk.Text(f, width=64, height=5, wrap="word")
        self.txt_plain.grid(row=1, column=1, columnspan=2, pady=3)
        ttk.Label(f, text="Key (Base64):").grid(row=2, column=0, sticky="w")
        self.key = tk.StringVar()
        ttk.Entry(f, textvariable=self.key, width=60).grid(row=2, column=1, sticky="w", pady=3)
        ttk.Button(f, text="Generate Key", command=lambda: self.key.set(new_key())).grid(row=2, column=2, sticky="ew")
        ttk.Label(f, text="IV (Base64):").grid(row=3, column=0, sticky="w")
        self.iv = tk.StringVar()
        ttk.Entry(f, textvariable=self.iv, width=60).grid(row=3, column=1, sticky="w", pady=3)
        ttk.Button(f, text="Generate IV", command=lambda: self.iv.set(new_iv())).grid(row=3, column=2, sticky="ew")
        ttk.Label(f, text="Ciphertext:").grid(row=4, column=0, sticky="nw")
        self.txt_cipher = tk.Text(f, width=64, height=5, wrap="char")
        self.txt_cipher.grid(row=4, column=1, columnspan=2, pady=3)
        b = ttk.Frame(f)
        b.grid(row=5, column=1, columnspan=2, sticky="w", pady=6)
        for text, cmd in [("Encrypt", self.encrypt), ("Decrypt", self.decrypt), ("Save File", self.save), ("Open File", self.open)]:
            ttk.Button(b, text=text, command=cmd).pack(side="left", padx=(0, 8))
        self.status = tk.StringVar(value="Bấm Generate Key và Generate IV trước.")
        ttk.Label(f, textvariable=self.status).grid(row=6, column=1, columnspan=2, sticky="w")

    def _get(self, widget):
        return widget.get("1.0", "end-1c")

    def _set(self, widget, value):
        widget.delete("1.0", "end")
        widget.insert("1.0", value)

    def encrypt(self):
        try:
            self._set(self.txt_cipher, encrypt_text(self._get(self.txt_plain), self.key.get(), self.iv.get()))
            self.status.set("Đã mã hóa AES-CBC.")
        except ValueError as ex:
            messagebox.showerror("Lỗi", str(ex))

    def decrypt(self):
        try:
            self._set(self.txt_plain, decrypt_text(self._get(self.txt_cipher), self.key.get(), self.iv.get()))
            self.status.set("Đã giải mã AES-CBC.")
        except ValueError as ex:
            messagebox.showerror("Lỗi", str(ex))

    def save(self):
        """Lưu IV + bản mã (KHÔNG lưu Key)."""
        path = filedialog.asksaveasfilename(defaultextension=".txt", initialfile="aes_cbc_ciphertext.txt")
        if path:
            with open(path, "w", encoding="utf-8") as fh:
                fh.write("IV=" + self.iv.get().strip() + "\n" + self._get(self.txt_cipher).strip() + "\n")
            self.status.set("Đã lưu: " + path)

    def open(self):
        path = filedialog.askopenfilename(filetypes=[("Text file", "*.txt")])
        if path:
            with open(path, encoding="utf-8") as fh:
                lines = fh.read().strip().split("\n", 1)
            if len(lines) < 2 or not lines[0].startswith("IV="):
                messagebox.showerror("Lỗi", "Tệp không đúng định dạng (dòng 1: IV=..., dòng 2: bản mã).")
                return
            self.iv.set(lines[0][3:].strip())
            self._set(self.txt_cipher, lines[1].strip())
            self.status.set("Đã mở tệp. Nhập đúng Key rồi bấm Decrypt.")


def self_test():
    key, iv = new_key(), new_iv()
    long_text = "Văn bản dài tiếng Việt có dấu. " * 200
    for text in ["Xin chào AES-CBC", "", "1234567890123456", long_text]:
        assert decrypt_text(encrypt_text(text, key, iv), key, iv) == text
    c = encrypt_text("abc", key, iv)
    assert len(base64.b64decode(c)) == 16                       # 3 byte + đệm = 16 byte
    try:
        decrypt_text(c, new_key(), iv)
        raise AssertionError("sai khóa phải lỗi")
    except ValueError:
        pass
    print("SELF-TEST OK: 4 thông điệp mã hóa/giải mã đúng, sai khóa bị phát hiện.")


if __name__ == "__main__":
    if "--self-test" in sys.argv:
        self_test()
    else:
        App().mainloop()