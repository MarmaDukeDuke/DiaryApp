package com.v1_4.mydiaryapp.com;

import java.io.File;
import java.util.List;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;


public class Act_ActivityBase extends Activity implements LocationListener{

	public AppDelegate appDelegate = (AppDelegate) this.getApplication();
	public String thisActivityName = "Act_ActivityBase";
	public AlertDialog myAlert;
	public ProgressDialog myProgressBox;
	public MyDownloadThread myDownloadThread;
	public String myDownloadText;
	public boolean binaryDownloadDone;
	public String saveBinaryFileName;
	public Bitmap myDownlaodImage;
	public Bitmap saveImageFileName;    
	
	
	//location updates
	private LocationManager locationManager;
	private int locationUpdateCount = 0;
	
	//remember current screen
	public Obj_Screen selectedScreen;
	
	//for data downloads / saves
	public String appGuid = "";
	public String appApiKey = "";
	public String screenGuid = "";
	public String saveAsFileName = "";
	public String remoteDataCommand = "";
	public String JSONData = "";
    
	//hidden / showing
	public static final int INVISIBLE = 4;
	public static final int VISIBLE = 0;	
	
	
	//show alert message
	public void showAlert(String theTitle, String theMessage) {
		if(theTitle == "") theTitle = "No Alert Title?";
		if(theMessage == "") theMessage = "No alert message?";
		myAlert = new AlertDialog.Builder(this).create();
		myAlert.setTitle(theTitle);
		myAlert.setMessage(theMessage);
		myAlert.setIcon(R.drawable.icon);
		myAlert.setCancelable(false);
		myAlert.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	        myAlert.dismiss();
	    } }); 
		myAlert.show();
	}	
				
	//show / hide progress
	public void showProgress(String theTitle, String theMessage){
		myProgressBox = ProgressDialog.show(this, theTitle, theMessage, true);
	}
	public void hideProgress(){
		if(myProgressBox != null){
			myProgressBox.dismiss();
		}
	}
	
	//show info
	public void showInfo(){
      	Intent theIntent = new Intent(this, Screen_About.class);
        startActivity(theIntent);
	}
	
	//menuTap
	public void menuTap(Obj_Screen theScreen){
		Log.i("ZZ", thisActivityName + ":menuTap: " + theScreen.screenType);      

		//remember selected screen
	    selectedScreen = theScreen;
		appDelegate.currentScreen = theScreen;
	   
	    //should probably create ENUM for this (Java won't do SWITCH with String values)
	    if(theScreen.screenType.equals("screen_subMenu")) showSubMenu();
	    if(theScreen.screenType.equals("screen_rssFeed")) showRssFeed();
	    if(theScreen.screenType.equals("screen_rssReader")) showRssFeed();
	    if(theScreen.screenType.equals("screen_url")) showCustomURL();
	    if(theScreen.screenType.equals("screen_customText")) showCustomText();
	    if(theScreen.screenType.equals("screen_customHTML")) showCustomHTML();
	    if(theScreen.screenType.equals("screen_customHtml")) showCustomHTML();
	    if(theScreen.screenType.equals("screen_call")) placeCall();
	    if(theScreen.screenType.equals("screen_email")) sendEmail();
	    if(theScreen.screenType.equals("screen_shareEmail")) shareEmail();
	    if(theScreen.screenType.equals("screen_shareTwitter")) shareTwitter();
	    if(theScreen.screenType.equals("screen_shareFacebook")) shareFacebook();
	    if(theScreen.screenType.equals("screen_locationMap")) showLocationMap();
	    if(theScreen.screenType.equals("screen_multipleLocationMap")) showLocationMap();
	    if(theScreen.screenType.equals("screen_multiLocationMap")) showLocationMap();
	    if(theScreen.screenType.equals("screen_video")) playVideo();
	    if(theScreen.screenType.equals("screen_quizMultipleChoice")) showQuizMultipleChoice();
	    if(theScreen.screenType.equals("screen_quizScores")) showQuizScores();
	    if(theScreen.screenType.equals("screen_singleImage")) showSingleImage();
	}
	
	//menu-tap methods

	//showAppGroupList
	public void showAppGroupList(){
		showAlert("App Group List", "App Group List not supported on Android");
	}
	
	//showCustomText
	public void showCustomText(){
		Intent theIntent = new Intent(this, Screen_CustomText.class);
		startActivity(theIntent);
	}
	
	//showCustomUrl
	public void showCustomURL(){
		//intent depends on jsonStringOptions content..
		int useMyIntent = 1;
		int bolDone = 0;
		String useUrl = "";
		String useScreenGuid = "";
		
		try{
			
			String jsonString = selectedScreen.jsonScreenOptions;		
			JSONObject raw = new JSONObject(jsonString);
			Log.i("ZZ", thisActivityName + ":showCustomURL " + jsonString);   
			useUrl = raw.getString("url");
			
			try{
				useScreenGuid = raw.getString("screenGuid");
			}catch (Exception ex){
				//ignore
			}
			if(useScreenGuid.length() < 1){
				try{
					useScreenGuid = raw.getString("guid");
				}catch(Exception ex_2){
					//ignore..
				}
			}

    		//url contains youtube
    		if(jsonString.contains("youTubeVideo") || useUrl.contains("youtube.com")){
    			useMyIntent = 0;
    		}
			
      		//url contains soundcloud
    		if(jsonString.contains("soundcloud.com")){
    			useMyIntent = 0;
    		}
 			
    		
    		//url contains .pdf
    		if(useUrl.contains(".pdf") || useUrl.contains(".PDF")){
    			bolDone = 1;
    	
    			//if PDF is local...
    			saveBinaryFileName = useScreenGuid + ".pdf";
    			if(appDelegate.fileExists(saveBinaryFileName)){
    				showPDF(saveBinaryFileName);
    			}else{
    				//download pdf...
    				showProgress("Downloading .PDF Doc", "Downloads are saved for faster viewing next time. \n\nWi-Fi is recommended for large downloads.");
    				myDownloadThread = new MyDownloadThread();
    				myDownloadThread.setDownloadURL(useUrl);
    				myDownloadThread.setSaveAsFileName(saveBinaryFileName);
    				myDownloadThread.setDownloadType("pdf");
    				myDownloadThread.setThreadRunning(true);
    				myDownloadThread.start();
    			}
    		}
    		
    		//url contains .doc
    		if(useUrl.contains(".doc") || useUrl.contains(".DOC")){
    			bolDone = 1;
    			
    			//if DOC is local...
    			saveBinaryFileName = useScreenGuid + ".doc";
    			if(appDelegate.fileExists(saveBinaryFileName)){
    				showMSWord(saveBinaryFileName);
    			}else{
    				//download doc...
    				showProgress("Downloading Word .DOC", "Downloads are saved for faster viewing next time. \n\nWi-Fi is recommended for large downloads.");
    				myDownloadThread = new MyDownloadThread();
    				myDownloadThread.setDownloadURL(useUrl);
    				myDownloadThread.setSaveAsFileName(saveBinaryFileName);
    				myDownloadThread.setDownloadType("doc");
    				myDownloadThread.setThreadRunning(true);
    				myDownloadThread.start();
    			}
    		
    		}
    		
    		//url contains imageSingle
    		if(useUrl.contains("screen_imageSingle")){
    			bolDone = 1;
    			showSingleImage();
    		}
    		
     	  	//if we are using our own intent..launch a Screen_CustomURL activity...
     	  	if(useMyIntent == 1 && bolDone == 0){
    			Intent theIntent = new Intent(this, Screen_CustomURL.class);
    			startActivity(theIntent);
    		}
     	  	
     	  	//let phone decide intent
     	  	if(bolDone == 0 && useMyIntent == 0){
    			//let device determine which intent to launch...
    			Intent theIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(useUrl));
    			startActivity(theIntent);
    		}  		
    		
    		
    		
 	  	}catch (Exception je){
 			showAlert("Error Processing", "There was a problem determining what screen to show?" + je.toString());
 	  	};
    }
	
	
	//showCustomHtml
	public void showCustomHTML(){
		Intent theIntent = new Intent(this, Screen_CustomHTML.class);
		startActivity(theIntent);
	}
	
	//shareFacebook
	public void shareFacebook(){
		Intent theIntent = new Intent(this, Screen_ShareFacebook.class);
        startActivity(theIntent);
	}

	//shareTwitter
	public void shareTwitter(){
		Intent theIntent = new Intent(this, Screen_ShareTwitter.class);
        startActivity(theIntent);
	}

	//shareEmail
	public void shareEmail(){
		try{
			JSONObject raw = new JSONObject(selectedScreen.jsonScreenOptions);
			String emailSubject = raw.getString("emailSubject");
			String emailMessage = raw.getString("emailMessage");

			try{
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent .setType("plain/text");
				intent .putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
				intent .putExtra(android.content.Intent.EXTRA_TEXT, emailMessage);
				//intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/"+ sPhotoFileName));

				startActivity(intent);
			}catch(Exception e) {
				showAlert("Email Error", "There was a problem launching the email composer. Your device may not be configured to send email?");
			}			
 	  	}catch (Exception je){
 	  		
 	  	};
	
	}

	//showLocationMap
	public void showLocationMap(){

		/*
		Intent theIntent = new Intent(this, Screen_LocationMap.class);
        startActivity(theIntent);
        */
		showAlert("Maps Not Configured", "Maps not configured properley. See: showLocationMap() method in src/Act_ActivityBase.java");

	}
	

	//placeCall
	public void placeCall(){
		try{
			JSONObject raw = new JSONObject(selectedScreen.jsonScreenOptions);
			String number = raw.getString("number");
			
			try{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("tel:" + number));

				startActivity(intent);
			}catch(Exception e) {
				showAlert("Call Error", "There was a problem starting the phone call. Is the number valid?");
			}			
			
 	  	}catch (Exception je){
 	  		
 	  	};
		
	}
	
	//sendEmail
	public void sendEmail(){
		try{
			JSONObject raw = new JSONObject(selectedScreen.jsonScreenOptions);
			String emailToAddress = raw.getString("emailToAddress");
			String emailSubject = raw.getString("emailSubject");
			try{
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent .setType("plain/text");
				intent .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailToAddress});
				intent .putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
				startActivity(intent);
			}catch(Exception e) {
				showAlert("Email Error", "There was a problem launching the email composer. Your device may not be configured to send email?");
			}			
 	  	}catch (Exception je){
 	  		
 	  	};
		
	}
	
	//showRssFeed
	public void showRssFeed(){
		Intent theIntent = new Intent(this, Screen_RSSReader.class);
        startActivity(theIntent);
    }
	
	//showSubMenu
	public void showSubMenu(){
		Intent theIntent = new Intent(this, Screen_SubMenu.class);
        startActivity(theIntent);
	}	
	
	//startQuizMultipleChoice
	public void showQuizMultipleChoice(){
		Intent theIntent = new Intent(this, Screen_QuizMultipleChoice.class);
        startActivity(theIntent);		
	}

	//showQuizScores
	public void showQuizScores(){
		Intent theIntent = new Intent(this, Screen_QuizScores.class);
        startActivity(theIntent);
	}
	
	//playVideo
	public void playVideo(){
		Intent theIntent = new Intent(this, Screen_Video.class);
        startActivity(theIntent);
	}
	
	//playAudio
	public void playAudio(){
		showAlert("Audio", "Audio screen under construction");
	}	
	
	//showSingleImage
	public void showSingleImage(){
		Intent theIntent = new Intent(this, Screen_SingleImage.class);
        startActivity(theIntent);
	}
	
	//END MENU TAP METHODS
	
	   
	////////////////////////////////////////////////////////////////////
    //location methods
    public void getLastLocation(){
        try{
        	//only ask for location info "once" when app launches (saves battery)
			if(appDelegate.foundUpdatedLocation == 0){
				locationUpdateCount = 0;
        	
	        	if(this.locationManager == null){
	        		this.locationUpdateCount = 0;
	        		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);       
	        	}
	        	if(this.locationManager != null){
	            	Location lastLocation = this.locationManager.getLastKnownLocation("gps");
	            	if(lastLocation != null){
	            	    
	            		//remember in delegate
	            		appDelegate.currentDevice.deviceLatitude = String.valueOf(lastLocation.getLatitude());
	        		    appDelegate.currentDevice.deviceLongitude = String.valueOf(lastLocation.getLongitude());
	        		    appDelegate.currentDevice.deviceAccuracy = String.valueOf(lastLocation.getAccuracy());
	        		    

	            		String s = "";
	            		s += "-Last Location Time: " + lastLocation.getTime();
	            		s += " Last Location Latitude: " + lastLocation.getLatitude();
	            		s += " Last Location Longitude: " + lastLocation.getLongitude();
	            		s += " Last Location Accuracy: " + lastLocation.getAccuracy();
	            		//Log.i("ZZ", s);
	            	}
	            	
	            	//start listening for location updates if we have GPS enabled...
	            	if(this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
	            		startListening();
	            	}else{
	            	    Log.i("ZZ", thisActivityName + ":getLastLocation: GPS not available");      
	            	}
	            	
	        	}
			}
        }catch (Exception je){
	        Log.i("ZZ", thisActivityName + ":getLastLocation: ERROR: " + je.getMessage());      
	    }
    }
	
	//LocationListener must implement these methods
	public void onProviderDisabled(String theProvider){};    
	public void onProviderEnabled(String theProvider){};
	public void onLocationChanged(Location location){
		this.locationUpdateCount++;
        //Log.i("ZZ", thisActivityName + ":onLocationChanged");
  		
		//remember in delegate
		appDelegate.currentDevice.deviceLatitude = String.valueOf(location.getLatitude());
	    appDelegate.currentDevice.deviceLongitude = String.valueOf(location.getLongitude());
	    appDelegate.currentDevice.deviceAccuracy = String.valueOf(location.getAccuracy());
		
		String s = "";
		s += "-Updated Location Time: " + location.getTime();
		s += " Updated Location Latitude: " + location.getLatitude();
		s += " Updated Location Longitude: " + location.getLongitude();
		s += " Updated Location Accuracy: " + location.getAccuracy();
		//Log.i("ZZ", s);
		
		//stop listening to save battery if...
		//we have been listenign for about 5 seconds
		//accurate up to 30 meters.
		if(locationUpdateCount > 5 || location.getAccuracy() < 30){
			stopListening();
			appDelegate.foundUpdatedLocation = 1;
		}
 	};
	public void onStatusChanged(String theProvider, int status, Bundle extras){
        //Log.i("ZZ", thisActivityName + ":onStatusChanged");      
	};
	//start listening..
	public void startListening() {
		if(this.locationManager != null){
			this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}
    }
	//stop listening..
	public void stopListening() {
        if(this.locationManager != null){
        	this.locationManager.removeUpdates(this);
        }
    }
	//end location methods
	////////////////////////////////////////////////////////////////////


	//is intent available?
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}	

	
	////////////////////////////////////////////////////////////////////
	//downloads files in background thread..
	public class MyDownloadThread extends Thread{
		 boolean threadRunning = false;
		 String downloadURL = "";
		 String saveAsFileName = "";
		 String downloadType = "";
		 void setThreadRunning(boolean bolRunning){
			 threadRunning = bolRunning;
		 }		
		 void setDownloadURL(String theURL){
			 downloadURL = theURL;
		 }
		 void setSaveAsFileName(String theFileName){
			 saveAsFileName = theFileName;
		 }
		 void setDownloadType(String imageOrText){
			 downloadType = imageOrText;		 
		 }
    	 @Override 
    	 public void run(){
			
    		 //text downloads
			if(downloadType == "text"){
				myDownloadText = appDelegate.downloadText(downloadURL);
				appDelegate.saveText(saveAsFileName, myDownloadText);
				MyDownloadTextHandler.sendMessage(MyDownloadTextHandler.obtainMessage());
				this.setThreadRunning(false);
			}
			
   		 	//image downloads
			if(downloadType == "image"){
				myDownlaodImage = appDelegate.downloadImage(downloadURL);
				appDelegate.saveBitmap(saveAsFileName, myDownlaodImage);
				MyDownloadImageHandler.sendMessage(MyDownloadImageHandler.obtainMessage());
				this.setThreadRunning(false);
			}			
			
   		 	//binary downloads (pdf, word, etc)
			if(downloadType == "pdf" || downloadType == "word"){
				binaryDownloadDone = appDelegate.downloadAndSaveBinaryFile(downloadURL, saveAsFileName);
				MyDownloadPDFHandler.sendMessage(MyDownloadPDFHandler.obtainMessage());
				this.setThreadRunning(false);
			}
   		 	//word downloads
			if(downloadType == "doc"){
				binaryDownloadDone = appDelegate.downloadAndSaveBinaryFile(downloadURL, saveAsFileName);
				MyDownloadWordHandler.sendMessage(MyDownloadWordHandler.obtainMessage());
				this.setThreadRunning(false);
			}			
    	 }
	}	
	//end download thread
	
	//after download text files....
	Handler MyDownloadTextHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			Log.i("ZZ", thisActivityName + ":MyDownloadTextHandler complete...");   
			if(myDownloadText.length() < 1){
				hideProgress();
				showAlert("Error Downloading Data?", "Please check your internet connection then try again.");
			}else{
				//handle string...
				// myDownloadText is data...
			}
		}
	};
	//after download image files....
	Handler MyDownloadImageHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			Log.i("ZZ", thisActivityName + ":MyDownloadImageHandler complete...");   
			hideProgress();
			if(myDownlaodImage != null){
				showAlert("Error Downloading Image?", "Please check your internet connection then try again.");
			}else{
				//handle image...
				// myDownloadImage is Bitmap
			}
		}
	};	
	//after download .PDF files....
	Handler MyDownloadPDFHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			Log.i("ZZ", thisActivityName + ":MyDownloadPDFHandler complete...");   
			hideProgress();
			if(!binaryDownloadDone){
				showAlert("Error Downloading PDF?", "Please check your internet connection then try again. If you're sure you have a good connection, there may be a problem with the server?");
			}else{
				//show PDF
				showPDF(saveBinaryFileName);
			}
		}
	};	
	Handler MyDownloadWordHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			Log.i("ZZ", thisActivityName + ":MyDownloadWordHandler complete...");   
			hideProgress();
			if(!binaryDownloadDone){
				showAlert("Error Downloading .DOC?", "Please check your internet connection then try again. If you're sure you have a good connection, there may be a problem with the server?");
			}else{
				//show MS Word
				showMSWord(saveBinaryFileName);
			}
		}
	};		
	////////////////////////////////////////////////////////////////////
	
	//show's PDF from file system
	public void showPDF(String theFileName){
		//try to open pdf in device's default intent...
        //Log.i("ZZ", thisActivityName + ":showPDF: " + theFileName);      
		if(appDelegate.fileExists(theFileName)){
	        //Log.i("ZZ", thisActivityName + ":showPDF: EXISTS: " + theFileName); 
			try{
	    		PackageManager pm = getPackageManager();
	    		ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
				File theFile = new File(appInfo.dataDir + "/files/" + theFileName);
				//test intent 
	    		Intent testIntent = new Intent(Intent.ACTION_VIEW);
				testIntent.setType("application/pdf");
				//see if we have an intent for this type of doc...
				List list = pm.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
				if(list.size() > 0 && theFile.isFile()){
					Intent intent = new Intent();
				    intent.setAction(Intent.ACTION_VIEW);
				    Uri uri = Uri.fromFile(theFile);
				    intent.setDataAndType(uri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    startActivity(intent);
				 }else{
					 showAlert(".PDF Not Supported", "Your device does not support reading .PDF files");
				 }
	   		}catch (Exception je){
				 showAlert("Error Opening .PDF", "Your device does not support reading .PDF files");
	        }			
		}else{
			showAlert("Error Saving .PDF?", "An error occured while trying to save the document? Please check your internet connection then try again.");
		}
	}
	
	//show's WORD from file system
	public void showMSWord(String theFileName){
			//try to open pdf in device's default intent...
	        Log.i("ZZ", thisActivityName + ":showMSWord: " + theFileName);      
			if(appDelegate.fileExists(theFileName)){
		        //Log.i("ZZ", thisActivityName + ":showPDF: EXISTS: " + theFileName); 
				try{
		    		PackageManager pm = getPackageManager();
		    		ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
					File theFile = new File(appInfo.dataDir + "/files/" + theFileName);
					//test intent
		    		Intent testIntent = new Intent(Intent.ACTION_VIEW);
					testIntent.setType("application/msword");
					//see if we have an intent for this type of doc...
					List list = pm.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
					if(list.size() > 0 && theFile.isFile()){
						Intent intent = new Intent();
					    intent.setAction(Intent.ACTION_VIEW);
					    Uri uri = Uri.fromFile(theFile);
					    intent.setDataAndType(uri, "application/msword");
	                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					    startActivity(intent);
					 }else{
						 showAlert(".DOC Not Supported", "Your device does not support reading Microsoft .DOC files");
					 }
		   		}catch (Exception je){
					 showAlert("Error Opening .DOC", "Your device does not support reading Microsoft .DOC files");
		        }			
			}else{
				showAlert("Error Saving .DOC?", "An error occured while trying to save the document? Please check your internet connection then try again.");
			}		
	}

		
	////////////////////////////////////////////////////////////////////
	//options menu
	@Override 
	public boolean onCreateOptionsMenu(Menu menu) { 
		super.onCreateOptionsMenu(menu); 
        return true;
	} 

	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
        return true;
	} 
	//end menu
	////////////////////////////////////////////////////////////////////
			
		
	
	
}












