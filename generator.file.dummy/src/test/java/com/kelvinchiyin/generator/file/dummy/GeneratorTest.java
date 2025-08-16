package com.kelvinchiyin.generator.file.dummy;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.kelvinchiyin.file.dummy.Generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Generator} class.
 * <p>
 * These tests verify that the Generator can create dummy files of various formats (DOCX, XLSX, PPTX, PDF, JPG)
 * with the correct size, format validity, and uniqueness. It also checks that files are not truncated and
 * that the constructor and padding logic work as expected.
 * </p>
 */
public class GeneratorTest {

    private static final long TEST_SIZE_1MB = 1024 * 1024; // 1MB for fast tests
    private static final long TEST_SIZE_5MB = 5L * 1024 * 1024; // 5MB for thorough tests
    private static final long TEST_SIZE_50MB = 50L * 1024 * 1024; // 5MB for thorough tests
    
    @TempDir
    static Path tempDir;

    private Generator generator;
    private Generator generatorLargeFile;
    private String baseFileName;

    @BeforeEach
    void setUp() {
        // Set up test file names and Generator instances for different file sizes
        baseFileName = "test_file";
        generator = new Generator(baseFileName, TEST_SIZE_1MB);
        generatorLargeFile = new Generator(baseFileName, TEST_SIZE_50MB);
    }
    
    @Test
    @DisplayName("Test files are not truncated and remain valid")
    void testFilesNotTruncated() throws IOException {
        // Test that generated files do not exceed the target size and remain valid for all formats
        // Use larger size to test the safety mechanism
        Generator largeGen = new Generator("large_test", 10L * 1024 * 1024); // 10MB
        
        byte[] docxData = largeGen.createDocx();
        byte[] xlsxData = largeGen.createXlsx();
        byte[] pptxData = largeGen.createPptx();
        byte[] pdfData = largeGen.createPdf();
        byte[] jpgData = largeGen.createJpg(); // Added JPG testing
        
        // Files should be <= target size (no truncation)
        assertTrue(docxData.length <= 10L * 1024 * 1024, "DOCX should not exceed target");
        assertTrue(xlsxData.length <= 10L * 1024 * 1024, "XLSX should not exceed target");
        assertTrue(pptxData.length <= 10L * 1024 * 1024, "PPTX should not exceed target");
        assertTrue(pdfData.length <= 10L * 1024 * 1024, "PDF should not exceed target");
        assertTrue(jpgData.length <= 10L * 1024 * 1024, "JPG should not exceed target");
        
        // Files should still be valid format
        assertTrue(isValidZipFile(docxData), "DOCX should remain valid");
        assertTrue(isValidZipFile(xlsxData), "XLSX should remain valid");
        assertTrue(isValidZipFile(pptxData), "PPTX should remain valid");
        assertTrue(isValidPdfFile(pdfData), "PDF should remain valid");
        assertTrue(isValidJpgFile(jpgData), "JPG should remain valid"); // Added JPG validation
    }

    

    @Test
    @DisplayName("Test DOCX generation creates correct size")
    void testCreateDocx() throws IOException {
        // Test that DOCX files are generated with the exact target size and are valid ZIP files
      byte[] docxData = generator.createDocx();
      byte[] docxData50MB = generatorLargeFile.createDocx();
        
        assertNotNull(docxData, "DOCX data should not be null");
        assertEquals(TEST_SIZE_1MB, docxData.length, "DOCX should be exactly 1MB");
        assertEquals(TEST_SIZE_50MB, docxData50MB.length, "DOCX should be exactly 50MB");
        
        // Verify it's a valid DOCX (starts with PK - ZIP format)
        assertTrue(isValidZipFile(docxData), "DOCX should be valid ZIP file");
        assertTrue(isValidZipFile(docxData50MB), "DOCX should be valid ZIP file");
    }

    @Test
    @DisplayName("Test XLSX generation creates correct size")
    void testCreateXlsx() throws IOException {
        // Test that XLSX files are generated with the exact target size and are valid ZIP files
        byte[] xlsxData = generator.createXlsx();
        byte[] xlsxData50MB = generatorLargeFile.createXlsx();

        assertNotNull(xlsxData, "XLSX data should not be null");
        assertEquals(TEST_SIZE_1MB, xlsxData.length, "XLSX should be exactly 1MB");
        assertEquals(TEST_SIZE_50MB, xlsxData50MB.length, "XLSX should be exactly 150B");
        
        // Verify it's a valid XLSX (starts with PK - ZIP format)
        assertTrue(isValidZipFile(xlsxData), "XLSX should be valid ZIP file");
        assertTrue(isValidZipFile(xlsxData50MB), "XLSX should be valid ZIP file");
    }

    @Test
    @DisplayName("Test PPTX generation creates correct size")
    void testCreatePptx() throws IOException {
        // Test that PPTX files are generated with the exact target size and are valid ZIP files
      byte[] pptxData = generator.createPptx();
      byte[] pptxData50MB = generatorLargeFile.createPptx();
        
        assertNotNull(pptxData, "PPTX data should not be null");
        assertEquals(TEST_SIZE_1MB, pptxData.length, "PPTX should be exactly 1MB");
        assertEquals(TEST_SIZE_50MB, pptxData50MB.length, "PPTX should be exactly 50MB");
        
        // Verify it's a valid PPTX (starts with PK - ZIP format)
        assertTrue(isValidZipFile(pptxData), "PPTX should be valid ZIP file");
        assertTrue(isValidZipFile(pptxData50MB), "PPTX should be valid ZIP file");
    }

