package com.simon816.chatui;

import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.tabs.UITest;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.canvas.BlockRenderContext;
import com.simon816.chatui.ui.canvas.CanvasUI;
import com.simon816.chatui.ui.canvas.CanvasUI.Context;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;

public class DemoContent {

    public static final boolean ENABLE_DEMO = true;

    public static final Tab TAB = new DemoTab();

    static final String[] TEXT_EDITOR_TEXT = new String[] {
            "This is a text editor",
            "",
            "Click on a line to set the line as the active line",
            "Click again on the active line to get a copy of the line in the text prompt",
            "Type something and press enter while on an active line to set its content"
    };

    private static class DemoTab extends NewTab {

        public DemoTab() {
            addButton("Text Editor", new NewTab.LaunchTabAction(() -> {
                TextEditorWindow te = new TextEditorWindow();
                Collections.addAll(te.getLines(), TEXT_EDITOR_TEXT);
                return te.createTab(Text.of("Text Editor"));
            }));
            addButton("Sine Wave Animation", new SineWaveAnimLoader());
            addButton("UI Test", new NewTab.LaunchTabAction(() -> new UITest()));
            addButton("Back", new NewTab.LaunchTabAction(PlayerChatView::getNewTab));
        }

        @Override
        public Text getTitle() {
            return Text.of("Demo Content");
        }
    }

    private static class SineWaveAnimLoader extends NewTab.ButtonAction {

        public SineWaveAnimLoader() {
        }

        @Override
        protected void onClick(PlayerChatView view) {
            CanvasUI canvas = new CanvasUI();
            Tab sineWaveTab = new Tab(Text.of("Sine Wave Demo"), new AnchorPaneUI(canvas)) {

                private Task task;

                @Override
                public void onBlur() {
                    super.onBlur();
                    if (this.task != null) {
                        this.task.cancel();
                    }
                }

                @Override
                public void onFocus() {
                    super.onFocus();
                    BlockRenderContext ctx = canvas.getContext(Context.BLOCKS);
                    ctx.clear();
                    int[] i = new int[1];
                    this.task = Sponge.getScheduler().createTaskBuilder().execute(() -> {
                        ctx.clear();
                        int n = i[0]++;
                        for (int x = 0; x < 210; x++) {
                            int y = (int) ((Math.sin(Math.toRadians((x + n) * 10)) * 5) + 7);
                            ctx.drawRect(x, y, x + 1, y + 1, TextColors.BLUE);
                        }
                        view.update();
                    }).intervalTicks(2).submit(ChatUI.instance());
                }

                @Override
                public void onClose() {
                    super.onClose();
                    if (this.task != null) {
                        this.task.cancel();
                    }
                }
            };
            replaceWith(sineWaveTab, view);
        }

    }

}
