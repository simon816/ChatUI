package com.simon816.minecraft.tabchat.util;

import com.google.common.collect.Lists;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.locale.Locales;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TextUtils {
    // See PaginationCalculator

    private static final String NON_UNICODE_CHARS;
    private static final int[] NON_UNICODE_CHAR_WIDTHS;
    private static final byte[] UNICODE_CHAR_WIDTHS;

    static {
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setURL(TextUtils.class.getResource("font-sizes.json"))
                .setPreservesHeader(false).build();
        try {
            ConfigurationNode node = loader.load();
            NON_UNICODE_CHARS = node.getNode("non-unicode").getString();
            List<? extends ConfigurationNode> charWidths = node.getNode("char-widths").getChildrenList();
            int[] nonUnicodeCharWidths = new int[charWidths.size()];
            for (int i = 0; i < nonUnicodeCharWidths.length; ++i) {
                nonUnicodeCharWidths[i] = charWidths.get(i).getInt();
            }
            NON_UNICODE_CHAR_WIDTHS = nonUnicodeCharWidths;

            List<? extends ConfigurationNode> glyphWidths = node.getNode("glyph-widths").getChildrenList();
            byte[] unicodeCharWidths = new byte[glyphWidths.size()];
            for (int i = 0; i < nonUnicodeCharWidths.length; ++i) {
                unicodeCharWidths[i] = (byte) glyphWidths.get(i).getInt();
            }
            UNICODE_CHAR_WIDTHS = unicodeCharWidths;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static double getWidth(int codePoint, boolean isBold) {
        if (codePoint == '\u2063') {
            return 9; // TODO Investigate why incorrect value is returned
        }
        if (codePoint >= '\u2800' && codePoint < '\u2900') {
            return 3;
        }
        int nonUnicodeIdx = NON_UNICODE_CHARS.indexOf(codePoint);
        double width;
        if (nonUnicodeIdx != -1) {
            width = NON_UNICODE_CHAR_WIDTHS[nonUnicodeIdx];
            if (isBold) {
                width += 1;
            }
        } else {
            // MC unicode -- what does this even do? but it's client-only so we
            // can't use it directly :/
            int j = UNICODE_CHAR_WIDTHS[codePoint] >>> 4;
            int k = UNICODE_CHAR_WIDTHS[codePoint] & 15;

            if (k > 7) {
                k = 15;
                j = 0;
            }
            width = ((k + 1) - j) / 2 + 1;
            if (isBold) {
                width += 0.5;
            }
        }
        return width;
    }

    public static int getStringWidth(String text, boolean isBold) {
        double width = 0;
        for (int i = 0; i < text.length(); ++i) {
            width += getWidth(text.codePointAt(i), isBold);
        }
        return (int) Math.ceil(width);
    }

    public static int getWidth(char c, boolean isBold) {
        return getStringWidth(String.valueOf(c), isBold);
    }

    public static List<Text> splitLines(Text original, int maxWidth) {
        return splitLines(original, maxWidth, Locales.DEFAULT);
    }

    public static List<Text> splitLines(Text original, int maxWidth, Locale locale) {
        List<Text> output = Lists.newArrayList();
        TextSplitter.splitLines(original, output, maxWidth, locale);
        return output;
    }

    public static int getWidth(Text text) {
        return getStringWidth(text.toPlain(), text.getStyle().isBold().orElse(false));
    }

    private static final TCharObjectMap<Text> charTextCache = new TCharObjectHashMap<>();

    public static Text charCache(char c) {
        Text t = charTextCache.get(c);
        if (t == null) {
            charTextCache.put(c, t = Text.of(c));
        }
        return t;
    }

    private static final int SPACE_WIDTH = getWidth(' ', false);
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

    public static int repeat(Text.Builder builder, char repChar, int length) {
        return startRepeatTerminate(builder, (char) 0, repChar, (char) 0, length);
    }

    public static Text repeatAndTerminate(char repChar, char termChar, int length) {
        Text.Builder builder = Text.builder();
        repeatAndTerminate(builder, repChar, termChar, length);
        return builder.build();
    }

    public static int repeatAndTerminate(Text.Builder builder, char repChar, char termChar, int length) {
        return startRepeatTerminate(builder, (char) 0, repChar, termChar, length);
    }

    public static Text startRepeatTerminate(char startChar, char repChar, char termChar, int length) {
        Text.Builder builder = Text.builder();
        startRepeatTerminate(builder, startChar, repChar, termChar, length);
        return builder.build();
    }

    public static int startRepeatTerminate(Text.Builder builder, char startChar, char repChar, char termChar, int length) {
        int repWidth = TextUtils.getWidth(repChar, false);
        int startWidth = 0;
        if (startChar != 0) {
            startWidth = TextUtils.getWidth(startChar, false);
        }
        int termWidth = 0;
        if (termChar != 0) {
            termWidth = TextUtils.getWidth(termChar, false);
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
