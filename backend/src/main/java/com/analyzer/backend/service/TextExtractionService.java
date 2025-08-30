package com.analyzer.backend.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TextExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(TextExtractionService.class);

    // Provide a default value for tessDataPath in case it's not set in application.properties
    // This value is a common location on Linux/macOS, but will be overridden by properties.
    @Value("${tesseract.datapath:/usr/local/share/tessdata}")
    private String tessDataPath;

    @Value("${tesseract.language:eng}")
    private String tessLanguage;
    
    // Flag to ensure initialization logic runs only once
    private boolean initialized = false;

    // Use a method for lazy initialization to ensure @Value properties are injected
    private void initializeIfNeeded() {
        if (!initialized) {
            init();
            initialized = true;
        }
    }

    // Initialization logic for Tesseract data path validation
    private void init() {
        logger.info("TextExtractionService initializing with configured tesseract.datapath: '{}', tesseract.language: '{}'",
                    tessDataPath, tessLanguage);
        
        File tessdataPathFile = new File(tessDataPath);
        
        // Check if the configured path exists. If not, try common alternatives.
        if (!tessdataPathFile.exists()) {
            logger.warn("Configured Tesseract data path '{}' does not exist. Attempting to find alternatives.", tessDataPath);
            String[] commonPaths = {
                "/usr/local/share/tessdata", // Common for standard Linux/macOS install
                "/opt/homebrew/share/tessdata", // Common for Homebrew on macOS (Intel/ARM)
                "/usr/share/tessdata",        // Another common Linux path
                System.getProperty("user.home") + "/tessdata" // User home directory
            };
            
            for (String path : commonPaths) {
                File altPath = new File(path);
                if (altPath.exists() && altPath.isDirectory()) {
                    logger.info("Found alternative tessdata path: {}. Using this path.", path);
                    tessDataPath = path;
                    tessdataPathFile = altPath;
                    break;
                }
            }
        }
        
        // Final check on the determined tessDataPath
        if (!tessdataPathFile.exists() || !tessdataPathFile.isDirectory()) {
            logger.error("Tesseract data path '{}' is still invalid or not a directory. Please ensure Tesseract is installed and tessdata is accessible.", tessDataPath);
        } else {
            File traineddataFile = new File(tessDataPath, tessLanguage + ".traineddata");
            if (!traineddataFile.exists()) {
                logger.error("Tesseract traineddata file '{}' not found in '{}'. Please ensure it's downloaded and placed correctly.",
                            traineddataFile.getName(), tessDataPath);
                logger.error("You can usually download language data from: https://github.com/tesseract-ocr/tessdata");
            } else {
                logger.info("Successfully located Tesseract traineddata file: '{}'", traineddataFile.getAbsolutePath());
            }
        }
    }

    public static boolean isPdf(String filename, String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("pdf")) return true;
        if (filename == null) return false;
        return filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    public static boolean isImage(String filename, String contentType) {
        String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (ct.startsWith("image/")) return true;

        if (filename == null) return false;
        String f = filename.toLowerCase(Locale.ROOT);
        return f.endsWith(".png") || f.endsWith(".jpg") || f.endsWith(".jpeg") || 
               f.endsWith(".bmp") || f.endsWith(".tif") || f.endsWith(".tiff");
    }

    public ExtractResult extractText(MultipartFile file) throws Exception {
        // Ensure initialization runs before any extraction attempt
        initializeIfNeeded();
        
        long start = System.currentTimeMillis();
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();

        logger.info("Attempting to extract text from file: '{}' (Type: '{}')", originalName, contentType);

        if (isPdf(originalName, contentType)) {
            logger.info("File identified as PDF.");
            ExtractResult result = extractFromPdf(file.getInputStream());
            result.sourceType = "PDF";
            result.durationMs = System.currentTimeMillis() - start;
            logger.info("PDF extraction completed in {} ms. Extracted text length: {}", 
                       result.durationMs, result.text.length());
            return result;
        }

        if (isImage(originalName, contentType)) {
            logger.info("File identified as Image.");
            ExtractResult result = extractFromImage(file);
            result.sourceType = "IMAGE";
            result.durationMs = System.currentTimeMillis() - start;
            logger.info("Image extraction completed in {} ms. Extracted text length: {}", 
                       result.durationMs, result.text.length());
            return result;
        }

        logger.warn("Unsupported file type for file: '{}' (ContentType: '{}')", originalName, contentType);
        throw new IllegalArgumentException("Unsupported file type. Please upload a PDF or an image (PNG/JPG/BMP/TIFF).");
    }

    private ExtractResult extractFromPdf(InputStream in) throws IOException {
        try (PDDocument document = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            ExtractResult result = new ExtractResult();
            result.text = text == null ? "" : text.trim();
            result.pageCount = document.getNumberOfPages();
            logger.debug("PDF Text extracted (first 100 chars): {}", 
                        result.text.substring(0, Math.min(result.text.length(), 100)));
            return result;
        } catch (IOException e) {
            logger.error("Error during PDF text extraction: {}", e.getMessage(), e);
            throw e;
        }
    }
    private ExtractResult extractFromImage(MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename();
        Path tempFilePath = null;

        try {
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            } else {
                extension = ".png";
            }

            tempFilePath = Files.createTempFile("ocr-", extension);
            file.transferTo(tempFilePath.toFile());
            logger.info("Saved image to temporary file: {}", tempFilePath.toAbsolutePath());

            // ✅ Always read as BufferedImage
            BufferedImage bufferedImage = ImageIO.read(tempFilePath.toFile());
            if (bufferedImage == null) {
                throw new Exception("Unable to read the image file. Unsupported format: " + originalName);
            }
            logger.info("Image loaded successfully. Dimensions: {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());

            ITesseract tesseract = buildTesseract();
            logger.info("Tesseract configured. Datapath: '{}', Language: '{}'", tessDataPath, tessLanguage);

            // ✅ Use BufferedImage instead of File
            String text = tesseract.doOCR(bufferedImage);
            logger.info("OCR completed for '{}'. Extracted length: {}", originalName, text != null ? text.length() : 0);

            ExtractResult result = new ExtractResult();
            result.text = text == null ? "" : text.trim();
            result.sourceType = "IMAGE";
            return result;

        } catch (TesseractException e) {
            logger.error("Tesseract OCR failed for '{}': {}", originalName, e.getMessage(), e);
            throw new Exception("OCR processing failed: " + e.getMessage(), e);
        } finally {
            if (tempFilePath != null && Files.exists(tempFilePath)) {
                try {
                    Files.delete(tempFilePath);
                    logger.debug("Deleted temporary file: {}", tempFilePath.toAbsolutePath());
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary file '{}': {}", tempFilePath.toAbsolutePath(), e.getMessage());
                }
            }
        }
    }

    private ITesseract buildTesseract() {
        // Make sure Tess4J knows where to find native lib
        System.setProperty("jna.library.path", "/opt/homebrew/opt/tesseract/lib");

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/share/tessdata");
        tesseract.setLanguage("eng");

        return tesseract;
    }

    // Internal result holder class (lombok could simplify this but current approach is fine)
    public static class ExtractResult {
        public String sourceType;
        public String text;
        public Integer pageCount; 
        public long durationMs;
    }
}