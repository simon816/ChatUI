package com.simon816.chatui.tabs.perm;

import com.simon816.chatui.ActivePlayerChatView;
import com.simon816.chatui.ChatUI;
import com.simon816.chatui.PlayerChatView;
import com.simon816.chatui.PlayerContext;
import com.simon816.chatui.tabs.Tab;
import com.simon816.chatui.ui.Button;
import com.simon816.chatui.ui.FlowPaneUI;
import com.simon816.chatui.ui.UIPane;
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

    private final PermissionService service;
    private final PermissionActions actions;

    public PermissionsTab(PermissionService service) {
        super(Text.of("Permissions"));
        this.service = service;
        this.actions = findActions(service);
        this.dashboard = createDashboard();
        this.subjListPane = new SubjectListPane(this);
        this.subjViewer = new SubjectViewer(this);
        this.entryDisplayer = new EntryDisplayer(this);
        this.setRoot(this.dashboard);
    }

    private static PermissionActions findActions(PermissionService service) {
        ProviderRegistration<PermissionService> reg = Sponge.getServiceManager().getRegistration(PermissionService.class).get();
        if (reg.getPlugin().getId().equals("permissionsex")) {
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
            if (!(view instanceof ActivePlayerChatView) || ((ActivePlayerChatView) view).getWindow().getActiveTab() != this) {
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
        FlowPaneUI dashboard = new FlowPaneUI(FlowPaneUI.WRAP_HORIZONALLY);
        for (Entry<String, SubjectCollection> subjEntry : this.service.getKnownSubjects().entrySet()) {
            Button button = new Button(subjEntry.getKey()) {

                @Override
                public int getPrefWidth(PlayerContext ctx) {
                    return ctx.width / 2;
                }
            };
            button.setClickHandler(onClick(() -> {
                this.subjListPane.setSubjectList(subjEntry.getValue());
                this.setRoot(this.subjListPane);
            }));
            dashboard.getChildren().add(button);
        }

        return dashboard;
    }

    public SubjectViewer getSubjViewer() {
        return this.subjViewer;
    }

    public UIPane getDashboard() {
        return this.dashboard;
    }

    @Override
    public void onTextInput(PlayerChatView view, Text input) {
        if (this.getRoot() == this.subjViewer) {
            this.subjViewer.onTextEntered(view, input);
        } else if (this.getRoot() == this.subjListPane) {
            this.subjListPane.onTextEntered(view, input);
        } else if (this.getRoot() == this.entryDisplayer) {
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
