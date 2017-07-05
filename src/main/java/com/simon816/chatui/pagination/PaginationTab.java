package com.simon816.chatui.pagination;

import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.VBoxUI;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Collections;

public class PaginationTab extends Tab {

    private static class RawTextComonent implements UIComponent {

        private Iterable<Text> origTextContent;

        RawTextComonent() {
        }

        public void setTextContent(Iterable<Text> textContent) {
            this.origTextContent = textContent;
        }

        @Override
        public void draw(PlayerContext ctx, LineFactory lineFactory) {
            if (this.origTextContent != null) {
                for (Text origLine : this.origTextContent) {
                    lineFactory.addAll(TextUtils.splitLines(origLine, ctx.width, ctx.forceUnicode), ctx.forceUnicode);
                }
            }
        }
    }

    private final PaginationSourceWrapper source;
    private final RawTextComonent rawText;

    public PaginationTab(PaginationSourceWrapper source, Text title) {
        super(title, new VBoxUI());
        this.source = source;
        this.rawText = new RawTextComonent();
        getRoot().getChildren().add(this.rawText);
    }

    private void update() {
        if (this.source.view.getWindow().getActiveTab() == this) {
            this.source.view.update();
        }
    }

    public void setText(Text text) {
        this.rawText.setTextContent(Collections.singletonList(text));
        update();
    }

    public void setPage(Iterable<Text> texts) {
        this.rawText.setTextContent(texts);
        update();
    }

    public void setPage(Text[] texts) {
        this.rawText.setTextContent(Arrays.asList(texts));
        update();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.rawText.setTextContent(null);
    }

}
