package com.simon816.chatui.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.translation.Translation;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TextSplitter {

    private static class Format {

        private static final Format DEFAULTS = new Format();

        public final TextFormat format;
        public final ClickAction<?> onClick;
        public final HoverAction<?> onHover;
        public final ShiftClickAction<?> onShiftClick;

        private Format() {
            this.format = TextFormat.of(TextColors.RESET, TextStyles.RESET);
            this.onClick = null;
            this.onHover = null;
            this.onShiftClick = null;
        }

        public Format(Text root) {
            this(DEFAULTS, root);
        }

        public Format(Format parent, Text text) {
            this.format = TextFormat.of(text.getColor() == TextColors.NONE ? parent.format.getColor() : text.getColor(),
                    inheritStyle(parent.format.getStyle(), text.getStyle()));
            this.onClick = text.getClickAction().orElse(parent.onClick);
            this.onHover = text.getHoverAction().orElse(parent.onHover);
            this.onShiftClick = text.getShiftClickAction().orElse(parent.onShiftClick);
        }

        private static TextStyle inheritStyle(TextStyle base, TextStyle overrides) {
            // styles from base always exist as it must derive from DEFAULTS
            return new TextStyle(overrides.isBold().orElse(base.isBold().get()),
                    overrides.isItalic().orElse(base.isItalic().get()),
                    overrides.hasUnderline().orElse(base.hasUnderline().get()),
                    overrides.hasStrikethrough().orElse(base.hasStrikethrough().get()),
                    overrides.isObfuscated().orElse(base.isObfuscated().get()));
        }

        public Format with(Text text) {
            return new Format(this, text);
        }

        public Text.Builder applyToBuilder(Text.Builder builder) {
            return builder.format(this.format).onClick(this.onClick).onHover(this.onHover).onShiftClick(this.onShiftClick);
        }

        public Text createText(String content) {
            return applyToBuilder(Text.builder(content)).build();
        }
    }

    private static final Splitter LINE_SPLITTER = Splitter.on('\n');
    private static final Pattern MARKER_PATTERN = Pattern.compile("\\$MARKER(\\d+)\\$");

    public static void splitLines(Text original, List<Text> output, int maxWidth, Locale locale, boolean forceUnicode) {
        if (maxWidth < 1) {
            throw new IllegalArgumentException("Max width must be at least 1, was " + maxWidth);
        }
        Stack<Format> formatStack = new Stack<>();
        Object[] ret = apply(0, Text.builder(), formatStack, original, output, maxWidth, locale, forceUnicode);
        output.add(((Text.Builder) ret[1]).build());
    }

    private static Object[] apply(int currLineLength, Text.Builder currLineBuilder, Stack<Format> formatStack, Text text, List<Text> output,
            int maxWidth, Locale locale, boolean forceUnicode) {
        if (text instanceof TranslatableText) {
            text = transformTranslationText((TranslatableText) text, locale);
        }
        Format format = pushFormat(formatStack, text);

        String plainText = text.toPlainSingle();
        List<String> lines = LINE_SPLITTER.splitToList(plainText);
        boolean first = true;
        String next = null;
        for (int i = 0; next != null || i < lines.size(); i++) {
            String line;
            if (next != null) {
                line = next;
                next = null;
                i--;
            } else {
                line = lines.get(i);
            }
            if (!first) {
                output.add(currLineBuilder.build());
                currLineBuilder = Text.builder();
                currLineLength = 0;
            }
            first = false;
            if (line.isEmpty()) {
                continue;
            }
            boolean isBold = format.format.getStyle().isBold().get();
            int lineW = TextUtils.getStringWidth(line, isBold, forceUnicode);
            if (currLineLength + lineW > maxWidth) {
                String[] trimmed = trimToMaxWidth(line, isBold, maxWidth - currLineLength, forceUnicode);
                line = trimmed[0];
                if (currLineLength == 0 && line.isEmpty()) {
                    // Cannot fit this within the maxWidth
                    break; // give up
                }
                lineW = TextUtils.getStringWidth(line, isBold, forceUnicode);
                next = trimmed[1];
            }
            currLineLength += lineW;
            currLineBuilder.append(format.createText(line));
        }

        for (Text child : text.getChildren()) {
            Object[] ret = apply(currLineLength, currLineBuilder, formatStack, child, output, maxWidth, locale, forceUnicode);
            currLineLength = (Integer) ret[0];
            currLineBuilder = (Text.Builder) ret[1];
        }
        formatStack.pop();
        return new Object[] {currLineLength, currLineBuilder};
    }

    private static Format pushFormat(Stack<Format> formatStack, Text text) {
        Format format;
        if (formatStack.isEmpty()) {
            format = new Format(text);
        } else {
            format = formatStack.peek().with(text);
        }
        formatStack.push(format);
        return format;
    }

    // Transforms a TranslatableText into a LiteralText
    private static Text transformTranslationText(TranslatableText text, Locale locale) {
        // This is bad, don't look
        Translation translation = text.getTranslation();
        ImmutableList<Object> arguments = text.getArguments();
        Object[] markers = new Object[arguments.size()];
        for (int i = 0; i < markers.length; i++) {
            markers[i] = "$MARKER" + i + "$";
        }
        String patched = translation.get(locale, markers);

        List<Object> sections = Lists.newArrayList();
        Matcher m = MARKER_PATTERN.matcher(patched);
        int prevPos = 0;
        while (m.find()) {
            if (m.start() != prevPos) {
                sections.add(patched.substring(prevPos, m.start()));
            }
            int index = Integer.parseInt(m.group(1));
            sections.add(arguments.get(index));
            prevPos = m.end();
        }
        if (prevPos != patched.length() - 1 || prevPos == 0) {
            sections.add(patched.substring(prevPos));
        }
        Text.Builder builder = new Format(text).applyToBuilder(Text.builder());
        for (Object val : sections) {
            builder.append(Text.of(val));
        }
        builder.append(text.getChildren());
        return builder.build();
    }

    private static String[] trimToMaxWidth(String text, boolean bold, int maxWidth, boolean forceUnicode) {
        int currLen = 0;
        int pos = 0;
        while (currLen < maxWidth && pos < text.length()) {
            currLen += TextUtils.getWidth(text.codePointAt(pos++), bold, forceUnicode);
        }
        if (currLen > maxWidth) {
            pos--;
        }
        return new String[] {text.substring(0, pos), text.substring(pos)};
    }

    public static void splitLines(String original, List<String> output, int maxWidth, boolean forceUnicode) {
        String next = null;
        Iterator<String> iterator = LINE_SPLITTER.split(original).iterator();
        int currLineWidth = 0;
        while (iterator.hasNext() || next != null) {
            String part = next != null ? next : iterator.next();
            next = null;
            int partW = TextUtils.getStringWidth(part, false, forceUnicode);
            if (partW + currLineWidth > maxWidth) {
                String[] trimmed = trimToMaxWidth(part, false, maxWidth - currLineWidth, forceUnicode);
                part = trimmed[0];
                if (currLineWidth == 0 && part.isEmpty()) {
                    break;
                }
                next = trimmed[1];
                partW = TextUtils.getStringWidth(part, false, forceUnicode);
                currLineWidth = 0;
            } else {
                currLineWidth += partW;
            }
            output.add(part);
        }
    }

}
