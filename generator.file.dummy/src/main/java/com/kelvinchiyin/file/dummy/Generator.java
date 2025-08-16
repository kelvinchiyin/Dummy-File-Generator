package com.kelvinchiyin.file.dummy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

public class Generator {
    private long targetSize;
    private String fileName;

    public Generator(String fileName, long targetSize) {
        this.fileName = fileName;
        this.targetSize = targetSize;
    }

    // Create file content and return byte array (exactly target size)
    public byte[] createDocxWithContent() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        
        // Scale content based on target size
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < Math.max(1000, targetSize / 10000); i++) {
            largeText.append("DOCX_Line_").append(i)
                    .append("_").append(UUID.randomUUID().toString().substring(0, 8))
                    .append("_FillerTextToIncreaseFileSize_");
        }

        int paragraphs = (int) Math.max(50, targetSize / 200000);
        for (int i = 0; i < paragraphs; i++) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(largeText.toString().substring(0, Math.min(largeText.length(), 20000)));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.write(baos);
        doc.close();

        return makeExactSize(baos.toByteArray());
    }

    public byte[] createXlsx() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Data");

        int rows = (int) Math.max(1000, targetSize / 5000);
        int cols = Math.min(15, Math.max(5, (int)(targetSize / 100000)));

        StringBuilder baseText = new StringBuilder();
        for (int i = 0; i < Math.max(1000, targetSize / 5000); i++) {
            baseText.append("XLSX_Data_").append(i)
                    .append("_").append(UUID.randomUUID().toString().substring(0, 8))
                    .append("_");
        }
        String largeText = baseText.toString();

        for (int row = 0; row < rows; row++) {
            XSSFRow r = sheet.createRow(row);
            for (int col = 0; col < cols; col++) {
                XSSFCell cell = r.createCell(col);
                cell.setCellValue(largeText.substring(0, Math.min(largeText.length(), 1000)) 
                                + "_R" + row + "_C" + col);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        wb.close();

        return makeExactSize(baos.toByteArray());
    }

    public byte[] createPptx() throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        
        int slides = (int) Math.max(20, targetSize / 300000);
        int textLength = (int) Math.max(2000, targetSize / 100);

        for (int i = 0; i < slides; i++) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox textBox = slide.createTextBox();
            textBox.setAnchor(new java.awt.Rectangle(20, 20, 900, 600));
            
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < textLength / 50; j++) {
                sb.append("Slide_").append(i)
                  .append("_Item_").append(j)
                  .append("_").append(UUID.randomUUID().toString().substring(0, 8))
                  .append("_");
            }
            
            XSLFTextParagraph p = textBox.addNewTextParagraph();
            XSLFTextRun run = p.addNewTextRun();
            run.setText(sb.toString());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppt.write(baos);
        ppt.close();

        return makeExactSize(baos.toByteArray());
    }

    public byte[] createPdf() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        StringBuilder textChunk = new StringBuilder();
        for (int i = 0; i < Math.max(1000, targetSize / 2000); i++) {
            textChunk.append("PDF_Chunk_").append(i)
                     .append("_").append(UUID.randomUUID().toString().substring(0, 6))
                     .append("_FillerText_");
        }
        String chunk = textChunk.toString();

        int paragraphs = (int) Math.max(30, targetSize / 150000);
        for (int i = 0; i < paragraphs; i++) {
            document.add(new Paragraph(chunk));
        }

        document.close();

        return makeExactSize(baos.toByteArray());
    }

    public byte[] createJpg() throws IOException {
        int dimension = (int) Math.sqrt(targetSize / 3);
        dimension = Math.max(1000, Math.min(dimension, 15000));

        BufferedImage img = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, dimension, dimension);
        g.setColor(java.awt.Color.BLUE);
        g.drawString("Test Image", 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        baos.close();

        return makeExactSize(baos.toByteArray());
    }

    // Ensure byte array is exactly target size
    private byte[] makeExactSize(byte[] data) {
        if (data.length == targetSize) {
            return data;
        } else if (data.length > targetSize) {
            // Truncate
            byte[] result = new byte[(int) targetSize];
            System.arraycopy(data, 0, result, 0, (int) targetSize);
            System.out.println("WARNING: Content truncated from " + data.length + " to " + targetSize + " bytes");
            return result;
        } else {
            // Pad
            byte[] result = new byte[(int) targetSize];
            System.arraycopy(data, 0, result, 0, data.length);
            // Rest is already 0-filled
            return result;
        }
    }

    // Write byte array to file
    public void writeFile(String filePath, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }
        System.out.println(filePath + ": " + new File(filePath).length() + " bytes");
    }

    // Convenience methods that create and write
    public void createAndWriteDocx() throws IOException {
        byte[] data = createDocxWithContent();
        writeFile(fileName + ".docx", data);
    }

    public void createAndWriteXlsx() throws IOException {
        byte[] data = createXlsx();
        writeFile(fileName + ".xlsx", data);
    }

    public void createAndWritePptx() throws IOException {
        byte[] data = createPptx();
        writeFile(fileName + ".pptx", data);
    }

    public void createAndWritePdf() throws IOException {
        byte[] data = createPdf();
        writeFile(fileName + ".pdf", data);
    }

    public void createAndWriteJpg() throws IOException {
        byte[] data = createJpg();
        writeFile(fileName + ".jpg", data);
    }
}