package com.newsolicitudes.newsolicitudes.services.ceropapel;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import com.newsolicitudes.newsolicitudes.config.CeroPapelProperties;

@Service
public class CeroPapelClaveService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final CeroPapelProperties properties;
    public CeroPapelClaveService(CeroPapelProperties properties) { this.properties = properties; }

    public byte[] cifrar(String clave) {
        try {
            byte[] nonce = new byte[12];
            RANDOM.nextBytes(nonce);
            var cipher = crearCipher(Cipher.ENCRYPT_MODE, nonce);
            byte[] encrypted = cipher.doFinal(clave.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.allocate(nonce.length + encrypted.length).put(nonce).put(encrypted).array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo proteger la clave de Cero Papel", e);
        }
    }

    public String descifrar(byte[] valor) {
        if (valor.length == 0) return "";
        if (valor.length < 28) throw new IllegalStateException("La clave protegida no es válida");
        try {
            var buffer = ByteBuffer.wrap(valor);
            byte[] nonce = new byte[12];
            buffer.get(nonce);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            return new String(crearCipher(Cipher.DECRYPT_MODE, nonce).doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo recuperar la clave de Cero Papel", e);
        }
    }

    private Cipher crearCipher(int modo, byte[] nonce) throws GeneralSecurityException {
        byte[] key;
        try { key = Base64.getDecoder().decode(properties.getEncryptionKey()); }
        catch (IllegalArgumentException e) { throw new IllegalStateException("Configure CEROPAPEL_ENCRYPTION_KEY válida", e); }
        if (key.length != 32) throw new IllegalStateException("Configure CEROPAPEL_ENCRYPTION_KEY de 32 bytes en Base64");
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(modo, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        return cipher;
    }
}
