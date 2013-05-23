lawena Recording Tool
=====================

[![Build Status](https://travis-ci.org/iabarca/lawena-recording-tool.png)](https://travis-ci.org/iabarca/lawena-recording-tool)

Simple Team Fortress 2 (TF2) movie recording tool

Original project at [Google Code](http://code.google.com/p/lawenarecordingtool/) by Montz. This is a fork that supports SteamPipe and is planned to fix bugs and implement additional features.
It requires [JRE 1.7](http://www.java.com) and it's currently being maintained by me ([Quantic](http://steamcommunity.com/id/thepropane)) since Montz appears to be inactive.

#### Download latest package
- [Get the zipfile from here](https://www.dropbox.com/sh/lfyio7gxaf1gml3/ezmc9MPEVD) (Dropbox)
- Extract to any folder and run **lawena.exe**.

#### Build from repository
- Clone or [Download](https://github.com/iabarca/lawena-recording-tool/zipball/master) this repository
- Execute **build-jar.bat** (requires JDK 1.7) and then double-click **lawena.jar**.
- Also included a Gradle build file that can create a jarfile with the "jar" task.

#### Instructions
- Run lawena.exe, a splash screen should appear.
- Choose your tf directory, for instance "\Steam\steamapps\common\Team Fortress 2\tf".
- Choose your moviefile directory, where the tool will store everything you record.
- Configure it as you like, also you can save settings to a file and edit them later.
- Start TF2, lawena will make a backup of your cfg and custom folder while TF2 is running.
- Load a demo and press P to start recording.
- When you close TF2, your files should be restored and there should be no "lwrt" folders inside your tf folder.
- If lawena is not closed properly, running it once should restore all folders. Your files are inside the "lwrtcfg" and "lwrtcustom" folders.
- Please report any [issue](https://github.com/iabarca/lawena-recording-tool/issues) you might find. A logfile "lawena.log" will be present if there were errors.

#### Adding custom resources
- To use a custom HUD for recording, copy said HUD's resource and scripts folders into "\hud\custom" and then choose Custom HUD in the HUD settings.
- To use a custom Skybox for recording, simply copy said Skybox's .vtf files (not the .vmt files) into "\Skybox". The tool will automatically generate an option in the settings and a preview screenshot of the skybox.

#### Keybindings
- **N** - Locks viewmodels so that if they were enabled when you press the key, they stay on and can't be disabled, and viceversa. This setting is overridden by restarting TF2
- **M** - Locks default crosshair so that it cannot be changed. This setting is overridden by restarting TF2
- **R** - Enables/disables ragdolls.
- **UP ARROW** - Increases the recording frame rate
- **DOWN ARROW** - Decreases the recording frame rate
- **P** - Starts recording a series of .tga files and their respective .wav file. Each series is named XY_ where X is a letter and Y is a number between 1 and 15 

#### Credits and Thanks
- Montz - original lawena developer.
- Chris Down - configs.
- Broesel, m0re, povohat and Barrakketh - HUDs.
- Komaokc - skyboxes.
