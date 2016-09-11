package com.simon816.chatui;

import com.simon816.chatui.tabs.NewTab;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.tabs.TextFileTab;
import com.simon816.chatui.tabs.canvas.BlockRenderContext;
import com.simon816.chatui.tabs.canvas.CanvasTab;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DemoContent {

    public static final boolean ENABLE_DEMO = true;

    public static final Tab TAB = new DemoTab();

    static final String[] TEXT_EDITOR_TEXT = new String[] {
            "This is a text editor",
            "",
            "Click on a character to move the caret",
            "Click on '::' to move to that line",
            "Shift+click to get a text prompt"
    };

    private static class DemoTab extends NewTab {

        public DemoTab() {
            addButton(new NewTab.LaunchTabButton("Text Editor", () -> new TextFileTab(TEXT_EDITOR_TEXT)));
            addButton(new SineWaveAnimLoader());
            addButton(new NewTab.LaunchTabButton("Back", PlayerChatView::getNewTab));
        }

        @Override
        public Text getTitle() {
            return Text.of("Demo Content");
        }
    }

    private static class SineWaveAnimLoader extends NewTab.Button {

        public SineWaveAnimLoader() {
            super("Sine Wave Animation");
        }

        @Override
        protected void onClick(PlayerChatView view) {
            CanvasTab canvas = new CanvasTab(Text.of("Sine Wave Demo")) {

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
                    BlockRenderContext ctx = getContext(Context.BLOCKS);
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
            replaceWith(canvas, view);
        }

    }

}
