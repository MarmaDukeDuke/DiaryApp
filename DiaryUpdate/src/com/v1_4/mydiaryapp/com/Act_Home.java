package com.v1_4.mydiaryapp.com;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Act_Home extends Act_ActivityBase{
	
	//download stuff..
	public DownloadThread downloadThread;
	public String thisActivityName = "Act_Home";
	public String localFileName;
	public String localImageName;
	public int selectedIndex;
	public int checkForUpdates;
	
	//appVars, components
	Bitmap appImage;
	ImageView appImageView;
	ArrayList<Obj_Screen> menuItems;
	Adapter_List_Menu_Style_1 menuItemAdapter;
	ListView myListView;
	
	//hidden / showing
	public static final int INVISIBLE = 4;
	public static final int VISIBLE = 0;
	
    //onCreate
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	appDelegate = (AppDelegate) this.getApplication();
    	appGuid = appDelegate.currentApp.guid;
		appApiKey = appDelegate.currentApp.apiKey;
        setContentView(R.layout.act_home);
    	
        //init save file names, array's
        localFileName = appGuid + "_appData.txt";
        saveAsFileName = appGuid + "_appData.txt";
        menuItems = new ArrayList<Obj_Screen>();        

        //components
        appImageView = (ImageView) findViewById(R.id.appImageView);
        myListView = (ListView)findViewById(R.id.myListView);
        
        //init integers
        selectedIndex = -1;
        checkForUpdates = 1;
        
        //setup data adapter for list view
        int resID = R.layout.list_menu_style_1;
        menuItemAdapter = new Adapter_List_Menu_Style_1(this, resID, menuItems);
        myListView.setAdapter(menuItemAdapter);
        
        //back button..
        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setVisibility(INVISIBLE);
        
        //info button
        ImageButton btnInfo = (ImageButton) findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	showInfo();
           }
        }); 
        
        //default image
		Bitmap noImage = BitmapFactory.decodeResource(getResources(),R.drawable.app_default);				
		noImage = appDelegate.roundImage(noImage, 10);
		appImageView.setImageBitmap(noImage);
		appDelegate.currentApp.setImage(noImage);

		//click listener for image..
        appImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	fadeAppImage();
            	showInfo();
           }
        });
  }//end onCreate
	
	
	///////////////////////////////////////////////////
	//activity life-cycle overrides
	
	//onStart
	@Override 
	protected void onStart(){
		super.onStart();
		//Log.i("ZZ", thisActivityName + ":onStart");
		getLastLocation();
	}
	
    //onResume
    @Override
    public void onResume(){
    	super.onResume();
		appDelegate.foundUpdatedLocation = 0;
		getLastLocation();
		Log.i("ZZ", thisActivityName + ":saveAsFileName: " + saveAsFileName);
		
		//local or remote data?
		if(appDelegate.fileExists(saveAsFileName)){
			initGUI();
			if(checkForUpdates == 1){
				checkForUpdates();
			}
		}else{
			showProgress("Downloading...","This should only take a moment");
			downloadAppData();
		}
    }
    
    //onPause
    @Override
    public void onPause() {
	    //Log.i("ZZ", thisActivityName + ":onPause");
        super.onPause();
        stopListening();
    }
    
    //onStop
	@Override 
	protected void onStop(){
		super.onStop();
	    //Log.i("ZZ", thisActivityName + ":onStop");
        stopListening();
	}	
	
	//onDestroy
    @Override
    public void onDestroy() {
	    //Log.i("ZZ", thisActivityName + ":onDestroy");
        super.onDestroy();
        stopListening();
    }
    
	//activity life-cycle overrides
    ///////////////////////////////////////////////////

	//init GUI (data must be saved locally)
	public void initGUI(){

			//empty list view data adapter
			menuItemAdapter.clear();
			myListView.invalidate();
			
			//read local data (download and save first if we don't already have it)
			if(localFileName.length() > 2){
       
				//parse app data if it's local
				String theJSONString = appDelegate.getLocalText(localFileName);
				//Log.i("ZZ", thisActivityName + ":initGUI:localData: " + theJSONString); 

				if(theJSONString.length() > 2){
					
					try{
						
						JSONObject objRoot = new JSONObject(theJSONString);
						JSONObject appVars = objRoot.getJSONObject("appData");
						
						//set title..
				        ((TextView)findViewById(R.id.myTitle)).setText(appVars.getString("name"));
				        
				        //set properties for current app 
				        appDelegate.currentApp.setName(appVars.getString("name"));
				        appDelegate.currentApp.setVersion(appVars.getString("version"));
				        appDelegate.currentApp.setImageName(appVars.getString("imageName"));
				        appDelegate.currentApp.setImageUrl(appVars.getString("imageUrl"));
				        appDelegate.currentApp.setModifiedUTC(appVars.getString("modifiedUTC"));
				        
				        //load or download image..
						if(appDelegate.fileExists(appVars.getString("imageName"))){
							appImage = appDelegate.getLocalImage(appVars.getString("imageName"));
							if(appImage != null){
						        appDelegate.currentApp.setImage(appImage);
								appImage = appDelegate.roundImage(appImage, 10);
								appImageView.setImageBitmap(appImage);
								hideProgress();
							}else{
								showAlert("Image Load Error","There was a problem loading a saved image?");
							}
						}else{
							
							//Log.i("ZZ", thisActivityName + ":initGUI:imageDoesNotExist"); 
							
							//configure downloader for image
							downloadThread = new DownloadThread();
							downloadThread.setDownloadURL(appVars.getString("imageUrl"));
							downloadThread.setSaveAsFileName(appVars.getString("imageName"));
							downloadThread.setDownloadType("image");
							downloadThread.setThreadRunning(true);
							downloadThread.start();
						
						}				        
				                
						
				        //build screen's array
			            JSONArray screens =  appVars.getJSONArray("screens");
			            
			            //loop   
			            int i = 0;
			            for (i = 0; i < screens.length(); i++){
		            	
			            	JSONObject tmpJson = screens.getJSONObject(i);
			    	        Obj_Screen thisScreen = new Obj_Screen(tmpJson.getString("appGuid"), tmpJson.getString("guid"));
			    	        thisScreen.setMenuText(tmpJson.getString("menuText"));
			    	        thisScreen.setScreenType(tmpJson.getString("screenType"));
			    	        thisScreen.setMenuIcon(tmpJson.getString("menuIcon"));
			    	       
			    	        //if we have an icon...
			    	        if(tmpJson.getString("menuIcon").length() > 1){
				    	        int pos = appDelegate.root_iconsFileNames.indexOf(tmpJson.getString("menuIcon"));
				    	        if(pos > -1){
				    	        	//Log.i("ZZ", thisActivityName + ": setting icon"); 
			    	        		thisScreen.setImgMenuIcon(appDelegate.root_iconsBitmaps.get(pos));
				    	        }
			    	        }    	
			    	        
			    	        //no screens for sub-menu
			    	        String parentScreenGuid = "";
			    	        try{
			    	        	parentScreenGuid = tmpJson.getString("parentScreenGuid");
			    	        }catch (Exception je){
			    	        	//some screens don't have a parentScreenGuid
			    	        }
			    	        
			    	        //ignore sub-menu screens and screen_info screen
			    	        if(parentScreenGuid.length() > 1 || tmpJson.getString("screenType").equals("screen_info")){
			    	        	//ignore
			    	        }else{
			    	        
			    	        	//Log.i("ZZ", thisActivityName + ":screen: " + tmpJson.getString("screenType")); 
			    	        
				    	        //some screens have the title in the jsonScreenOptions..
				    	        try{
				    	        	thisScreen.setScreenTitle(tmpJson.getString("title"));
				    	        }catch (Exception je){
				    	        	//continue..
				    	        }
				    	        
				    	        try{
				    	        	//create JSON from all variables for this screen
				    	        	String tmpKeys = "{";
				    	        	JSONArray keyNames =  tmpJson.names();
				    	        	for (int n = 0; n < keyNames.length(); n++){
				    	        		if(keyNames.getString(n).equals("title")){
				    	        			thisScreen.setScreenTitle(tmpJson.getString(keyNames.getString(n)));
				    	        		}
				    	        		tmpKeys += "\"" + keyNames.getString(n) + "\":\"" + tmpJson.getString(keyNames.getString(n)) + "\",";
				    	        	}
				    	        	tmpKeys += "\"end\":\"end\"}";
				    	        	thisScreen.setJsonScreenOptions(tmpKeys);
				    	        }catch(Exception je){
				    	        	thisScreen.setJsonScreenOptions("");
				    	        }
				    	        
				    	        //add to screens array
				    	        menuItems.add(thisScreen);
				    	        
			    	        }
				            
				        }//end for each screen
				        
			            
			            //setup data adapter for list view
			            int resID = R.layout.list_menu_style_1;
			            menuItemAdapter = new Adapter_List_Menu_Style_1(this, resID, menuItems);
			            myListView.setAdapter(menuItemAdapter);
			      
				        //click listener for each menu item
				    	final OnItemClickListener listItemClickHandler = new OnItemClickListener() {
				            public void onItemClick(AdapterView parent, View v, int position, long id){
				            	//clicked item
				            	Obj_Screen tmpMenuItem = (Obj_Screen) menuItems.get(position);
				               		
				               	//remember selected index to select this row on resume...
				               	selectedIndex = position;
				               		
				               	//call menuClick (method in Act_ActivityBase)
				               	menuTap(tmpMenuItem);
				               	
				           	}
				        };    
				        myListView.setOnItemClickListener(listItemClickHandler);             
			     			
				        //show selected item from previous selection
				    	if(selectedIndex > -1){
				    		myListView.setSelection(selectedIndex); 
						} 
				        
			            //hide progress
	    				hideProgress();
	    				

					}catch (Exception je){
						hideProgress();
						showAlert("Data Format Error", "There was a problem reading data associated with this app.");
					}
							
				} //json data length 
			}//localFileName
			
	} 

	//slide appImage..
	public void slideAppImage(){
		 Animation animation = new TranslateAnimation(
		      Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
		      Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
		  );
		  animation.setDuration(500);
		  appImageView.startAnimation(animation);
		  
	}
	
	//rotate image..
	public void rotateAppImage(){
		Animation animation = new RotateAnimation(0, 360, appImageView.getWidth() / 2, appImageView.getHeight() / 2);
		animation.setRepeatMode(Animation.REVERSE);
		animation.setRepeatCount(0);
		animation.setDuration(1000);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());		  
		appImageView.startAnimation(animation);
	}
	
	//pulse app image
	public void fadeAppImage(){
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		appImageView.startAnimation(animation); 
	}
	
	//refresh
	public void refresh(){
	    
		//set default image
		Bitmap noImage = BitmapFactory.decodeResource(getResources(),R.drawable.app_default);				
		noImage = appDelegate.roundImage(noImage, 10);
		appImageView.setImageBitmap(noImage);
        appDelegate.currentApp.setImage(noImage);
		
		//delete saved data
		appDelegate.deleteLocalData();
		
		//empty menu
		menuItemAdapter.clear();
		myListView.invalidate();
		
		//clear title
        ((TextView)findViewById(R.id.myTitle)).setText("");
		
		//re-build gui (it will download data again)
		showProgress("Downloading...","This should only take a moment");
		downloadAppData();
        
	}
	
	
	//download app data..
	public void downloadAppData(){
    	//Log.i("ZZ", thisActivityName + ":downloadAppData"); 
		if(appGuid.length() > 1){ 

			remoteDataCommand = "getAppJSON";
			String tmpURL = appDelegate.currentApp.dataUrl;
			tmpURL += "?command=" + remoteDataCommand;	
			tmpURL += "&appGuid=" + appGuid;	
			tmpURL += "&appApiKey=" + appApiKey;	
			tmpURL += "&deviceId=" + appDelegate.currentDevice.deviceId;	
			tmpURL += "&deviceLatitude=" + appDelegate.currentDevice.deviceLatitude;	
			tmpURL += "&deviceLongitude=" + appDelegate.currentDevice.deviceLongitude;	
			tmpURL += "&deviceModel=" + appDelegate.currentDevice.deviceModel;	
			
			//configure downloader...
			downloadThread = new DownloadThread();
			downloadThread.setDownloadURL(tmpURL);
			downloadThread.setSaveAsFileName(localFileName);
			downloadThread.setDownloadType("text");
			downloadThread.setThreadRunning(true);
			downloadThread.start();
	    	Log.i("ZZ", thisActivityName + ":downloadAppData: " + tmpURL); 

		}
	}
	
	//check for updates..
	public void checkForUpdates(){
		if(appGuid.length() > 1){
			if(checkForUpdates == 1){
				
				remoteDataCommand = "getAppLastModified";
				String tmpURL = appDelegate.currentApp.dataUrl;
				tmpURL += "?command=" + remoteDataCommand;	
				tmpURL += "&appGuid=" + appGuid;	
				tmpURL += "&appApiKey=" + appApiKey;	
				tmpURL += "&deviceId=" + appDelegate.currentDevice.deviceId;	
				tmpURL += "&deviceLatitude=" + appDelegate.currentDevice.deviceLatitude;	
				tmpURL += "&deviceLongitude=" + appDelegate.currentDevice.deviceLongitude;	
				tmpURL += "&deviceModel=" + appDelegate.currentDevice.deviceModel;	
			
		    	Log.i("ZZ", thisActivityName + ":checkForUpdates: " + tmpURL); 
				
				//configure downloader...
				downloadThread = new DownloadThread();
				downloadThread.setDownloadURL(tmpURL);
				downloadThread.setSaveAsFileName("");
				downloadThread.setDownloadType("checkforupdates");
				downloadThread.setThreadRunning(true);
				downloadThread.start();
		
			}//checkForUpdates
		}
	}
	
	//handles text downloads..
	Handler downloadAppDataHandler = new Handler(){
		@Override 
		public void handleMessage(Message msg){
			initGUI();
		}
	};		
	
	//handles image downloads..
	Handler downloadImageHandler = new Handler(){
		@Override 
		public void handleMessage(Message msg){
			if(appImage != null){
		        appDelegate.currentApp.setImage(appImage);
				appImage = appDelegate.roundImage(appImage, 10);
				appImageView.setImageBitmap(appImage);
				slideAppImage();
			}else{
				showAlert("Download Error","There was a problem downloading an image. Please check your internet connection then try again.");
			}
		}
	};
	
	//handles checking for updates
	Handler downloadUpdateHandler = new Handler(){
		@Override 
		public void handleMessage(Message msg){
			Log.i("ZZ", thisActivityName + ":downloadUpdateHandler " + JSONData);
			String tmpModifiedUTC = appDelegate.currentApp.modifiedUTC;
			if(tmpModifiedUTC != null && JSONData != null){
				if(tmpModifiedUTC.length() > 5){
					if(JSONData.length() > 5){
						if(!tmpModifiedUTC.equals(JSONData)){
							
							//remove local data, reload
							appDelegate.deleteLocalData();
							
							//re-build gui (it will download data again)
							showProgress("Downloading...","Updating application data, this may take a moment.");
							downloadAppData();							
						}
					}
				}
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
			
    		 //text downloads
			if(downloadType == "text"){
				JSONData = appDelegate.downloadText(downloadURL);
				appDelegate.saveText(saveAsFileName, JSONData);
				downloadAppDataHandler.sendMessage(downloadAppDataHandler.obtainMessage());
				this.setThreadRunning(false);
			}
			
			//image downloads
			if(downloadType == "image"){
				appImage = appDelegate.downloadImage(downloadURL);
				appDelegate.saveBitmap(saveAsFileName, appImage);
				downloadImageHandler.sendMessage(downloadImageHandler.obtainMessage());
				this.setThreadRunning(false);
			}	
			
			//check for updates download
			if(downloadType == "checkforupdates"){
				JSONData = appDelegate.downloadText(downloadURL);
				downloadUpdateHandler.sendMessage(downloadUpdateHandler.obtainMessage());
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
        dialog.setContentView(R.layout.menu_home);
        dialog.setTitle("Screen Options");
        
        //show info..
        Button btnInfo = (Button) dialog.findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
            	showInfo();
           }
        });
        
         
        //close app..
        Button btnRefresh = (Button) dialog.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
                refresh();
            }
        });        
        
        //close app..
        Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
            	System.exit(0);
                //finish();
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













