
buzztouch v1.4, Android Edition
--------------------------------

Using Eclipse and the Android SDK to compile your app.

1)	Launch Eclipse. Eclipse must have the Android Developer Tools installed.
	Get the Android SDK for Eclipse here: http://developer.android.com/sdk/index.html
	
2) 	Create a new project. File > New > Android Project

3) 	Enter a project name

4) 	Choose "Create a project from existing source"

5) 	Using the "Browse" button, locate the source-code folder for your application.
	This is the folder you downloaded from buzztouch.com then un-zipped to a directory on your computer.
	Note: After choosing the source-code folder on your computer, a Google API's version 1.6 checkbox
	should be checked. Eclipse should check this automatically. If it did not, something is wrong.
	
6) 	Click Finish.
	
	YOUR APPLICATION SHOULD COMPILE AND INSTALL ON AN ANDROID SIMULATOR OR DEVICE. 
		
		
	ABOUT SHARE ON FACEBOOK SCREENS: 
		You will need to enter your Facebook App API info. This means you may need to setup
		a Facebook Application (easy, free) on Facebook before doing this. After getting your
		Facebook app configured, open res/values/strings.xml file in this project and
		enter the Facebook API info.

	ABOUT GOOGLE MAPS:
		Before you can use map functions, you will need to obtain two Google Map API Keys, one
		for debugging, and one for release. After obtaining the API keys from Google (free), 
		enter them in the res/values/strings.xml file in this project.
		
		See this URL for more info about Google Maps API keys:
		http://code.google.com/android/add-ons/google-apis/maps-overview.html		
		
		After entering your API Keys in the strings.xml file, you will need to "uncomment" two
		parts of the source code:
		
		1) In the AndroidManifest.xml (in the root of the project), uncomment the Google
		map library information by removing	the <!-- and the --> near the very end of
		AndroidManifest.xml file, all the way at the bottom, it looks like this:
     	
     	<!--  
       		<uses-library android:name="com.google.android.maps" />
       		<activity android:name=".Screen_LocationMap" android:label="@string/app_name" android:screenOrientation="sensor"></activity>
        -->
			
		2) In the Act_ActivityBase.java file, inside the src/yourProjectName folder, 
			in the ShowLocationMap() function, uncomment the warning and the function call.
			It looks like this (it's about half way down the file)
			
			//showLocationMap
			public void showLocationMap(){

				/* 
				Intent theIntent = new Intent(this, Screen_LocationMap.class);
        		startActivity(theIntent);
        		*/
				showAlert("Maps Not Configuredr", "Maps not configured properley. See: showLocationMap() method in src/Act_ActivityBase.java");
			}
			
			Remove the /* and the */ and the "showAlert" warning.
		
		
		
		
		
		
		
		
		
		
		
		
		

	
	
	