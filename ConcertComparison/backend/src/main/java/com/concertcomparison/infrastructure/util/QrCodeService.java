package com.concertcomparison.infrastructure.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service zur QR Code Generierung (US-179).
 * 
 * Verwendet ZXing Library zum Erstellen von QR Codes als PNG Images.
 * 
 * @author CONCERT COMPARISON Team
 * @since US-179
 */
@Service
public class QrCodeService {

    private static final int DEFAULT_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generiert einen QR Code als PNG Image.
     * 
     * @param content Text der in QR Code encodiert werden soll
     * @param size Größe des QR Codes in Pixel (Breite = Höhe)
     * @return PNG Image als byte array
     * @throws IllegalArgumentException wenn Content leer ist
     * @throws QrCodeGenerationException bei Generierungsfehlern
     */
    public byte[] generateQrCodeImage(String content, int size) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("QR Code Content darf nicht leer sein");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("QR Code Größe muss positiv sein");
        }

        try {
            // QR Code Konfiguration
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // QR Code Matrix generieren
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            );

            // Matrix zu PNG konvertieren
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            
            return outputStream.toByteArray();

        } catch (WriterException e) {
            throw new QrCodeGenerationException(
                "Fehler beim Encodieren des QR Codes: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new QrCodeGenerationException(
                "Fehler beim Schreiben des QR Code Images: " + e.getMessage(), e);
        }
    }

    /**
     * Generiert einen QR Code mit Standard-Größe (300x300px).
     * 
     * @param content Text der in QR Code encodiert werden soll
     * @return PNG Image als byte array
     */
    public byte[] generateQrCodeImage(String content) {
        return generateQrCodeImage(content, DEFAULT_SIZE);
    }

    /**
     * Custom Exception für QR Code Generierungsfehler.
     */
    public static class QrCodeGenerationException extends RuntimeException {
        public QrCodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
