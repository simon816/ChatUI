# Chat UI
A User Interface Toolkit for drawing GUIs in the Vanilla Minecraft chat box.  
Written using the Sponge API, compatible with versions 4.x and current 5.0.0 snapshots.

[__Early access downloads on the Releases page__](https://github.com/simon816/ChatUI/releases)

# Features
This plugin allows players to perform tasks using a graphically based interface as opposed to the traditonal command based interface.

### General Layout
A frame is defined in the chat window, where tabs can be navigated and a status bar resides on the bottom.

![Screenshot](http://puu.sh/r7v5K/3cb846506d.png)

### Player List
This screen uses the `TableUI` to list all players on the server. If you have the admin permisson, you can kick and ban players directly from this list.

![Screenshot](http://puu.sh/r7wtT/6c716a6341.png)


### Private Messages
From the player list, click 'Message' to start a private message with that player.

![Screenshot](http://puu.sh/r7wF5/ea5ee34efa.png)

The otherplayer will see this before clicking on the tab:  
![Screenshot](http://puu.sh/r7wM9/e0e7ab506a.png)  
Where '(1)' is the number of unread messages.

### Configuation Editor
Only accessible if you have the admin permission.
The configuration editor is another use of `TableUI`.  
It allows navigating through a key:value based config file (uses configurate's `ConfigurationNode`).
There are two types of values: simple and complex.
A simple value is either a number, boolean or a string. A complex value is a list or a map.
To open a complex value, click on it and it will navigate to that node. Keep track of the current path by observing the 'breadcrumb' at the top. Click on any part of a breadcrumb to go back to that node.

To edit a simple value, clicking on the value will highlight that row. You can type in chat the new value of the node, or click again on the value to prompt the value in chat.
Example screen:  
![Screenshot](http://puu.sh/r7wX9/5c7d8fa2f1.png)

New nodes can be created by clicking the [New] button.

![Screenshot](http://puu.sh/r7x3W/15c9b470cd.png)

Here, you enter the name of the key in chat. Then click on the type of value, and optionally enter the value afterwards.

### Settings
Each player has configurable settings. Players should adjust the settings if they have adjusted their client chat settings.
The interface uses the same config editor design from above.
![Screenshot](http://puu.sh/r7vFQ/99cf6a79ee.png)


| Setting Name  | Description                                                                           | Default |
|---------------|---------------------------------------------------------------------------------------|---------|
| displayHeight | This is the height of the window, measured in lines                                   | 20      |
| displayWidth  | This is the width of the window, measured in pixels                                   | 320     |
| enabled       | Whether or not ChatUI is enabled for this player (currently does not have any effect) | true    |

## Extra features
Theses are features that don't have an application just yet, but may be used in the future or by another plugin.

### Text Editor
A simple text editor

![Screenshot](http://puu.sh/r7xej/3b1bc42ba2.png)

### Canvas
The canvas tab defines a drawing context (similar to JavaScript's 2D canvas context).  
Shapes an images can be drawn on the canvas. The context is a x*y plane of 'pixels' that can be set to different colors.
There are two types of drawing contexts: 'block' and 'braille'. Block uses a unicode block character to form a grid, while braille uses the unicode braille characters to form a grid of smaller pixels.
Sine wave example using block context:  
![Screenshot](http://puu.sh/r7xvd/9625e07747.png)  
(Note: The black empty characters are support characters, I've yet to find an invisible 9x9 character.

Animations are also possible by using SpongeAPI's scheduler to push updates to the client if the tab is active.

## Plugin support

It is possible for other plugins to design their own interfaces on the UI.
Plugins can use existing components and are free to design custom components.

# Permissions
Currently only one permission is defined, the admin permission.

Admin Permission: `chatui.admin`
