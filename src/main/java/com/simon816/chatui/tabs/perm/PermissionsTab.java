package com.simon816.chatui.tabs.perm;

import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.Button;
import com.simon816.chatui.ui.HBoxUI;
import com.simon816.chatui.ui.UIPane;
import com.simon816.chatui.ui.VBoxUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction.ExecuteCallback;

import java.util.Map.Entry;
import java.util.function.Consumer;

public class PermissionsTab extends Tab {

    private final UIPane dashboard;
    private final SubjectListPane subjListPane;
    private final SubjectViewer subjViewer;
    private final EntryDisplayer entryDisplayer;
    private UIPane active;

    private final PermissionService service;
    private final PermissionActions actions;

    public PermissionsTab(PermissionService service) {
        this.service = service;
        this.actions = findActions(service);
        this.dashboard = createDashboard();
        this.subjListPane = new SubjectListPane(this);
        this.subjViewer = new SubjectViewer(this);
        this.entryDisplayer = new EntryDisplayer(this);
        this.active = this.dashboard;
    }

    private static PermissionActions findActions(PermissionService service) {
        ProviderRegistration<PermissionService> reg = Sponge.getServiceManager().getRegistration(PermissionService.class).get();
        if (reg.getPlugin().getId().endsWith("permissionsex")) {
            // Ensure loaded
            service.getSubjects(PermissionService.SUBJECTS_COMMAND_BLOCK);
            service.getSubjects(PermissionService.SUBJECTS_GROUP);
            service.getSubjects(PermissionService.SUBJECTS_ROLE_TEMPLATE);
            service.getSubjects(PermissionService.SUBJECTS_SYSTEM);
            service.getSubjects(PermissionService.SUBJECTS_USER);
            return new PEXActions();
        }
        return new FallbackPermActions();
    }

    public PermissionActions actions() {
        return this.actions;
    }

    Consumer<PlayerChatView> onClick(Runnable r) {
        return onClick(view -> r.run());
    }

    Consumer<PlayerChatView> onClick(Consumer<PlayerChatView> consumer) {
        return view -> {
            if (view.getWindow().getActiveTab() != this) {
                return;
            }
            consumer.accept(view);
            view.update();
        };
    }

    ExecuteCallback execClick(Runnable r) {
        return ChatUI.execClick(src -> onClick(r).accept(ChatUI.getView(src)));
    }

    ExecuteCallback execClick(Consumer<PlayerChatView> consumer) {
        return ChatUI.execClick(src -> onClick(consumer).accept(ChatUI.getView(src)));
    }

    private UIPane createDashboard() {
        VBoxUI col1 = new VBoxUI();
        VBoxUI col2 = new VBoxUI();
        boolean left = true;
        for (Entry<String, SubjectCollection> subjEntry : this.service.getKnownSubjects().entrySet()) {
            Button button = new Button(subjEntry.getKey()) {

                @Override
                public int getPrefWidth(PlayerContext ctx) {
                    return ctx.width / 2;
                }
            };
            button.setClickHandler(onClick(() -> {
                this.subjListPane.setSubjectList(subjEntry.getValue());
                this.active = this.subjListPane;
            }));
            if (left) {
                col1.addChildren(button);
            } else {
                col2.addChildren(button);
            }
            left = !left;
        }

        HBoxUI dashboard = new HBoxUI();
        dashboard.addChildren(col1, col2);
        return dashboard;
    }

    @Override
    public Text getTitle() {
        return Text.of("Permissions");
    }

    @Override
    public Text draw(PlayerContext ctx) {
        return this.active.draw(ctx);
    }

    public SubjectViewer getSubjViewer() {
        return this.subjViewer;
    }

    void setActive(UIPane pane) {
        this.active = pane;
    }

    public UIPane getDashboard() {
        return this.dashboard;
    }

    @Override
    public void onTextEntered(PlayerChatView view, Text input) {
        if (this.active == this.subjViewer) {
            this.subjViewer.onTextEntered(view, input);
        } else if (this.active == this.subjListPane) {
            this.subjListPane.onTextEntered(view, input);
        } else if (this.active == this.entryDisplayer) {
            this.entryDisplayer.onTextEntered(view, input);
        }
    }

    public SubjectListPane getSubjListPane() {
        return this.subjListPane;
    }

    public EntryDisplayer getEntryDisplayer() {
        return this.entryDisplayer;
    }

}
