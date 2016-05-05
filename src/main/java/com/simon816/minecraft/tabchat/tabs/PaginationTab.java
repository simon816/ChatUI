package com.simon816.minecraft.tabchat.tabs;

import com.simon816.minecraft.tabchat.pagination.PaginationSourceWrapper;
import org.spongepowered.api.text.Text;

public class PaginationTab extends BufferedTab {

    private Text currentPageText;
    private final PaginationSourceWrapper source;
    private final Text title;

    public PaginationTab(PaginationSourceWrapper source, Text title) {
        this.source = source;
        this.title = title;
    }

    @Override
    public void appendMessage(Text message) {
        this.currentPageText = message;
        if (this.source.view.getWindow().getActiveTab() == this) {
            this.source.view.update();
        }
    }

    public void setPage(Iterable<Text> texts) {
        this.currentPageText = Text.joinWith(Text.NEW_LINE, texts);
        if (this.source.view.getWindow().getActiveTab() == this) {
            this.source.view.update();
        }
    }

    public void setPage(Text[] texts) {
        this.currentPageText = Text.joinWith(Text.NEW_LINE, texts);
        if (this.source.view.getWindow().getActiveTab() == this) {
            this.source.view.update();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.currentPageText = null;
    }

    @Override
    public Text draw(int height) {
        if (this.currentPageText == null) {
            return super.draw(height);
        }
        return this.currentPageText;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

}
