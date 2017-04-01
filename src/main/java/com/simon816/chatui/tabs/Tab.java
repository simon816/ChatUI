package com.simon816.chatui.tabs;

import static com.google.common.base.Preconditions.checkNotNull;

import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.TopWindow;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.UIPane;
import org.spongepowered.api.text.Text;

public class Tab implements UIComponent, TopWindow {

    private Text title;
    private UIPane root;
    private boolean hasCloseButton = true;

    public Tab(Text title, UIPane root) {
        setTitle(title);
        setRoot(root);
    }

    protected Tab(Text title) {
        setTitle(title);
    }

    public Text getTitle() {
        return this.title;
    }

    public void setTitle(Text title) {
        this.title = checkNotNull(title, "title");
    }

    public UIPane getRoot() {
        return this.root;
    }

    public void setRoot(UIPane root) {
        this.root = checkNotNull(root, "root");
    }

    public boolean hasCloseButton() {
        return this.hasCloseButton;
    }

    public void setHasCloseButton(boolean hasCloseButton) {
        this.hasCloseButton = hasCloseButton;
    }

    @Override
    public void draw(PlayerContext ctx, LineFactory lineFactory) {
        this.root.draw(ctx, lineFactory);
    }

    public void onBlur() {
    }

    public void onFocus() {
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
    }

    @Override
    public boolean onCommand(PlayerChatView view, String[] args) {
        return false;
    }

}
