# HOIIVUtils

## Install
#### You will need an OpenJDK 24. (Here is what we use: https://adoptium.net/temurin/releases/?os=any&arch=x64&version=24).  
#### Download the latest release HOIIVUtils.zip file on the GitHub release's page https://github.com/battleskorpion/HOIIVUtils/releases/latest.  
#### Extract the HOIIVUtils.zip file anywhere, Desktop, Documents, Etc.

## Run
#### Open the HOIIVUtils Folder you extracted from the zip.
#### Double-click on the HOIIVUtils.bat file if you are on windows.
#### WARNING, You need Hearts of Iron 4 installed on your system.
Report any bugs to the discord.
## Description

This Java application provides a user-friendly interface to interact with your Hearts of Iron 4 mod folder. It offers a variety of tools and utilities designed to enhance your modding experience, making it easier and more efficient.  

We created HOIIVUtils to help with our
["North America Divided"](https://steamcommunity.com/sharedfiles/filedetails/?id=2780506619)
Hearts of Iron 4 mod.  
North America Divided: [https://discord.gg/AyJY59BcbM](https://discord.gg/AyJY59BcbM)  
HOIIVUtils Official Discord: [https://discord.gg/dyakcKQZk9](https://discord.gg/dyakcKQZk9)

**WIP!** Features are in various stages of completion. Including: 

- Demo Mod
- Keybindings
- Open Logs
- Focuses and Ideas (National Modifiers)
- Manage Focus Trees
- Localization automation features (Auto-generated localization for focuses, ideas)
     - Custom Tooltip creation
- View Country and View State Data (Buildings, Air Capacity, Population, Infrastructure)
- View GFX 
- Focus Tree View/Editor
- Unit Comparison (Base Game, 'Vanilla' vs. Modified) 
- Unique Province Color Generator 
- Map Generation (Basic, Multithreaded, GPU-accelerated options) 
- Parser View
- Everything :D

![image](/images/focustree.gif)
![image](/images/mapgeneration.gif)
![image](/images/parserview.gif)
![image](/images/countrydata.gif)
![image](/images/settings.png)


## Technical
Primarily written in Scala, as well as Java. Includes a Clauzewitz scripting language parser, and intermediate 'PDXScript' 
Map Generation includes many options, including multithreading and GPGPU generation (Java Aparapi).
Although tested on both AMD and NVIDIA GPU's, GPU map generation support is *not* guaranteed for AMD cards due to technical limitations. 

## Feedback

Please give us feedback at the [HOIIVUtils's Discord Server...](https://discord.gg/dyakcKQZk9)
