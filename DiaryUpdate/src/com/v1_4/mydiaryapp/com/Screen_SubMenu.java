package com.v1_4.mydiaryapp.com;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Screen_SubMenu extends Act_ActivityBase {

	public DownloadThread downloadThread;
	public ListView myListView;
	public ArrayList<Obj_Screen> menuItems;
	public Adapter_List_Menu_Style_1 menuItemAdapter;
	int selectedIndex;
	
	//hidden / showing
	public static final int INVISIBLE = 4;
	public static final int VISIBLE = 0;	
		
	//onCreate
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_global_list);

        //activity name
        thisActivityName = "Screen_SubMenu";
       
        //remote command
        remoteDataCommand = "subMenuViewController";
        
        //appDelegate holds current screen
	    this.appDelegate = (AppDelegate) this.getApplication();
        appGuid = appDelegate.currentApp.guid;
		appApiKey = appDelegate.currentApp.apiKey;
    	screenGuid = appDelegate.currentScreen.screenGuid;
    	saveAsFileName = appDelegate.currentScreen.screenGuid + "_subMenu.txt";
    	
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
    	
        //title
        ((TextView)findViewById(R.id.myTitle)).setText(appDelegate.currentScreen.screenTitle);
         	       
        //list view
        myListView = (ListView)findViewById(R.id.myList);
        
        //array of items
        menuItems = new ArrayList<Obj_Screen>();
        
        //init selected index
        selectedIndex = -1;
        
        //setup data adapter for list view
        int resID = R.layout.list_menu_style_1;
        menuItemAdapter = new Adapter_List_Menu_Style_1(this, resID, menuItems);
        myListView.setAdapter(menuItemAdapter);
        
    }


	//onStart
	@Override 
	protected void onStart() {
		super.onStart();
			
		//local or remote JSON text...
		if(appDelegate.fileExists(saveAsFileName)){
			//Log.i("ZZ", thisActivityName + ":onStart local data exists");      
			JSONData = appDelegate.getLocalText(saveAsFileName);
			parseData(JSONData);
		}else{
			//parse called after when done
			//Log.i("ZZ", thisActivityName + ":onStart downloading data");      
			downloadData();			
		}
	}

	//onStop
	@Override 
	protected void onStop(){
		super.onStop();
		if(downloadThread != null){
			boolean retry = true;
			downloadThread.setThreadRunning(false);
			while(retry){
				try{
					downloadThread.join();
					retry = false;
				}catch (Exception je){
					Log.i("ZZ", thisActivityName + ":onStop ERROR: " + je.getMessage());      
				}
			}
		}
	}
	
	//onResume
	@Override
	protected void onResume(){
		super.onResume();
		if(selectedIndex > -1){
			if(myListView != null){
				if(myListView.getAdapter().getCount() >= selectedIndex){
					myListView.setSelection(selectedIndex);
				}
			}	
		}
	}
		
	//download data
	public void downloadData(){
		//show progress
		showProgress("Loading...", "We'll save this to speed things up for next time.");

		//build URL
		String tmpURL = appDelegate.currentApp.dataUrl;
		tmpURL += "?appGuid=" + appGuid;
		tmpURL += "&appApiKey=" + appApiKey;	
		tmpURL += "&screenGuid=" + screenGuid;
		tmpURL += "&command=" + remoteDataCommand;
		
			downloadThread = new DownloadThread();
			downloadThread.setDownloadURL(tmpURL);
			downloadThread.setSaveAsFileName(saveAsFileName);
			downloadThread.setDownloadType("text");
			downloadThread.setThreadRunning(true);
			downloadThread.start();
			Log.i("ZZ", thisActivityName + ":downloadData: " + tmpURL);      

	}
	
	//parse data..
	public void parseData(String theJSONString){
		
		//empty list view data adapter
		menuItemAdapter.clear();
		myListView.invalidate();
		
		
		//parse JSON string
		//Log.i("ZZ", thisActivityName + ":parseData: " + theJSONString);      
    	try{

    		//empty data if previously filled...
    		menuItems.clear();

    		//create json objects from data string...
    		JSONObject raw = new JSONObject(theJSONString);
            JSONObject results =  raw.getJSONObject("results");
            JSONArray screens =  results.getJSONArray("screens");
            
            //loop    
            int i = 0;
            for (i = 0; i < screens.length(); i++){
            	
            	JSONObject tmpJson = screens.getJSONObject(i);
    	        Obj_Screen tmpItem = new Obj_Screen(tmpJson.getString("appGuid"), tmpJson.getString("screenGuid"));
    	        tmpItem.setMenuText(tmpJson.getString("menuText"));
    	        tmpItem.setScreenType(tmpJson.getString("screenType"));
    	        tmpItem.setMenuIcon(tmpJson.getString("menuIcon"));
    	        

	    	        //if we have an icon...
	    	        if(tmpJson.getString("menuIcon").length() > 1){
		    	        int pos = appDelegate.root_iconsFileNames.indexOf(tmpJson.getString("menuIcon"));
		    	        if(pos > -1){
		    	        	//Log.i("ZZ", thisActivityName + ": setting icon"); 
		    	        	tmpItem.setImgMenuIcon(appDelegate.root_iconsBitmaps.get(pos));
		    	        }
	    	        }     	        
	    	        
	    	        //some screens have the title in the jsonScreenOptions..
	    	        try{
	        	        tmpItem.setScreenTitle(tmpJson.getString("title"));
	    	        }catch (Exception je){
	    	        	//continue..
	    	        }
	    	        
    	        	//create JSON from all JSON variables for this screen
    	        	String tmpKeys = "{";
                	JSONArray keyNames =  tmpJson.names();
                    for (int n = 0; n < keyNames.length(); n++){
                    	if(keyNames.getString(n).equals("title")){
                    		tmpItem.setScreenTitle(tmpJson.getString(keyNames.getString(n)));
                    	}
                    	tmpKeys += "\"" + keyNames.getString(n) + "\":\"" + tmpJson.getString(keyNames.getString(n)) + "\",";
                    }
                    tmpKeys += "\"end\":\"end\"}";
                    
    	        tmpItem.setJsonScreenOptions(tmpKeys);
    	        menuItems.add(i, tmpItem);
            
            }//end for
            
         
            //setup data adapter for list view
            int resID = R.layout.list_menu_style_1;
            menuItemAdapter = new Adapter_List_Menu_Style_1(this, resID, menuItems);
            myListView.setAdapter(menuItemAdapter);
            
	        //setup list click listener
	    	final OnItemClickListener listItemClickHandler = new OnItemClickListener() {
	            public void onItemClick(AdapterView parent, View v, int position, long id){
	            	//clicked item
	            	Obj_Screen tmpMenuItem = (Obj_Screen) menuItems.get(position);
	               	if(tmpMenuItem.getScreenGuid().toString() != ""){
	               		
	               		//remember selected index to select this row on resume...
	               		selectedIndex = position;
	               		
	               		//fire menuClick (method in Act_ActivityBase)
	               		menuTap(tmpMenuItem);
	               	
	               	}
	           	}
	        };    
	        myListView.setOnItemClickListener(listItemClickHandler);             
     
            
    	}catch (Exception je){
    		showAlert("Data Format Error", "There was a problem reading data associated with this app.");
    	}
		
		//hide progress
		hideProgress();   
		
	}

	/////////////////////////////////////////////////////
	//done downloading data..

	Handler downloadTextHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			if(JSONData.length() < 1){
				hideProgress();
				showAlert("Error Downloading", "Please check your internet connection then try again.");
			}else{
				parseData(JSONData);
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
				downloadTextHandler.sendMessage(downloadTextHandler.obtainMessage());
				this.setThreadRunning(false);
			}
			
			//image downloads
			if(downloadType == "image"){
				JSONData = appDelegate.downloadText(downloadURL);
				appDelegate.saveText(saveAsFileName, JSONData);
				downloadTextHandler.sendMessage(downloadTextHandler.obtainMessage());
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
                downloadData();
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







