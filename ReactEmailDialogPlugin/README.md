This plugin is a sample demostrate how to develop ICN Plugin UI using React.
User can customize ICN UI using React after read this sample.



## Get the plugin
* Clone the repo: git clone https://github.com/ibm-ecm/ibm-content-navigator-samples.git


## Build the plugin

1. Make sure the NPM and ANT are installed.
2. Copy all the dependencies into the **lib** directory.
    * **navigatorAPI.jar**
    * **j2ee.jar**
3. cd ibm-content-navigator-samples/ReactEmailDialogPlugin
4. Open build.xml. Search for "npmPath" at line 5. Update your NPM path in the value if the path is not default NPM path on your environment.
5. ant -f build.xml 

ReactEmailDialogPlugin.jar is generated under ibm-content-navigator-samples/ReactEmailDialogPlugin

## Install the plugin
1. Login IBM Content Navigator and open the Admin desktop
2. Goto Plug-Ins in Admin desktop
3. New Plug-in. Input the ReactEmailDialogPlugin.jar full path in the "JAR file path" and click "Load" button.
4. There should be some actions with "React" as name prefix.  Save.


## Configure ICN menu using the plugin.
1. Goto Menus in Admin desktop.
2. Find and select the "Default document context menu". (Search "default document" by filter)
3. Click Copy button on toolbar.
4. Give the copied menu a name like "React Email".  Click "Add Submenu" and create a sub menu named "React Email". Add actions with "React" from available list to "React Email" submenu. Save.
5. Goto Desktops in Admin desktop.Edit your desktop.
6. Goto "Menus" tab and find the "Document context menu"
7. Select "React Email" from the drop down list.  Save

Refresh browser. You will see the "React Email" items from context menu when you right click on documents.

There is a little behavior change on removing attachment from the email dialog comparing to the OOTB ICN Email dialog. It require at least one document on the email dialog. User is not able to remove last document from the email dialog.
