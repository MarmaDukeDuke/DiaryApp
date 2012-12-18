package com.v1_4.mydiaryapp.com;
import org.json.JSONObject;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Screen_SingleImage extends Act_ActivityBase {
	
	public DownloadThread downloadThread;
	public Bitmap myImage;
	public ImageView myImageView;
	public String imageUrl;
	public String localImageName;

	
	//onCreate
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_singleimage);

    	//activity name
    	thisActivityName = "Screen_SingleImage";
    	        
        //appDelegate holds current screen
	    appDelegate = (AppDelegate) this.getApplication();
	    myImageView = (ImageView)findViewById(R.id.myImageView);
 		
 		//for local data..
 		appGuid = appDelegate.currentApp.guid;
		appApiKey = appDelegate.currentApp.apiKey;

        //back button..
        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
           }
        });
        
        //info button..
        ImageButton btnInfo = (ImageButton) findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	showInfo();
           }
        });
      	
  		
        //appDelegate.currentScreen.jsonScreenOptions holds variables 
		try{
			JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);

			//screen data
	        ((TextView)findViewById(R.id.myTitle)).setText(raw.getString("title"));
	        imageUrl = raw.getString("imageSingleUrl");
	        localImageName = raw.getString("imageName");
			
		}catch (Exception je){
    		Log.i("ZZ", thisActivityName + ":onCreate error : " + appDelegate.currentScreen.jsonScreenOptions);   
    	}
	
	}
	
	///////////////////////////////////////////////////
	//activity life-cycle overrides
	
	
    //onResume
    @Override
    public void onResume() {
    	super.onResume();
		//Log.i("ZZ", thisActivityName + ":onResume");
		if(appDelegate.fileExists(localImageName)){
			myImageView.setImageBitmap(appDelegate.getLocalImage(localImageName));
		}else{
			downloadImage();			
		}		
    	
    }
    
    //onPause
    @Override
    public void onPause() {
	    //Log.i("ZZ", thisActivityName + ":onPause");
        super.onPause();
    }
    
    //onStop
	@Override 
	protected void onStop(){
		super.onStop();
	    //Log.i("ZZ", thisActivityName + ":onStop");
	}	
	
	//onDestroy
    @Override
    public void onDestroy() {
	    //Log.i("ZZ", thisActivityName + ":onDestroy");
        super.onDestroy();
    }
    
	//activity life-cycle overrides
    ///////////////////////////////////////////////////
	
	
	//download data
	public void downloadImage(){

		//show progress
		showProgress("Loading...", "We'll save this to speed things up for next time.");

		//configure downloader for image
		downloadThread = new DownloadThread();
		downloadThread.setDownloadURL(imageUrl);
		downloadThread.setSaveAsFileName(localImageName);
		downloadThread.setDownloadType("image");
		downloadThread.setThreadRunning(true);
		downloadThread.start();		

	}	
	
	
	//handles image downloads..
	Handler downloadImageHandler = new Handler(){
		@Override 
		public void handleMessage(Message msg){
			hideProgress();
			if(myImage != null){
				myImageView.setImageBitmap(myImage);
			}else{
				showAlert("Download Error","There was a problem downloading an image. Please check your internet connection then try again.");
			}
		}
	};
	
	
	
	//downloads in background thread..
	public class DownloadThread extends Thread{
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
			
 			//image downloads
 			if(downloadType == "image"){
 				myImage = appDelegate.downloadImage(downloadURL);
 				appDelegate.saveBitmap(saveAsFileName, myImage);
 				downloadImageHandler.sendMessage(downloadImageHandler.obtainMessage());
 				this.setThreadRunning(false);
 			}	
			
    	 }
	}	
	//end download thread		

	
	/////////////////////////////////////////////////////
	//options menu
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
		
		 //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.menu_refresh);
        dialog.setTitle("Screen Options");
        
        //refresh ..
        Button btnRefresh = (Button) dialog.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
                downloadImage();
            }
        });

        //cancel...
        Button button = (Button) dialog.findViewById(R.id.btnCancel);
        button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
               dialog.cancel();
            }
        });        
        
        //show
        dialog.show();
		return true;
		
	} 
	//end menu
	/////////////////////////////////////////////////////	
		
	
	
}







