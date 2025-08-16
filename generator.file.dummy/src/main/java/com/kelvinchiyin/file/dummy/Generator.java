package com.kelvinchiyin.file.dummy;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

/**
 * Generator is a utility class for creating dummy files of various formats (DOCX, XLSX, PPTX, PDF, JPG)
 * with a specified target size. It supports generating file content, padding to exact size, and writing files.
 * <p>
 * Supported formats: DOCX, XLSX, PPTX, PDF, JPG
 * </p>
 * Usage example:
 * <pre>
 *     Generator gen = new Generator("output", 1048576); // 1MB
 *     gen.createAndWriteDocx();
 * </pre>
 */
public class Generator {
	private long targetSize;
	private String fileName;

	/**
	 * Constructs a Generator with the given file name and target size in bytes.
	 * @param fileName the base name for output files (without extension)
	 * @param targetSize the desired file size in bytes
	 */
	public Generator(String fileName, long targetSize) {
		this.fileName = fileName;
		this.targetSize = targetSize;
	}

	/**
	 * Creates a DOCX file content as a byte array, padded to the target size.
	 * @return byte array representing the DOCX file
	 * @throws IOException if an I/O error occurs
	 */
	public byte[] createDocx() throws IOException {
		XWPFDocument doc = new XWPFDocument();
		ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();

		// Create large text chunk
		StringBuilder chunk = new StringBuilder();
		for (int i = 0; i < 5000; i++) {
			chunk.append("DOCX_Chunk_").append(i).append("_").append(UUID.randomUUID().toString().substring(0, 8))
					.append("_");
		}
		String largeChunk = chunk.toString();

		long safeLimit = (long) (targetSize * 0.95); // Stop at 95% to be safe
		int paragraphCount = 0;

		// Keep adding content until we're close to target
		while (paragraphCount < 1000) { // Safety limit
			XWPFParagraph p = doc.createParagraph();
			XWPFRun run = p.createRun();
			run.setText(largeChunk.substring(0, Math.min(largeChunk.length(), 10000)));

			paragraphCount++;

			// Check size periodically
			if (paragraphCount % 20 == 0) {
				tempBaos.reset();
				doc.write(tempBaos);
				if (tempBaos.size() > safeLimit) {
					break;
				}
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.write(baos);
		doc.close();

		return padToExactSize(baos.toByteArray());
	}

	/**
	 * Creates an XLSX file content as a byte array, padded to the target size.
	 * @return byte array representing the XLSX file
	 * @throws IOException if an I/O error occurs
	 */
	public byte[] createXlsx() throws IOException {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("Data");

		// Very conservative approach - XLSX has huge overhead
		long targetContentSize = (long) (targetSize * 0.7); // Aim for 70% to account for overhead

		// Minimal unique content
		String baseContent = "XLSX_Data_" + UUID.randomUUID().toString().substring(0, 6);

		int rowCount = 0;
		int colCount = 5; // Fewer columns
		ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
		long currentSize = 0;

		// Add rows until we're close to target content size
		while (rowCount < 10000 && currentSize < targetContentSize) { // Safety limit
			XSSFRow row = sheet.createRow(rowCount);
			for (int col = 0; col < colCount; col++) {
				XSSFCell cell = row.createCell(col);
				cell.setCellValue(baseContent + "_R" + rowCount + "_C" + col);
			}

			rowCount++;

			// Check size every 100 rows (XLSX is expensive to check)
			if (rowCount % 100 == 0) {
				tempBaos.reset();
				wb.write(tempBaos);
				currentSize = tempBaos.size();

				// If we're getting close, stop
				if (currentSize > targetContentSize * 0.9) {
					break;
				}
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		wb.close();

		return padToExactSize(baos.toByteArray());
	}

	/**
	 * Creates a PPTX file content as a byte array, padded to the target size.
	 * @return byte array representing the PPTX file
	 * @throws IOException if an I/O error occurs
	 */
	public byte[] createPptx() throws IOException {
		XMLSlideShow ppt = new XMLSlideShow();

		// Be very conservative with PPTX since it has high overhead
		int maxSlides = Math.max(5, (int) (targetSize / 300000));
		int textLength = Math.max(1000, (int) (targetSize / 500));

		// Create content
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < textLength / 20; i++) {
			content.append("PPTX_").append(i).append("_").append(UUID.randomUUID().toString().substring(0, 4)).append("_");
		}
		String slideText = content.toString();

		// Add limited number of slides
		for (int i = 0; i < Math.min(100, maxSlides); i++) {
			XSLFSlide slide = ppt.createSlide();
			XSLFTextBox textBox = slide.createTextBox();
			textBox.setAnchor(new java.awt.Rectangle(20, 20, 800, 500));

			XSLFTextParagraph p = textBox.addNewTextParagraph();
			XSLFTextRun run = p.addNewTextRun();
			run.setText(slideText);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ppt.write(baos);
		ppt.close();

		return padToExactSize(baos.toByteArray());
	}

	/**
	 * Creates a PDF file content as a byte array, padded to the target size.
	 * @return byte array representing the PDF file
	 * @throws IOException if an I/O error occurs
	 */
	public byte[] createPdf() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(baos);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf);

		// Create content chunk
		StringBuilder chunk = new StringBuilder();
		for (int i = 0; i < 2000; i++) {
			chunk.append("PDF_Chunk_").append(i).append("_").append(UUID.randomUUID().toString().substring(0, 6))
					.append("_FillerText_");
		}
		String content = chunk.toString();

		// Add moderate number of paragraphs to stay under target
		int maxParagraphs = (int) Math.max(50, targetSize / 50000);
		for (int i = 0; i < maxParagraphs; i++) {
			document.add(new Paragraph(content));

			// Periodically check if we're getting close to target
			if (i % 30 == 0 && i > 0) {
				// Estimate current size without closing document
				long estimatedSize = (long) baos.size() + (baos.size() * i / 30); // Rough estimation
				if (estimatedSize > targetSize * 0.9) {
					break; // Stop before getting too close
				}
			}
		}

		document.close();
		return padToExactSize(baos.toByteArray());
	}

	/**
	 * Creates a JPG image as a byte array, padded to the target size.
	 * For very small sizes, creates a minimal valid JPG.
	 * @return byte array representing the JPG image
	 * @throws IOException if an I/O error occurs
	 */
	public byte[] createJpg() throws IOException {
		if (targetSize < 1024) {
			// For very small sizes, create minimal content
			byte[] minimalJpg = createMinimalJpg();
			return padToExactSize(minimalJpg);
		}

		// Calculate image dimensions based on target size
		// Rough estimate: width * height * 3 bytes (RGB) ≈ target size
		// But JPEG compression makes this tricky, so we estimate smaller
		long pixelsNeeded = targetSize / 6; // Conservative estimate due to compression
		int dimension = (int) Math.sqrt(pixelsNeeded);
		dimension = Math.max(100, Math.min(dimension, 5000)); // Reasonable limits

		BufferedImage img = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = img.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, dimension, dimension);
		g.setColor(java.awt.Color.BLUE);
		g.drawString("Test", 10, 20);
		g.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Use lower quality for better size control
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		if (param.canWriteCompressed()) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(0.7f); // Lower quality = smaller size
		}

		writer.setOutput(ImageIO.createImageOutputStream(baos));
		writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
		writer.dispose();

		return padToExactSize(baos.toByteArray());
	}

