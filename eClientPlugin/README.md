eClient plug-in


## Get the plugin
* Clone the repository: git clone https://github.com/ibm-ecm/ibm-content-navigator-samples.git


## Build the plugin

1. Make sure the ANT is installed.
2. Copy all the dependencies into the **lib** directory.
    * **icncore.jar**
    * **j2ee.jar**
3. cd icn-plugins/AFP2PDFPlusPlugin
4. Run Ant Build using ibm-content-navigator-samples/eClientPlugin/build.xml
	* eClientPlugin.jar is generated under ibm-content-navigator-samples/eClientPlugin

## Install the plugin
1. Login IBM Content Navigator and open the Admin desktop
2. Go to Plug-Ins in Admin desktop
3. New Plug-in. Input the full path of eClientPlugin.jar in the "JAR file path" and click "Load" button.
4. Click Save.  


