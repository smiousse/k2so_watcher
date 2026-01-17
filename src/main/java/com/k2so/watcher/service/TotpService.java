package com.k2so.watcher.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class TotpService {

    private static final Logger logger = LoggerFactory.getLogger(TotpService.class);

    @Value("${k2so.security.totp-issuer:K2SO Watcher}")
    private String issuer;

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    public TotpService() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeDataUri(String secret, String username) {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            byte[] imageData = qrGenerator.generate(data);
            return getDataUriForImage(imageData, qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            logger.error("Error generating QR code", e);
            return null;
        }
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            logger.error("Error verifying TOTP code", e);
            return false;
        }
    }

    public String getManualEntryKey(String secret) {
        // Format secret for manual entry (groups of 4 characters)
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(secret.charAt(i));
        }
        return formatted.toString();
    }
}
