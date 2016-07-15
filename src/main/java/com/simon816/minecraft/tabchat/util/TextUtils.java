package com.simon816.minecraft.tabchat.util;

import com.google.common.collect.Lists;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import net.minecraft.util.ChatComponentText;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.text.SpongeTexts;

import java.io.IOException;
import java.util.List;

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

    private static String trimStringToWidth(String text, int width, boolean isBold) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = 0;
        int k = 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
            char c0 = text.charAt(l);
            int i1 = (int) getWidth(c0, isBold);

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (i1 < 0) {
                flag = true;
            } else {
                i += i1;

                if (flag1) {
                    ++i;
                }
            }

            if (i > width) {
                break;
            }

            stringbuilder.append(c0);
        }

        return stringbuilder.toString();
    }

    private static LiteralText createWithInheritedProperties(String text, final Text from) {
        // TODO This uses internal code for now
        ChatComponentText component = new ChatComponentText(text);
        component.setChatStyle(SpongeTexts.toComponent(from).getChatStyle().createShallowCopy());
        return (LiteralText) SpongeTexts.toText(component);
    }

    public static Text unwrap(Text message) {
        while (message.getChildren().size() == 1) {
            message = message.getChildren().get(0);
        }
        return message;
    }

    public static List<Text> splitLines(Text original, int maxWidth, boolean withColor) {
        // See GuiUtilRenderComponents
        int i = 0;
        LiteralText.Builder ichatcomponent = Text.builder("");
        List<Text> list = Lists.newArrayList();
        List<Text> list1 = Lists.newArrayList(original);

        for (int j = 0; j < list1.size(); ++j) {
            Text ichatcomponent1 = list1.get(j);
            String s = ichatcomponent1.toPlain();
            boolean flag = false;

            if (s.contains("\n")) {
                int k = s.indexOf(10);
                String s1 = s.substring(k + 1);
                s = s.substring(0, k + 1);
                list1.add(j + 1, createWithInheritedProperties(s1, ichatcomponent1));
                flag = true;
            }

            String s4 = s;
            // String s4 = withColor ? temp :
            // EnumChatFormatting.getTextWithoutFormattingCodes(temp);
            String s5 = s4.endsWith("\n") ? s4.substring(0, s4.length() - 1) : s4;
            boolean isBold = ichatcomponent1.getStyle().contains(TextStyles.BOLD);
            int i1 = getStringWidth(s5, isBold);
            LiteralText chatcomponenttext1 = createWithInheritedProperties(s5, ichatcomponent1);

            if (i + i1 > maxWidth) {
                String s2 = trimStringToWidth(s4, maxWidth - i, isBold);
                String s3 = s2.length() < s4.length() ? s4.substring(s2.length()) : null;

                if (s3 != null && s3.length() > 0) {
                    int l = s2.lastIndexOf(" ");

                    if (l >= 0 && getStringWidth(s4.substring(0, l), isBold) > 0) {
                        s2 = s4.substring(0, l);

                        s3 = s4.substring(l);
                    } else if (i > 0 && !s4.contains(" ")) {
                        s2 = "";
                        s3 = s4;
                    }

                    list1.add(j + 1, createWithInheritedProperties(s3, chatcomponenttext1));
                }

                i1 = getStringWidth(s2, isBold);
                chatcomponenttext1 = createWithInheritedProperties(s2, chatcomponenttext1);
                flag = true;
            }

            if (i + i1 <= maxWidth) {
                i += i1;
                ichatcomponent.append(chatcomponenttext1);
            } else {
                flag = true;
            }

            if (flag) {
                list.add(ichatcomponent.build());
                i = 0;
                ichatcomponent = Text.builder("");
            }
        }

        list.add(ichatcomponent.build());
        return list;
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

    private static final int SPACE_WIDTH = getStringWidth(" ", false);
    private static final char PIXEL_CHAR = '⁚'; // 1px wide character

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
        int repWidth = TextUtils.getStringWidth(String.valueOf(repChar), false);
        int startWidth = 0;
        if (startChar != 0) {
            startWidth = TextUtils.getStringWidth(String.valueOf(startChar), false);
        }
        int termWidth = 0;
        if (termChar != 0) {
            termWidth = TextUtils.getStringWidth(String.valueOf(termChar), false);
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
