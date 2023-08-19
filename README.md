# <h1> hoi4utils.HOIIVUtils </h1>

by battleskorpion

<b>IMPORTANT:</b> Use the batch file to run the hoi4utils.HOIIVUtils.jar, as the jar file tends to immediately close.
<br> Currently you need Java Runtime Environment (JRE) 17 or greater. Java SDK/JDK also should work.

To report bugs mention me in the North America Divided server, or my discord id: BattleSkorpion#0679
<br> hoi4 mod: North America Divided - <hyperlink> https://discord.gg/AyJY59BcbM </hyperlink> 

<b>I  strongly recommend</b>  using git/Github to back up your mod. I assume no responsibility if
this application corrupts/breaks or inadvertently or incorrectly alters any personal files or other, or 
causes any other issues. It <i>should</i> not, but of course, the program is complicated, and by using this 
application, you acknowledge that you understand the risks of use, <i> especially if your mod files are not
backed up in some form.</i>

Some features are WIP, and may not work as intended. 

<b>DEV NOTES</b>

command to run the jar>
java --module-path "lib\javafx-sdk-20.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar out\artifacts\HOIIVUtils.jar
or use the bat

Lowercase = hoi4
Capitlize = HOIIV

todo:
    Enable/disable intro popup (make sure to save file path)
    
    maybe add multiple file paths with a plus and minus button
    
    Make a drop down for themes (white mode, dark mode, and system default)
    
    Add grey text version number on main window and intro window

<b> assigned todo </b>

first day of new internship: 

Change title of mainmenu window from "Hello world". 
Done: 420ms

medium: implement "Focus Localization" button 
    step 1: 
    - get FocusTree file (file chooser) -> in code as file
    - get localization / .yml file -> in code not as type File but as 
    a FocusLocalizationFile, a class I made myself which has the features of File class + more. 
    - once all good call addFocusLoc(focus_file, loc_file); 
    
    step 2: 
    make FocusTreeLocProgress window (you can rename this to like focusTreeLocalizationInfo for example.)
    - this does NOT need a progress bar, localizing one focus is very very fast making it a lil pointness. but sure adding one is extra credit :D
    - needs a Table. 
        -> list of focus names (focus ID's) next to list of localized focus names. 
        HINT: look at public void refreshUnlocFocusesTable and see what data it was feeding into the old table. 

very difficult: 
    *fully* implement ui.buildings.BuildingsByCountryWindow

    - table columns are {"Country", "Population", "Civilian Factories", "Military Factories", "Dockyards", "Airfields",
    "Civ/Mil Ratio", "Pop / Factory", "Pop / Civ Ratio", "Pop Mil Ratio", "Pop / Air Capacity", "Pop / State",
    "Aluminum", "Chromium", "Oil", "Rubber", "Steel", "Tungsten"}

    - validate that each row sorter works when done(alphabetical/numerically)
        - i saw row sorter, its where u click the up or down arrow on the column to sort the rows of that column, javafX should have this functionality like thats pretty normal. 
    - Double.NaN should display as "N/A", not infinity symbol. 

    - should be able to right click on the table, small popup window displays (not proper window, just popup)
        example: ContextMenu class, then add menu items to it. 
        https://stackoverflow.com/questions/20635192/how-to-create-popup-menu
        - action listeners on the 6 different options in this popup menu
        - each option (menu item) is a CheckMenuItem (checkbox but for the lil menu) 
        - each option toggles between displaying as percent and normal of: 
            aluminium, chromium, rubber, oil, steel, and tungsten. 
        - called "Display [resource] as percent" 

    - double clicking on the table, so if e is a MouseEvent then 
    ((e.getClickCount == 2) && !e.isConsumed()) 
    should let u see the very similar CountryBuildingsByStateWindow for the 
    country clicked on
    adapt this code for JavaFX: 
        // get country
        int row = buildingsTable.rowAtPoint( e.getPoint() );
        int modelRow = HOIIVUtils.rowToModelIndex(buildingsTable,row);
        String country_name = (String) buildingsTableModel.getValueA(modelRow,  0);     // column 0 - country name

        CountryTag country = new CountryTag(country_name);

        CountryBuildingsByStateWindow countryBuildingsByStateWindow= new    CountryBuildingsByStateWindow(country);
        countryBuildingsByStateWindow.setVisible(true);

    - reload table data ability (not button no es necessary for cool reasons, i just need a refresh function in code)
    - keep my stateDirWatcher file listener if you can :D, or comment it out for now. 

    - dont worry about the CellColorRenderer, wanna make a way better looking version of what that was doing, but with JavaFX so ya. 

    BTW for data: 
    CountryTag country = countryList.get(i);
    Infrastructure infrastructure = State.infrastructureOfState(State.listFromCountry(country));
    Resources resources = State.resourcesOfStates(StatelistFromCountry(country));
    Resources resourcesAll = State.resourcesOfStates();
    and also
    ArrayList<CountryTag> countryList = CountryTags.list(); for countries list. 


        

Useful links:
    Idea Download:
https://www.jetbrains.com/idea/download/

    Vscode Download:
https://code.visualstudio.com/download

    Java LTS jdk-17:
https://adoptium.net/

    JavaFX Introduction and Setup:
https://openjfx.io/openjfx-docs/#introduction

    JavaFX download:
https://gluonhq.com/products/javafx/

    JavaFX .fxml file GUI Scene Builder:
https://gluonhq.com/products/scene-builder/#download

    Vscode path variables:
https://code.visualstudio.com/docs/editor/variables-reference

    IntelliJ path variables:
https://www.jetbrains.com/help/idea/absolute-path-variables.html