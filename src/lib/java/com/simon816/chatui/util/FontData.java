package com.simon816.chatui.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.BaseEncoding;
import scala.actors.threadpool.Arrays;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

public class FontData {

    private static final String ASCII_PNG_CHARS =
            "ÀÁÂÈÊËÍÓÔÕÚßãõğİ"
                    + "ıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
                    + " !\"#$%&\'()*+,-./"
                    + "0123456789:;<=>?"
                    + "@ABCDEFGHIJKLMNO"
                    + "PQRSTUVWXYZ[\\]^_"
                    + "`abcdefghijklmno"
                    + "pqrstuvwxyz{|}~\u0000"
                    + "ÇüéâäàåçêëèïîìÄÅ"
                    + "ÉæÆôöòûùÿÖÜø£Ø×ƒ"
                    + "áíóúñÑªº¿®¬½¼¡«»"
                    + "░▒▓│┤╡╢╖╕╣║╗╝╜╛┐"
                    + "└┴┬├─┼╞╟╚╔╩╦╠═╬╧"
                    + "╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"
                    + "αβΓπΣσμτΦΘΩδ∞∅∈∩"
                    + "≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000";

    public static final FontData VANILLA;

    private static final LoadingCache<String, FontData> dataCache = CacheBuilder.newBuilder().build(new CacheLoader<String, FontData>() {

        @Override
        public FontData load(String key) throws Exception {
            byte[] compressedData = Base64.getDecoder().decode(key);
            return new FontData(compressedData);
        }
    });

    static {
        try {
            VANILLA = new FontData(FontData.class.getResourceAsStream("ascii.png"), FontData.class.getResourceAsStream("glyph_sizes.bin"));
            dataCache.put(VANILLA.asciiToBase64(), VANILLA);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private final int[] asciiCharWidths = new int[ASCII_PNG_CHARS.length()];
    private final byte[] unicodeCharWidths;

    public FontData(InputStream asciiPng, InputStream glyphSizes) throws IOException {
        readAsciiPNG(asciiPng);
        this.unicodeCharWidths = new byte[65536];
        readGlyphSizes(glyphSizes);
    }

    public FontData(InputStream asciiPng) throws IOException {
        readAsciiPNG(asciiPng);
        // Use vanilla unicode chars
        this.unicodeCharWidths = VANILLA.unicodeCharWidths;
    }

    FontData(byte[] compressedData) {
        this.unicodeCharWidths = VANILLA.unicodeCharWidths;
        readCompressedAscii(compressedData);
    }

    private void readCompressedAscii(byte[] compressedData) {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);
        byte[] data = new byte[this.asciiCharWidths.length >>> 1];
        try {
            inflater.inflate(data);
        } catch (DataFormatException e) {
            throw Throwables.propagate(e);
        }
        for (int i = 0; i < data.length; i++) {
            this.asciiCharWidths[i << 1] = (data[i] >>> 4) & 0xF;
            this.asciiCharWidths[(i << 1) + 1] = data[i] & 0xF;
        }
    }

    // See FontRenderer
    private void readAsciiPNG(InputStream asciiPngStream) throws IOException {
        BufferedImage img = ImageIO.read(asciiPngStream);
        asciiPngStream.close();
        int width = img.getWidth();
        int height = img.getHeight();
        int[] imgData = new int[width * height];
        img.getRGB(0, 0, width, height, imgData, 0, width);
        int charH = height / 16;
        int charW = width / 16;
        double scaleFactor = 8.0D / charW;
        for (int idx = 0; idx < 256; ++idx) {
            if (idx == 32) {
                this.asciiCharWidths[idx] = 4;
                continue;
            }
            int col = idx % 16;
            int row = idx / 16;
            int offX;
            for (offX = charW - 1; offX >= 0; --offX) {
                int imgX = col * charW + offX;
                boolean isTransparent = true;
                for (int offY = 0; offY < charH && isTransparent; ++offY) {
                    int imgY = (row * charW + offY) * width;
                    if ((imgData[imgX + imgY] >> 24 & 255) != 0) {
                        isTransparent = false;
                    }
                }
                if (!isTransparent) {
                    break;
                }
            }
            ++offX;
            this.asciiCharWidths[idx] = (int) (0.5D + offX * scaleFactor) + 1;
        }
    }

    public String asciiToBase64() {
        byte[] data = new byte[this.asciiCharWidths.length >>> 1];
        for (int i = 0; i < this.asciiCharWidths.length; i += 2) {
            data[i >>> 1] = (byte) (((this.asciiCharWidths[i] & 0xF) << 4) | (this.asciiCharWidths[i + 1] & 0xF));
        }
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        byte[] deflated = new byte[this.asciiCharWidths.length];
        int len = deflater.deflate(deflated);
        return BaseEncoding.base64().encode(deflated, 0, len);
    }

    private void readGlyphSizes(InputStream glyphSizesStream) throws IOException {
        glyphSizesStream.read(this.unicodeCharWidths);
        glyphSizesStream.close();
    }

    public double getWidth(int codePoint, boolean isBold, boolean forceUnicode) {
        if (codePoint == '\n') {
            return 0;
        }
        if (codePoint == ' ') {
            return 4;
        }
        int nonUnicodeIdx = forceUnicode ? -1 : ASCII_PNG_CHARS.indexOf(codePoint);
        double width;
        if (codePoint > 0 && nonUnicodeIdx != -1) {
            width = this.asciiCharWidths[nonUnicodeIdx];
        } else {
            int squashedVal = this.unicodeCharWidths[codePoint] & 255;
            if (squashedVal == 0) {
                return 0;
            }
            int upper = squashedVal >>> 4;
            int lower = squashedVal & 15;
            width = ((lower + 1) - upper) / 2 + 1;
        }
        if (isBold && width > 0) {
            width += 1;
        }
        return width;
    }

    public int getWidthInt(int codePoint, boolean isBold, boolean forceUnicode) {
        return (int) Math.ceil(getWidth(codePoint, isBold, forceUnicode));
    }

    public static FontData fromString(String fontData) {
        if (fontData == null || fontData.isEmpty()) {
            return VANILLA;
        }
        try {
            return dataCache.getUnchecked(fontData);
        } catch (Exception e) {
            e.printStackTrace();
            return VANILLA;
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(this.asciiCharWidths);
    }

    public static void checkValid(String fontData) throws IllegalArgumentException {
        if (fontData == null || fontData.isEmpty()) {
            return;
        }
        // Throws IllegalArgumentException if invalid
        byte[] data = Base64.getDecoder().decode(fontData);
        checkArgument(data.length == ASCII_PNG_CHARS.length() >>> 1, "Length of font data not valid");
    }

}
