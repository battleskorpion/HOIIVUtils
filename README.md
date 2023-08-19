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
    Close menu window when opening settings window

    Make a drop down for themes (white mode, dark mode, and system default)

    Fix issue with setting window not properly saving when hitting okButton

    Enable/disable intro popup (make sure to save file path)

    create and implement Buildings table in Buildings By Country Window

    fix crash/error when opening Buidings by Country Window
    
    Add multiple file paths with a plus and minus button

<b> assigned todo </b>

very difficult: 
    *fully* implement ui.buildings.BuildingsByCountryWindow

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

    JavaFX Library Files download:
https://gluonhq.com/products/javafx/

    JavaFX .fxml file GUI Scene Builder:
https://gluonhq.com/products/scene-builder/#download

    Vscode path variables:
https://code.visualstudio.com/docs/editor/variables-reference

    IntelliJ path variables:
https://www.jetbrains.com/help/idea/absolute-path-variables.html