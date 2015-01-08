Lawena Recording Tool [![Build Status](https://travis-ci.org/iabarca/lawena-recording-tool.svg?branch=v4.x)](https://travis-ci.org/iabarca/lawena-recording-tool)
=====================

A simple [Java 7+](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) tool which greatly enhances your TF2 and other Source games image quality for recording purposes, with surprisingly little performance loss compared to other such configurations. Records using ingame Source Recorder, with key bindings to ease the process.

Easy-to-use and no install required. This tool does not interfere with your regular TF2 configuration, HUD or launch options. Requires [Steam](https://steamcommunity.com/) in order to run.

## Installing Lawena
If you want to use a stable version, check the [releases](https://github.com/iabarca/lawena-recording-tool/releases) page. Also there's some pre-releases that preview upcoming content. To download the latest, not particularly stable version:

1. [Download this repository](https://github.com/iabarca/lawena-recording-tool/archive/v4.x.zip) and extract it to any folder.
2. Run **gradlew.bat** (Windows) or **gradlew** script (Unix) to start the program. Administrative permissions are required due to the use of symbolic links.

## About this branch
The v4.x branch contains the latest development towards a v4.2.0 release. You can switch branches from within the tool using the `Help -> Switch Updater Branch...` menu item.

### Features
* Insane max quality graphics configuration. 
* Recorded files are saved to the directory of your choosing for comfort and order.
* Options to enable/disable various game elements which may be unnecessary in the context of a movie.
* Includes built-in moviehuds with different levels of simplicity.
* Supports custom HUDs/materials/models/etc to add a personal touch to your movies.
* Includes a set of built-in skyboxes, and supports custom skyboxes via simple drag-and-drop.
* [SrcDemo2](https://code.google.com/p/srcdemo2/) support to reduce drive usage and space.
* [VDM](https://developer.valvesoftware.com/wiki/Demo_Recording_Tools) file support for automatizing your movie recording process.
* Updates automatically, you can see the latest changes [here](https://github.com/iabarca/lawena-recording-tool/commits/v4.x).

### Recommended links
* [Instructions](http://code.google.com/p/lawenarecordingtool/wiki/Instructions) on how to use the tool, how to add custom resources, keybindings and Frequently Asked Questions.
* [VDM Tutorial](http://code.google.com/p/lawenarecordingtool/wiki/VDMtutorial) to automate your recording process
* [Rendering Tutorial](http://code.google.com/p/lawenarecordingtool/wiki/RenderingTutorial) using [VirtualDub](http://www.virtualdub.org/)
* [Issues](https://github.com/iabarca/lawena-recording-tool/issues) to report anything wrong and also to suggest new features.
* [Wiki](https://github.com/iabarca/lawena-recording-tool/wiki) within GitHub is under construction!

### Credits
* Original project by Montz (currently inactive) at [Google Code](http://code.google.com/p/lawenarecordingtool/)
* Current developer: Quantic ([steam](http://steamcommunity.com/id/thepropane))
* Graphical .cfg files based on those made by Chris Down.
* Built-in Killnotices and Medic HUD made by [mih](http://steamcommunity.com/profiles/76561198023136325) [[repo](https://github.com/Kuw/recordinghuds)]
* Skyboxes made by komaokc from GameBanana.
* Includes some content used in PLDX recording tool.
* Valve - Team Fortress and Team Fortress logo.