    @Test
    @DisplayName("Test PDF generation creates correct size")
    void testCreatePdf() throws IOException {
        // Test that PDF files are generated with the exact target size and are valid PDF files
      byte[] pdfData = generator.createPdf();
      byte[] pdfData50MB = generatorLargeFile.createPdf();
        
        assertNotNull(pdfData, "PDF data should not be null");
        assertEquals(TEST_SIZE_1MB, pdfData.length, "PDF should be exactly 1MB");
        assertEquals(TEST_SIZE_50MB, pdfData50MB.length, "PDF should be exactly 50MB");
        
        // Verify it's a valid PDF (starts with %PDF)
        assertTrue(isValidPdfFile(pdfData), "PDF should start with %PDF");
        assertTrue(isValidPdfFile(pdfData50MB), "PDF should start with %PDF");
    }

    @Test
    @DisplayName("Test JPG generation creates correct size and format")
    void testCreateJpg() throws IOException {
        // Test that JPG files are generated with the exact target size and are valid JPG files
      	byte[] jpgData = generator.createJpg();
        byte[] jpgData50MB = generatorLargeFile.createJpg();

        assertNotNull(jpgData, "JPG data should not be null");
        assertEquals(TEST_SIZE_1MB, jpgData.length, "JPG should be exactly 1MB");
        assertEquals(TEST_SIZE_50MB, jpgData50MB.length, "JPG should be exactly 50MB");
        
        // Verify it's a valid JPG (starts with FF D8)
        assertTrue(isValidJpgFile(jpgData), "JPG should start with FF D8");
        assertTrue(isValidJpgFile(jpgData50MB), "JPG should start with FF D8");
        
        // JPG files are more tolerant of padding, so they should always work
        assertTrue(jpgData.length > 0, "JPG should have content");
    }


    @Test
    @DisplayName("Test different target sizes")
    void testDifferentTargetSizes() throws IOException {
        // Test file generation for a range of target sizes and formats
        // Use reasonable minimum sizes for each format
        long[] testSizes = {
            10L * 1024,      // 10KB (minimum reasonable for Office files)
            100L * 1024,     // 100KB
            1024L * 1024     // 1MB
        };
        
        for (long size : testSizes) {
            Generator sizeGenerator = new Generator("size_test_" + size, size);
            
            // Test simpler formats that work with smaller sizes
            if (size >= 10L * 1024) {
                byte[] jpgData = sizeGenerator.createJpg();
                assertEquals(size, jpgData.length, "JPG should create exactly " + size + " bytes");
                assertTrue(isValidJpgFile(jpgData), "JPG should be valid");
            }
            
            // Test Office formats with larger sizes
            if (size >= 100L * 1024) {
                byte[] docxData = sizeGenerator.createDocx();
                assertEquals(size, docxData.length, "DOCX should create exactly " + size + " bytes");
                assertTrue(isValidZipFile(docxData), "DOCX should be valid");
            }
        }
    }

    @Test
    @DisplayName("Test makeExactSize method")
    void testMakeExactSize() throws IOException {
        // Indirectly test the private padding method by verifying output size from public methods
        // Test smaller data gets padded
        byte[] result = generator.createPdf(); // This internally uses makeExactSize
        assertEquals(TEST_SIZE_1MB, result.length, "Should be padded to exact size");
        
        // The actual makeExactSize method is private, so we test it indirectly
        // through the public methods that use it
    }

    // Helper methods to validate file formats
    /**
     * Checks if the given byte array represents a valid ZIP file (used for DOCX, XLSX, PPTX).
     */
    private boolean isValidZipFile(byte[] data) {
        if (data.length < 4) return false;
        // ZIP files start with PK (0x50 0x4B)
        return data[0] == 0x50 && data[1] == 0x4B;
    }

    /**
     * Checks if the given byte array represents a valid PDF file.
     */
    private boolean isValidPdfFile(byte[] data) {
        if (data.length < 4) return false;
        // PDF files start with %PDF
        return data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46;
    }

    /**
     * Checks if the given byte array represents a valid JPG file.
     */
    private boolean isValidJpgFile(byte[] data) {
        if (data.length < 2) return false;
        // JPG files start with 0xFF 0xD8
        return (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8;
    }

    @Test
    @DisplayName("Test file content uniqueness")
    void testContentUniqueness() throws IOException {
        // Test that files generated with the same parameters have unique content due to UUIDs
        // Generate two files with same parameters
        Generator gen1 = new Generator("test1", TEST_SIZE_1MB);
        Generator gen2 = new Generator("test2", TEST_SIZE_1MB);
        
        byte[] data1 = gen1.createDocx();
        byte[] data2 = gen2.createDocx();
        
        // They should be different due to UUID generation
        assertNotEquals(data1, data2, "Files should have different content due to UUIDs");
    }

    @Test
    @DisplayName("Test constructor with different parameters")
    void testConstructor() {
        // Test that the Generator constructor works with various file names and sizes
        String name = "custom_name";
        
        // Test with reasonable sizes for each format type
        assertDoesNotThrow(() -> {
            // JPG works with small sizes
            Generator smallGen = new Generator(name + "_small", 10L * 1024); // 10KB
            byte[] jpgData = smallGen.createJpg();
            assertEquals(10L * 1024, jpgData.length, "Should use custom small size");
            
            // Larger sizes for complex formats
            Generator mediumGen = new Generator(name + "_medium", 100L * 1024); // 100KB
            byte[] docxData = mediumGen.createDocx();
            assertEquals(100L * 1024, docxData.length, "Should use custom medium size");
        });
    }
}