	/**
	 * Creates a minimal valid JPG for very small target sizes.
	 * @return byte array of a minimal JPG
	 * @throws IOException if an I/O error occurs
	 */
	private byte[] createMinimalJpg() throws IOException {
		// Create a 1x1 pixel image
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = img.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, 1, 1);
		g.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		return baos.toByteArray();
	}

	/**
	 * Pads the given byte array to the exact target size with zeros if needed.
	 * Never truncates to avoid file corruption.
	 * @param data the original file data
	 * @return a byte array of exact target size
	 */
	private byte[] padToExactSize(byte[] data) {
		if (data.length > targetSize) {
			System.out.println("WARNING: Generated " + data.length + " bytes, target is " + targetSize);
			System.out.println("File would be truncated and become corrupted - returning as-is");
			return data; // Don't truncate, return oversized file
		} else if (data.length < targetSize) {
			// Pad with zeros
			byte[] result = new byte[(int) targetSize];
			System.arraycopy(data, 0, result, 0, data.length);
			return result;
		} else {
			return data; // Already exact size
		}
	}

	/**
	 * Writes the given byte array to a file with the specified filename.
	 * @param filename the output file name
	 * @param data the file data to write
	 * @throws IOException if an I/O error occurs
	 */
	public void writeFile(String filename, byte[] data) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(filename)) {
			fos.write(data);
		}
		System.out.println(filename + ": " + new File(filename).length() + " bytes");
	}

	/**
	 * Creates and writes a DOCX file to disk with the target size.
	 * @throws IOException if an I/O error occurs
	 */
	public void createAndWriteDocx() throws IOException {
		byte[] data = createDocx();
		if (data.length <= targetSize) {
			writeFile(fileName + ".docx", data);
		} else {
			System.out.println("DOCX file too large (" + data.length + " bytes), not writing to avoid corruption");
		}
	}

	/**
	 * Creates and writes an XLSX file to disk with the target size.
	 * @throws IOException if an I/O error occurs
	 */
	public void createAndWriteXlsx() throws IOException {
		byte[] data = createXlsx();
		if (data.length <= targetSize) {
			writeFile(fileName + ".xlsx", data);
		} else {
			System.out.println("XLSX file too large (" + data.length + " bytes), not writing to avoid corruption");
		}
	}

	/**
	 * Creates and writes a PPTX file to disk with the target size.
	 * @throws IOException if an I/O error occurs
	 */
	public void createAndWritePptx() throws IOException {
		byte[] data = createPptx();
		if (data.length <= targetSize) {
			writeFile(fileName + ".pptx", data);
		} else {
			System.out.println("PPTX file too large (" + data.length + " bytes), not writing to avoid corruption");
		}
	}

	/**
	 * Creates and writes a PDF file to disk with the target size.
	 * @throws IOException if an I/O error occurs
	 */
	public void createAndWritePdf() throws IOException {
		byte[] data = createPdf();
		if (data.length <= targetSize) {
			writeFile(fileName + ".pdf", data);
		} else {
			System.out.println("PDF file too large (" + data.length + " bytes), not writing to avoid corruption");
		}
	}

	/**
	 * Creates and writes a JPG file to disk with the target size.
	 * @throws IOException if an I/O error occurs
	 */
	public void createAndWriteJpg() throws IOException {
		byte[] data = createJpg();
		writeFile(fileName + ".jpg", data); // JPG is less sensitive to padding
	}

	/**
	 * Sets a new target size for file generation.
	 * @param targetSize the new target size in bytes
	 */
	public void setTargetSize(long targetSize) {
		this.targetSize = targetSize;
	}
}
