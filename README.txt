lawena Recording Tool
---------------------

 Instructions

   * Run lawena.exe, the updater should run and it will automatically launch the tool
   * Your TF2 directory should be auto-detected (you can always change it from the "File" menu in the UI)
   * Choose your moviefile directory, where the tool will store everything you record
   * Configure it as you like, also you can save settings to a file and edit them later
   * Start TF2, lawena will make a backup of your cfg and custom folder while TF2 is running
   * Load a demo and press P to start recording
   * When you close TF2, your files should be restored (there should be no "lwrt" folders inside your tf folder)


 Adding custom resources

   * This version will detect all VPKs and folders inside your "tf/custom" folder, so just by having your files there you'll have the option to load them when lawena launches TF2
   * You can also use the "custom" folder of lawena, where some default VPK were included to show how it works
   * If you select your custom HUD in the custom resources list, you'll have to select "Custom" as your selected HUD option or there could be conflicts
   * If you want to add extra skyboxes, put the .vtf files (not the .vmt) inside the "skybox" folder of lawena and it will load along the included

 Troubleshooting

   * If lawena is not closed properly, running it once should restore all folders. Your files are inside the "lwrtcfg" and "lwrtcustom" folders
   * Please report any issue you might find on https://github.com/iabarca/lawena-recording-tool/issues where you can also suggest new features
   

 Keybindings

   * N - Locks viewmodels so that if they were enabled when you press the key, they stay on and can't be disabled, and viceversa
   * M - Locks default crosshair so that it cannot be changed
   * R - Enables/disables ragdolls
   * UP ARROW - Increases the recording frame rate
   * DOWN ARROW - Decreases the recording frame rate
   * P - Starts recording a series of .tga files and their respective .wav file.
     Each series is named XY_ where X is a letter and Y is a number between 1 and 15
   * Numpad Plus - Switch to third person
   * Numpad Minus - Switch back to first person
   * Numpad 2 - Back view
   * Numpad 8 - Front view
   * Numpad 5 - Toggle camera distance
   * Numpad 0 - Toggle camera pitch
   * Numpad 1 - Back-Left view
   * Numpad 4 - Left view
   * Numpad 7 - Front-Left view
   * Numpad 3 - Back-Right view
   * Numpad 6 - Right view
   * Numpad 9 - Front-Right view

Credits and Thanks

   * Montz - original lawena developer.
   * Chris Down - configs.
   * mih - HUDs.
   * Komaokc - skyboxes.

