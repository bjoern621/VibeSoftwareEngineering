package com.concertcomparison.infrastructure.util;

import com.concertcomparison.infrastructure.util.QrCodeService.QrCodeGenerationException;
import com.concertcomparison.presentation.dto.TicketDTO;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests für QrCodeService (US-179).
 * 
 * Testet:
 * - QR Code Generierung
 * - QR Code Content Validierung
 * - Error Handling
 */
@DisplayName("QrCodeService Unit Tests")
class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    @DisplayName("Sollte QR Code mit Standard-Größe (300x300) generieren")
    void generateQrCodeImage_DefaultSize_Success() throws Exception {
        // Arrange
        String content = "123|1|42|user@test.com";

        // Act
        byte[] qrCode = qrCodeService.generateQrCodeImage(content);

        // Assert
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode.length).isGreaterThan(100); // PNG sollte > 100 Bytes sein

        // PNG Header validieren (89 50 4E 47 0D 0A 1A 0A)
        assertThat(qrCode[0]).isEqualTo((byte) 0x89);
        assertThat(qrCode[1]).isEqualTo((byte) 0x50);
        assertThat(qrCode[2]).isEqualTo((byte) 0x4E);
        assertThat(qrCode[3]).isEqualTo((byte) 0x47);
    }

    @Test
    @DisplayName("Sollte QR Code mit custom Größe generieren")
    void generateQrCodeImage_CustomSize_Success() {
        // Arrange
        String content = "Test Content";
        int customSize = 500;

        // Act
        byte[] qrCode = qrCodeService.generateQrCodeImage(content, customSize);

        // Assert
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode.length).isGreaterThan(100);
    }

    @Test
    @DisplayName("Sollte QR Code mit korrektem Content erstellen")
    void generateQrCodeImage_ContentValidation_Success() throws Exception {
        // Arrange
        TicketDTO ticket = new TicketDTO(123L, 1L, 42L, "user@test.com");
        String expectedContent = ticket.toQrCodeContent();

        // Act
        byte[] qrCode = qrCodeService.generateQrCodeImage(expectedContent);

        // Assert - QR Code decodieren und Content prüfen
        String decodedContent = decodeQrCode(qrCode);
        assertThat(decodedContent).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("Sollte Exception werfen bei leerem Content")
    void generateQrCodeImage_EmptyContent_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> qrCodeService.generateQrCodeImage(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen bei null Content")
    void generateQrCodeImage_NullContent_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> qrCodeService.generateQrCodeImage(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen bei blank Content")
    void generateQrCodeImage_BlankContent_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> qrCodeService.generateQrCodeImage("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("darf nicht leer sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen bei negativer Größe")
    void generateQrCodeImage_NegativeSize_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> qrCodeService.generateQrCodeImage("Test", -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("muss positiv sein");
    }

    @Test
    @DisplayName("Sollte Exception werfen bei Größe 0")
    void generateQrCodeImage_ZeroSize_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> qrCodeService.generateQrCodeImage("Test", 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("muss positiv sein");
    }

    @Test
    @DisplayName("Sollte QR Code mit Sonderzeichen generieren")
    void generateQrCodeImage_SpecialCharacters_Success() throws Exception {
        // Arrange
        String content = "123|1|42|user+test@example.com";

        // Act
        byte[] qrCode = qrCodeService.generateQrCodeImage(content);

        // Assert
        assertThat(qrCode).isNotEmpty();
        String decodedContent = decodeQrCode(qrCode);
        assertThat(decodedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Sollte QR Code mit Umlauten generieren")
    void generateQrCodeImage_Umlauts_Success() throws Exception {
        // Arrange
        String content = "123|1|42|müller@test.de";

        // Act
        byte[] qrCode = qrCodeService.generateQrCodeImage(content);

        // Assert
        assertThat(qrCode).isNotEmpty();
        String decodedContent = decodeQrCode(qrCode);
        assertThat(decodedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Sollte QR Code mit langem Content generieren")
    void generateQrCodeImage_LongContent_Success() throws Exception {
        // Arrange
        String longContent = "1234567890|9876543210|1111111111|very.long.email.address@example-domain.com";

        // Act
        byte[] qrCode = qrCodeService.generateQrCodeImage(longContent);

        // Assert
        assertThat(qrCode).isNotEmpty();
        String decodedContent = decodeQrCode(qrCode);
        assertThat(decodedContent).isEqualTo(longContent);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Decodiert einen QR Code und gibt den Content zurück.
     * 
     * @param qrCodeBytes PNG Image als byte array
     * @return Decodierter Text
     */
    private String decodeQrCode(byte[] qrCodeBytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(qrCodeBytes);
        BufferedImage bufferedImage = ImageIO.read(bis);

        BinaryBitmap binaryBitmap = new BinaryBitmap(
            new HybridBinarizer(
                new BufferedImageLuminanceSource(bufferedImage)
            )
        );

        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
    }
}
