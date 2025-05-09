# HOIIVUtils

## Description

This Java application provides a user-friendly interface to interact with your Hearts of Iron 4 mod folder. It offers a variety of tools and utilities designed to enhance your modding experience, making it easier and more efficient.  

We created HOIIVUtils to help with our
["North America Divided"](https://steamcommunity.com/sharedfiles/filedetails/?id=2780506619)
Heats of Iron 4 mod.  
North America Divided: [https://discord.gg/AyJY59BcbM](https://discord.gg/AyJY59BcbM)  
HOIIVUtils: [https://discord.gg/dyakcKQZk9](https://discord.gg/dyakcKQZk9)

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

![image](./images/menu.png)
![image](./images/settings.png)
![image](./images/mangen1.png)
![image](./images/mapgen2.png)
![image](./images/focustree.png)

## Usage
Please keep in mind how **WIP** this is!  
This is in pre-pre-alpha, and we consistently push commits that break the program.   
1. Install [Java 23...](https://adoptium.net/temurin/releases/?version=23&os=any)
     (Use either Eclipse Temurin, OpenJDK, etc., the standard Oracle Java JDK or JRE may not work)
3. Go to [releases...](https://github.com/battleskorpion/HOIIVUtils/releases) and download HOIIVUtils.zip
4. Extract the zip
5. run  .bat or .sh
6. Go to Settings
7. Select your preferred settings  
6. Click Ok

## Technical
Primarily written in Scala, as well as Java. Includes a Clauzewitz scripting language parser, and intermediate 'PDXScript' 
Map Generation includes many options, including multithreading and GPGPU generation (Java Aparapi).
Although tested on both AMD and NVIDIA GPU's, GPU map generation support is *not* guaranteed for AMD cards due to technical limitations. 

## Feedback

Please give us feedback at the [HOIIVUtils's Discord Server...](https://discord.gg/dyakcKQZk9)
