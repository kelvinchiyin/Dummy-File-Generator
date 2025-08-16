package com.kelvinchiyin.generator.file.dummy;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.kelvinchiyin.file.dummy.Generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {

    private static final long TEST_SIZE_1MB = 1024 * 1024; // 1MB for fast tests
    private static final long TEST_SIZE_5MB = 5L * 1024 * 1024; // 5MB for thorough tests
    
    @TempDir
    static Path tempDir;

    private Generator generator;
    private String baseFileName;

    @BeforeEach
    void setUp() {
        baseFileName = "test_file";
        generator = new Generator(baseFileName, TEST_SIZE_1MB);
    }

    @Test
    @DisplayName("Test DOCX generation creates correct size")
    void testCreateDocx() throws IOException {
        byte[] docxData = generator.createDocxWithContent();
        
        assertNotNull(docxData, "DOCX data should not be null");
        assertEquals(TEST_SIZE_1MB, docxData.length, "DOCX should be exactly 1MB");
        
        // Verify it's a valid DOCX (starts with PK - ZIP format)
        assertTrue(isValidZipFile(docxData), "DOCX should be valid ZIP file");
    }

    @Test
    @DisplayName("Test XLSX generation creates correct size")
    void testCreateXlsx() throws IOException {
        byte[] xlsxData = generator.createXlsx();
        
        assertNotNull(xlsxData, "XLSX data should not be null");
        assertEquals(TEST_SIZE_1MB, xlsxData.length, "XLSX should be exactly 1MB");
        
        // Verify it's a valid XLSX (starts with PK - ZIP format)
        assertTrue(isValidZipFile(xlsxData), "XLSX should be valid ZIP file");
    }

    @Test
    @DisplayName("Test PPTX generation creates correct size")
    void testCreatePptx() throws IOException {
        byte[] pptxData = generator.createPptx();
        
        assertNotNull(pptxData, "PPTX data should not be null");
        assertEquals(TEST_SIZE_1MB, pptxData.length, "PPTX should be exactly 1MB");
        
        // Verify it's a valid PPTX (starts with PK - ZIP format)
        assertTrue(isValidZipFile(pptxData), "PPTX should be valid ZIP file");
    }

    @Test
    @DisplayName("Test PDF generation creates correct size")
    void testCreatePdf() throws IOException {
        byte[] pdfData = generator.createPdf();
        
        assertNotNull(pdfData, "PDF data should not be null");
        assertEquals(TEST_SIZE_1MB, pdfData.length, "PDF should be exactly 1MB");
        
        // Verify it's a valid PDF (starts with %PDF)
        assertTrue(isValidPdfFile(pdfData), "PDF should start with %PDF");
    }

    @Test
    @DisplayName("Test JPG generation creates correct size")
    void testCreateJpg() throws IOException {
        byte[] jpgData = generator.createJpg();
        
        assertNotNull(jpgData, "JPG data should not be null");
        assertEquals(TEST_SIZE_1MB, jpgData.length, "JPG should be exactly 1MB");
        
        // Verify it's a valid JPG (starts with FF D8)
        assertTrue(isValidJpgFile(jpgData), "JPG should start with FF D8");
    }


    @Test
    @DisplayName("Test different target sizes")
    void testDifferentTargetSizes() throws IOException {
        long[] testSizes = {1024L, 10240L, 102400L}; // 1KB, 10KB, 100KB
        
        for (long size : testSizes) {
            Generator sizeGenerator = new Generator("size_test", size);
            byte[] data = sizeGenerator.createDocxWithContent();
            assertEquals(size, data.length, "Should create exactly " + size + " bytes");
        }
    }

    @Test
    @DisplayName("Test makeExactSize method")
    void testMakeExactSize() throws IOException {
        // Test smaller data gets padded
        byte[] smallData = "Hello".getBytes();
        byte[] result = generator.createPdf(); // This internally uses makeExactSize
        assertEquals(TEST_SIZE_1MB, result.length, "Should be padded to exact size");
        
        // The actual makeExactSize method is private, so we test it indirectly
        // through the public methods that use it
    }

    // Helper methods to validate file formats
    private boolean isValidZipFile(byte[] data) {
        if (data.length < 4) return false;
        // ZIP files start with PK (0x50 0x4B)
        return data[0] == 0x50 && data[1] == 0x4B;
    }

    private boolean isValidPdfFile(byte[] data) {
        if (data.length < 4) return false;
        // PDF files start with %PDF
        return data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46;
    }

    private boolean isValidJpgFile(byte[] data) {
        if (data.length < 2) return false;
        // JPG files start with 0xFF 0xD8
        return (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8;
    }

    @Test
    @DisplayName("Test file content uniqueness")
    void testContentUniqueness() throws IOException {
        // Generate two files with same parameters
        Generator gen1 = new Generator("test1", TEST_SIZE_1MB);
        Generator gen2 = new Generator("test2", TEST_SIZE_1MB);
        
        byte[] data1 = gen1.createDocxWithContent();
        byte[] data2 = gen2.createDocxWithContent();
        
        // They should be different due to UUID generation
        assertNotEquals(data1, data2, "Files should have different content due to UUIDs");
    }

    @Test
    @DisplayName("Test constructor with different parameters")
    void testConstructor() {
        String name = "custom_name";
        long size = 2048L;
        Generator customGen = new Generator(name, size);
        
        // We can't directly access private fields, but we can test behavior
        assertDoesNotThrow(() -> {
            byte[] data = customGen.createPdf();
            assertEquals(size, data.length, "Should use custom size");
        });
    }
}
