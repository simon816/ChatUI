package com.simon816.chatui.tabs;

import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.ui.UIPane;
import org.spongepowered.api.text.Text;

/**
 * A simple tab to display a UIPane when drawn.
 */
public class SceneTab extends Tab {

    private final Text title;
    private final UIPane root;

    public SceneTab(Text title, UIPane root) {
        this.title = title;
        this.root = root;
    }

    @Override
    public Text getTitle() {
        return this.title;
    }

    @Override
    public Text draw(PlayerContext ctx) {
        return this.root.draw(ctx);
    }

}
