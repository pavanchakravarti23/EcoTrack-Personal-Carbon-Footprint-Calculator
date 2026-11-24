package com.ecotrack.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

/**
 * Self-contained pure-Java PDF 1.4 generator supporting multi-page layouts,
 * standard Type1 Helvetica typography, colored rectangles, rules, and Flate-compressed
 * RGB chart images without requiring any external PDF library or internet access.
 */
public class PdfDocumentWriter {

    public static final float PAGE_WIDTH = 595.0f;   // A4 portrait
    public static final float PAGE_HEIGHT = 842.0f;

    private static class PageData {
        final StringBuilder stream = new StringBuilder();
        final List<ImageResource> images = new ArrayList<>();
    }

    private static class ImageResource {
        final String name;
        final int width;
        final int height;
        final byte[] compressedRgb;

        ImageResource(String name, int width, int height, byte[] compressedRgb) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.compressedRgb = compressedRgb;
        }
    }

    private final List<PageData> pages = new ArrayList<>();
    private PageData currentPage;
    private int imageCounter = 0;

    public PdfDocumentWriter() {
        newPage();
    }

    public final void newPage() {
        currentPage = new PageData();
        pages.add(currentPage);
    }

    public void fillRect(float x, float yFromTop, float w, float h, Color color) {
        float y = PAGE_HEIGHT - yFromTop - h;
        setFillColor(color);
        currentPage.stream.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f %.2f re f\n", x, y, w, h));
    }

    public void strokeRect(float x, float yFromTop, float w, float h, Color color, float lineWidth) {
        float y = PAGE_HEIGHT - yFromTop - h;
        setStrokeColor(color);
        currentPage.stream.append(String.format(java.util.Locale.US, "%.2f w %.2f %.2f %.2f %.2f re S\n", lineWidth, x, y, w, h));
    }

    public void drawLine(float x1, float y1FromTop, float x2, float y2FromTop, Color color, float lineWidth) {
        float y1 = PAGE_HEIGHT - y1FromTop;
        float y2 = PAGE_HEIGHT - y2FromTop;
        setStrokeColor(color);
        currentPage.stream.append(String.format(java.util.Locale.US,
                "%.2f w %.2f %.2f m %.2f %.2f l S\n", lineWidth, x1, y1, x2, y2));
    }

    public void drawText(String text, float x, float yFromTop, float fontSize, boolean bold, Color color) {
        String fontKey = bold ? "/F2" : "/F1";
        float y = PAGE_HEIGHT - yFromTop;
        setFillColor(color);
        currentPage.stream.append("BT\n");
        currentPage.stream.append(String.format(java.util.Locale.US, "%s %.2f Tf\n", fontKey, fontSize));
        currentPage.stream.append(String.format(java.util.Locale.US, "1 0 0 1 %.2f %.2f Tm\n", x, y));
        currentPage.stream.append("(").append(escapePdfText(sanitizeAscii(text))).append(") Tj\n");
        currentPage.stream.append("ET\n");
    }

    /**
     * Draws wrapped paragraph text and returns the next Y coordinate (from top).
     */
    public float drawWrappedText(
            String text,
            float x,
            float yFromTop,
            float maxWidth,
            float fontSize,
            float lineHeight,
            boolean bold,
            Color color,
            float bottomMargin) {
        if (text == null || text.isEmpty()) {
            return yFromTop;
        }
        int approxCharsPerLine = Math.max(20, (int) (maxWidth / (fontSize * 0.52f)));
        String[] paragraphs = text.split("\\R");
        float y = yFromTop;
        for (String para : paragraphs) {
            List<String> lines = wrapParagraph(para.trim(), approxCharsPerLine);
            if (lines.isEmpty()) {
                y += lineHeight * 0.6f;
                continue;
            }
            for (String line : lines) {
                if (y > PAGE_HEIGHT - bottomMargin) {
                    newPage();
                    y = 55.0f;
                }
                drawText(line, x, y, fontSize, bold, color);
                y += lineHeight;
            }
        }
        return y;
    }

    public void drawImage(BufferedImage image, float x, float yFromTop, float width, float height) throws IOException {
        if (image == null) {
            return;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        byte[] rawRgb = new byte[w * h * 3];
        int idx = 0;
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int argb = image.getRGB(px, py);
                rawRgb[idx++] = (byte) ((argb >> 16) & 0xFF);
                rawRgb[idx++] = (byte) ((argb >> 8) & 0xFF);
                rawRgb[idx++] = (byte) (argb & 0xFF);
            }
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
            deflater.write(rawRgb);
        }
        imageCounter++;
        String imgName = "Im" + imageCounter;
        currentPage.images.add(new ImageResource(imgName, w, h, compressed.toByteArray()));

        float y = PAGE_HEIGHT - yFromTop - height;
        currentPage.stream.append("q\n");
        currentPage.stream.append(String.format(java.util.Locale.US,
                "%.2f 0 0 %.2f %.2f %.2f cm\n", width, height, x, y));
        currentPage.stream.append("/").append(imgName).append(" Do\n");
        currentPage.stream.append("Q\n");
    }

    public void saveToFile(File file) throws IOException {
        List<byte[]> objects = new ArrayList<>();
        // Object 1: Catalog, Object 2: Pages, Object 3: F1 Helvetica, Object 4: F2 Helvetica-Bold
        objects.add(null); // placeholder 1
        objects.add(null); // placeholder 2
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>".getBytes(StandardCharsets.ISO_8859_1));

        List<Integer> pageObjectIds = new ArrayList<>();

        for (PageData page : pages) {
            StringBuilder xObjectDict = new StringBuilder();
            if (!page.images.isEmpty()) {
                xObjectDict.append(" /XObject << ");
                for (ImageResource img : page.images) {
                    int imgObjId = objects.size() + 1;
                    ByteArrayOutputStream imgObj = new ByteArrayOutputStream();
                    String header = String.format(java.util.Locale.US,
                            "<< /Type /XObject /Subtype /Image /Width %d /Height %d "
                                    + "/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode /Length %d >>\nstream\n",
                            img.width, img.height, img.compressedRgb.length);
                    imgObj.write(header.getBytes(StandardCharsets.ISO_8859_1));
                    imgObj.write(img.compressedRgb);
                    imgObj.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
                    objects.add(imgObj.toByteArray());
                    xObjectDict.append("/").append(img.name).append(" ").append(imgObjId).append(" 0 R ");
                }
                xObjectDict.append(">>");
            }

            byte[] contentBytes = page.stream.toString().getBytes(StandardCharsets.ISO_8859_1);
            int contentObjId = objects.size() + 1;
            ByteArrayOutputStream contentObj = new ByteArrayOutputStream();
            String contentHeader = "<< /Length " + contentBytes.length + " >>\nstream\n";
            contentObj.write(contentHeader.getBytes(StandardCharsets.ISO_8859_1));
            contentObj.write(contentBytes);
            contentObj.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
            objects.add(contentObj.toByteArray());

            int pageObjId = objects.size() + 1;
            String pageDict = String.format(java.util.Locale.US,
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %.0f %.0f] "
                            + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >>%s >> /Contents %d 0 R >>",
                    PAGE_WIDTH, PAGE_HEIGHT, xObjectDict, contentObjId);
            objects.add(pageDict.getBytes(StandardCharsets.ISO_8859_1));
            pageObjectIds.add(pageObjId);
        }

        StringBuilder kids = new StringBuilder("[");
        for (int id : pageObjectIds) {
            kids.append(id).append(" 0 R ");
        }
        kids.append("]");
        objects.set(0, "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.set(1, ("<< /Type /Pages /Kids " + kids + " /Count " + pageObjectIds.size() + " >>")
                .getBytes(StandardCharsets.ISO_8859_1));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));
            List<Long> offsets = new ArrayList<>();
            long position = "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1).length;

            for (int i = 0; i < objects.size(); i++) {
                offsets.add(position);
                byte[] objHeader = ((i + 1) + " 0 obj\n").getBytes(StandardCharsets.ISO_8859_1);
                byte[] objBody = objects.get(i);
                byte[] objFooter = "\nendobj\n".getBytes(StandardCharsets.ISO_8859_1);
                fos.write(objHeader);
                fos.write(objBody);
                fos.write(objFooter);
                position += objHeader.length + objBody.length + objFooter.length;
            }

            long xrefStart = position;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n");
            xref.append("0 ").append(objects.size() + 1).append("\n");
            xref.append("0000000000 65535 f \n");
            for (long off : offsets) {
                xref.append(String.format(java.util.Locale.US, "%010d 00000 n \n", off));
            }
            xref.append("trailer\n");
            xref.append("<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
            xref.append("startxref\n").append(xrefStart).append("\n%%EOF\n");
            fos.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
        }
    }

    private void setFillColor(Color c) {
        currentPage.stream.append(String.format(java.util.Locale.US,
                "%.3f %.3f %.3f rg\n", c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f));
    }

    private void setStrokeColor(Color c) {
        currentPage.stream.append(String.format(java.util.Locale.US,
                "%.3f %.3f %.3f RG\n", c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f));
    }

    private static List<String> wrapParagraph(String paragraph, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (paragraph.isEmpty()) {
            return lines;
        }
        String[] words = paragraph.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() == 0) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= maxChars) {
                line.append(' ').append(word);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static String sanitizeAscii(String s) {
        if (s == null) {
            return "";
        }
        String normalized = s
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replace('\u2022', '*')
                .replace('\u2019', '\'')
                .replace('\u201C', '"')
                .replace('\u201D', '"')
                .replace("\u2082", "2");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch >= 32 && ch <= 126) {
                sb.append(ch);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private static String escapePdfText(String s) {
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
