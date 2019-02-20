# Chat UI
A User Interface library and plugin for creating GUIs in Minecraft's chat box.

# Aim
To enhance player user experience on multiplayer by utilizing the chat box. In addition, this plugin provides a graphical approach to tasks commonly performed with commands.

# Features
 * Tabbed interface with status bar
 * Player list with admin tools
 * Private messages between players
 * Pagination viewer
 * Configuration editor for the server config
 * Permissions manager
 * Chat groups
 * Per-player settings with ability to disable the interface
 * Expandable - other plugins can hook into the interface and add new features

For a full description and demonstration of Chat UI's features, please check out the [wiki](https://github.com/simon816/ChatUI/wiki/features).

# Demo
![Demo gif](https://i.imgur.com/BFmcx7q.gif)

# Chat UI Library
Chat UI depends on the Chat UI Library (also in this repository). The library provides a high-level User Interface component model for creating GUIs, it is inspired by JavaFX and Swing.  
More information can be found on the [wiki](https://github.com/simon816/ChatUI/wiki/Chat-UI-Library).

# Links
 * [__Downloads__](https://ore.spongepowered.org/simon816/Chat-UI/versions/recommended/download)
 * [Ore Project Page](https://ore.spongepowered.org/simon816/Chat-UI)
 * [Chat UI Library](https://ore.spongepowered.org/simon816/Chat-UI-Library)
 * [Source Code](https://github.com/simon816/ChatUI)
 * [Issue Tracker](https://github.com/simon816/ChatUI/issues)
 * [Wiki](https://github.com/simon816/ChatUI/wiki)
 * Archive (pre 1.0.0 release)
   * [Old Forum Post](https://forums.spongepowered.org/t/chat-ui-a-ui-toolkit-for-the-vanilla-chat-box/10109)
   * [Early versions](https://github.com/simon816/ChatUI/releases)


# Developing

## Using The UI Library
The UI Library can be used with or without the Chat UI tabbed interface. See the [developer documentation](https://github.com/simon816/ChatUI/wiki/Chat-UI-Library).

## Utilizing Chat UI in another plugin
Chat UI provides an API for other plugin developers to use in their plugins.  
Please check out the [wiki tutorial](https://github.com/simon816/ChatUI/wiki/integration) on integrating with Chat UI.

## Developing Chat UI itself
### Setup
Chat UI is built with [Gradle](https://gradle.org/).  
To set up a workspace for development, run `gradle eclipse` for [Eclipse](https://www.eclipse.org/) or `gradle idea` for [Intellij IDEA](https://www.jetbrains.com/idea/). Then import as an existing project.
Alternatively, import the project as a Gradle project from within your IDE.

If you've never worked with Sponge API before it may be worthwhile checking out the [developer documentation](https://docs.spongepowered.org/stable/en/plugin/index.html).

### Building
Simply run the gradle `build` task (`gradle build` from the command line). The plugin jar file will be written to `./build/libs/`

The ChatUILib subproject also gets built at the same time. Its output is in `./ChatUILib/build/libs/`.

