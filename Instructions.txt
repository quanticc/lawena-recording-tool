lawena Recording Tool
---------------------

* Run lawena.exe, a splash screen should appear.
* Choose your tf directory, for instance "\Steam\steamapps\common\Team Fortress 2\tf".
* Choose your moviefile directory, where the tool will store everything you record.
* Configure it as you like, also you can save settings to a file and edit them later.
* Start TF2, lawena will make a backup of your cfg and custom folder while TF2 is running.
* Load a demo and press P to start recording.
* When you close TF2, your files should be restored and should be no "lwrt" folders inside your tf folder.
* If lawena is not closed properly, running it once should restore all folders. Your files are inside the "lwrtcfg" and "lwrtcustom" folders.

* To use a custom HUD for recording, copy said HUD's resource and scripts folders into "\hud\custom" and then choose Custom HUD in the HUD settings.
* To use a custom Skybox for recording, simply copy said Skybox's .vtf files (not the .vmt files) into "\Skybox". The tool will automatically generate an option in the settings and a preview screenshot of the skybox.

The tool provides the following keybindings:

N - Locks viewmodels so that if they were enabled when you press the key, they stay on and can't be disabled, and viceversa. This setting is overridden by restarting TF2
M - Locks default crosshair so that it cannot be changed. This setting is overridden by restarting TF2
R - Enables/disables ragdolls.
UP ARROW - Increases the recording frame rate
DOWN ARROW - Decreases the recording frame rate
P - Starts recording a series of .tga files and their respective .wav file. Each series is named XY_ where X is a letter and Y is a number between 1 and 15 


Special Thanks to Montz for this tool, Chris for his awesome config and Broesel, m0re, povohat and Barrakketh for their HUDs. Also thanks to Komaokc for his beautiful skyboxes.