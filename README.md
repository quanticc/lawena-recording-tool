lawena Recording Tool [![Build Status](https://travis-ci.org/iabarca/lawena-recording-tool.png)](https://travis-ci.org/iabarca/lawena-recording-tool)
=====================

Simple Team Fortress 2 (TF2) movie recording tool. Original project by Montz at [Google Code](http://code.google.com/p/lawenarecordingtool/). The project source and issue tracking is now hosted here on GitHub. It requires [Java 1.7](http://www.java.com) and it's currently being maintained by [Quantic](http://steamcommunity.com/id/thepropane).

#### Download lawena (with auto-updates)
- Get the [Launcher](http://code.google.com/p/lawenarecordingtool/downloads/detail?name=lawena-recording-tool.v4.zip)
- Extract to any folder and run **lawena.exe**, on first run it will get all the base files and then it will update whenever a new version comes up.

#### New features in this version
- Renewed graphical user interface
- Ability to load custom materials/models/skins/etc as folders or VPKs
- Included PLDX enhanced particles
- Many included resources are now packed into VPKs
- Auto-updates using the launcher
- Linux TF2 support

#### Build from repository
- Clone or download this repository
- Use [Gradle](http://www.gradle.org/) to build the jar with the ``gradle jar`` command, then you can double-click **lawena.jar**
- Alternatively, on Windows you can build it with "build.bat", and on Linux using "build.sh". You can then run the tool using "lawena.bat" or "lawena.sh" respectively.
- Updates using ``git pull``

#### Instructions
- Run lawena.exe, the updater should run and it will automatically launch the tool
- Your TF2 directory should be auto-detected (you can always change it from the "File" menu in the UI)
- Choose your moviefile directory, where the tool will store everything you record
- Configure it as you like, also you can save settings to a file and edit them later
- Start TF2, lawena will make a backup of your cfg and custom folder while TF2 is running
- Load a demo and press P to start recording
- When you close TF2, your files should be restored (there should be no "lwrt" folders inside your tf folder)

#### Adding custom resources
- This version will detect all VPKs and folders inside your "tf/custom" folder, so just by having your files there you'll have the option to load them when lawena launches TF2
- You can also use the "custom" folder of lawena, where some default VPK were included to show how it works
- If you select your custom HUD in the custom resources list, you'll have to select "Custom" as your selected HUD option or there could be conflicts
- If you want to add extra skyboxes, put the .vtf files (not the .vmt) inside the "skybox" folder of lawena and it will load along the included

#### Troubleshooting
- If lawena is not closed properly, running it once should restore all folders. Your files are inside the "lwrtcfg" and "lwrtcustom" folders
- Please report any [issue](https://github.com/iabarca/lawena-recording-tool/issues) you might find. Also you can use that same page to suggest new features.

#### Keybindings
- **N** - Locks viewmodels so that if they were enabled when you press the key, they stay on and can't be disabled, and viceversa
- **M** - Locks default crosshair so that it cannot be changed
- **R** - Enables/disables ragdolls
- **UP ARROW** - Increases the recording frame rate
- **DOWN ARROW** - Decreases the recording frame rate
- **P** - Starts recording a series of .tga files and their respective .wav file. Each series is named XY_ where X is a letter and Y is a number between 1 and 15
- **NumPad Keys** - Third-person camera control (angle, position, distance). More details in the included README.txt file.

#### Credits and Thanks
- Montz - original lawena developer.
- Chris Down - configs.
- Broesel, m0re, povohat and Barrakketh - HUDs.
- Komaokc - skyboxes.

