package com.simon816.chatui.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.locale.Locales;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

public class TextUtils {

    // See FontRenderer

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

    private static final int[] ASCII_PNG_CHAR_WIDTHS = new int[ASCII_PNG_CHARS.length()];
    private static final byte[] UNICODE_CHAR_WIDTHS = new byte[65536];

    static {
        try {
            computeCharWidths();
            InputStream gStream = TextUtils.class.getResourceAsStream("glyph_sizes.bin");
            gStream.read(UNICODE_CHAR_WIDTHS);
            gStream.close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void computeCharWidths() throws IOException {
        InputStream iStream = TextUtils.class.getResourceAsStream("ascii.png");
        BufferedImage img = ImageIO.read(iStream);
        iStream.close();
        int width = img.getWidth();
        int height = img.getHeight();
        int[] imgData = new int[width * height];
        img.getRGB(0, 0, width, height, imgData, 0, width);
        int charH = height / 16;
        int charW = width / 16;
        float lvt_9_1_ = 8.0F / (float) charW;
        for (int idx = 0; idx < 256; ++idx) {
            if (idx == 32) {
                ASCII_PNG_CHAR_WIDTHS[idx] = 4;
                continue;
            }
            int col = idx % 16;
            int row = idx / 16;
            int offX;
            for (offX = charW - 1; offX >= 0; --offX) {
                int imgX = col * charW + offX;
                boolean hasValue = true;
                for (int offY = 0; offY < charH && hasValue; ++offY) {
                    int imgY = (row * charW + offY) * width;
                    if ((imgData[imgX + imgY] >> 24 & 255) != 0) {
                        hasValue = false;
                    }
                }
                if (!hasValue) {
                    break;
                }
            }
            ++offX;
            ASCII_PNG_CHAR_WIDTHS[idx] = (int) (0.5D + (double) ((float) offX * lvt_9_1_)) + 1;
        }
    }

    public static double getWidth(int codePoint, boolean isBold, boolean forceUnicode) {
        if (codePoint == '\n') {
            return 0;
        }
        int nonUnicodeIdx = forceUnicode ? -1 : ASCII_PNG_CHARS.indexOf(codePoint);
        double width;
        if (codePoint > 0 && nonUnicodeIdx != -1) {
            width = ASCII_PNG_CHAR_WIDTHS[nonUnicodeIdx];
        } else {
            int squashedVal = UNICODE_CHAR_WIDTHS[codePoint] & 255;
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

    public static int getStringWidth(String text, boolean isBold, boolean forceUnicode) {
        double width = 0;
        for (int i = 0; i < text.length(); ++i) {
            width += getWidth(text.codePointAt(i), isBold, forceUnicode);
        }
        return (int) Math.ceil(width);
    }

    public static int getWidth(char c, boolean isBold, boolean forceUnicode) {
        return (int) Math.ceil(getWidth((int) c, isBold, forceUnicode));
    }

    public static int getWidth(Text text, boolean forceUnicode) {
        return (int) Math.ceil(getWidth0(text, false, forceUnicode));
    }

    private static double getWidth0(Text text, boolean parentIsbold, boolean forceUnicode) {
        double width = 0;
        boolean thisIsBold = text.getStyle().isBold().orElse(parentIsbold);
        if (text instanceof LiteralText) {
            String content = ((LiteralText) text).getContent();
            width += getStringWidth(content, thisIsBold, forceUnicode);
            for (Text child : text.getChildren()) {
                width += getWidth0(child, thisIsBold, forceUnicode);
            }
        } else {
            width += getStringWidth(text.toPlain(), thisIsBold, forceUnicode);
        }
        return width;
    }

    public static List<Text> splitLines(Text original, int maxWidth, boolean forceUnicode) {
        return splitLines(original, maxWidth, Locales.DEFAULT, forceUnicode);
    }

    public static List<String> splitLines(String original, int maxWidth, boolean forceUnicode) {
        List<String> output = Lists.newArrayList();
        TextSplitter.splitLines(original, output, maxWidth, forceUnicode);
        return output;
    }

    public static List<Text> splitLines(Text original, int maxWidth, Locale locale, boolean forceUnicode) {
        List<Text> output = Lists.newArrayList();
        TextSplitter.splitLines(original, output, maxWidth, locale, forceUnicode);
        return output;
    }

    private static final Char2ObjectMap<Text> charTextCache = new Char2ObjectOpenHashMap<>();

    public static Text charCache(char c) {
        Text t = charTextCache.get(c);
        if (t == null) {
            charTextCache.put(c, t = Text.of(c));
        }
        return t;
    }

    // Space is same width for both unicode and non-unicode
    private static final int SPACE_WIDTH = getWidth(' ', false, false);
    public static final char PIXEL_CHAR = '⁚'; // 1px wide character

    public static void padSpaces(StringBuilder builder, int width) {
        while (width >= SPACE_WIDTH) {
            width -= SPACE_WIDTH;
            builder.append(' ');
        }

        while (width-- > 0) {
            builder.append(PIXEL_CHAR);
        }
    }

    public static int repeat(Text.Builder builder, char repChar, int length, boolean forceUnicode) {
        return startRepeatTerminate(builder, (char) 0, repChar, (char) 0, length, forceUnicode);
    }

    public static Text repeatAndTerminate(char repChar, char termChar, int length, boolean forceUnicode) {
        Text.Builder builder = Text.builder();
        repeatAndTerminate(builder, repChar, termChar, length, forceUnicode);
        return builder.build();
    }

    public static int repeatAndTerminate(Text.Builder builder, char repChar, char termChar, int length, boolean forceUnicode) {
        return startRepeatTerminate(builder, (char) 0, repChar, termChar, length, forceUnicode);
    }

    public static int startAndRepeat(Text.Builder builder, char startChar, char repChar, int length, boolean forceUnicode) {
        return startRepeatTerminate(builder, startChar, repChar, (char) 0, length, forceUnicode);
    }

    public static Text startRepeatTerminate(char startChar, char repChar, char termChar, int length, boolean forceUnicode) {
        Text.Builder builder = Text.builder();
        startRepeatTerminate(builder, startChar, repChar, termChar, length, forceUnicode);
        return builder.build();
    }

    public static int startRepeatTerminate(Text.Builder builder, char startChar, char repChar, char termChar, int length, boolean forceUnicode) {
        int repWidth = TextUtils.getWidth(repChar, false, forceUnicode);
        int startWidth = 0;
        if (startChar != 0) {
            startWidth = TextUtils.getWidth(startChar, false, forceUnicode);
        }
        int termWidth = 0;
        if (termChar != 0) {
            termWidth = TextUtils.getWidth(termChar, false, forceUnicode);
        }

        length -= termWidth + startWidth;
        if (length < 0) {
            return length;
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (startChar != 0) {
            stringBuilder.append(startChar);
        }
        while (length >= repWidth) {
            length -= repWidth;
            stringBuilder.append(repChar);
        }
        if (termChar != 0) {
            stringBuilder.append(termChar);
        }
        builder.append(Text.of(stringBuilder.toString()));
        return length;
    }

}
