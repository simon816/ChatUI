package com.simon816.chatui.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.UUID;

public class TextUtils {

    private final boolean forceUnicode;
    private final FontData fontData;
    private final UUID playerUuid;

    public TextUtils(FontData fontData, boolean forceUnicode, UUID playerUuid) {
        this.fontData = fontData;
        this.forceUnicode = forceUnicode;
        this.playerUuid = playerUuid;
    }

    public FontData getFontData() {
        return this.fontData;
    }

    public double getWidth(int codePoint, boolean isBold) {
        return this.fontData.getWidth(codePoint, isBold, this.forceUnicode);
    }

    public int getStringWidth(String text, boolean isBold) {
        double width = 0;
        for (int i = 0; i < text.length(); ++i) {
            width += getWidth(text.codePointAt(i), isBold);
        }
        return (int) Math.ceil(width);
    }

    public int getWidth(char c, boolean isBold) {
        return (int) Math.ceil(getWidth((int) c, isBold));
    }

    public int getWidth(Text text) {
        return (int) Math.ceil(getWidth0(text, false));
    }

    private double getWidth0(Text text, boolean parentIsbold) {
        double width = 0;
        boolean thisIsBold = text.getStyle().isBold().orElse(parentIsbold);
        if (text instanceof LiteralText) {
            String content = ((LiteralText) text).getContent();
            width += getStringWidth(content, thisIsBold);
            for (Text child : text.getChildren()) {
                width += getWidth0(child, thisIsBold);
            }
        } else {
            width += getStringWidth(text.toPlain(), thisIsBold);
        }
        return width;
    }

    public List<String> splitLines(String original, int maxWidth) {
        List<String> output = Lists.newArrayList();
        TextSplitter.splitLines(original, output, maxWidth, this);
        return output;
    }

    public List<Text> splitLines(Text original, int maxWidth) {
        List<Text> output = Lists.newArrayList();
        TextSplitter.splitLines(original, output, maxWidth, Sponge.getServer().getPlayer(this.playerUuid).get().getLocale(), this);
        return output;
    }

    public int repeat(Text.Builder builder, char repChar, int length) {
        return startRepeatTerminate(builder, (char) 0, repChar, (char) 0, length);
    }

    public Text repeatAndTerminate(char repChar, char termChar, int length) {
        Text.Builder builder = Text.builder();
        repeatAndTerminate(builder, repChar, termChar, length);
        return builder.build();
    }

    public int repeatAndTerminate(Text.Builder builder, char repChar, char termChar, int length) {
        return startRepeatTerminate(builder, (char) 0, repChar, termChar, length);
    }

    public int startAndRepeat(Text.Builder builder, char startChar, char repChar, int length) {
        return startRepeatTerminate(builder, startChar, repChar, (char) 0, length);
    }

    public Text startRepeatTerminate(char startChar, char repChar, char termChar, int length) {
        Text.Builder builder = Text.builder();
        startRepeatTerminate(builder, startChar, repChar, termChar, length);
        return builder.build();
    }

    public int startRepeatTerminate(Text.Builder builder, char startChar, char repChar, char termChar, int length) {
        int repWidth = getWidth(repChar, false);
        int startWidth = 0;
        if (startChar != 0) {
            startWidth = getWidth(startChar, false);
        }
        int termWidth = 0;
        if (termChar != 0) {
            termWidth = getWidth(termChar, false);
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

    private static final Char2ObjectMap<Text> charTextCache = new Char2ObjectOpenHashMap<>();

    public static Text charCache(char c) {
        Text t = charTextCache.get(c);
        if (t == null) {
            charTextCache.put(c, t = Text.of(c));
        }
        return t;
    }

    // Space is same width for both unicode and non-unicode
    private static final int SPACE_WIDTH = FontData.VANILLA.getWidthInt(' ', false, false);
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

}
