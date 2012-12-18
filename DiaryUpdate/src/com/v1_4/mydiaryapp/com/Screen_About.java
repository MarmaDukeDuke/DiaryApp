package com.v1_4.mydiaryapp.com;
import java.net.URLDecoder;
import java.net.URLEncoder;
import android.content.res.Configuration;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Screen_About extends Act_ActivityBase {
	
	public DownloadThread downloadThread;
	public WebView webView;
	public String loadUrl;
	
	//onCreate
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_about);

    	//activity name
    	thisActivityName = "Screen_About";
    	        
        //appDelegate holds current screen
	    appDelegate = (AppDelegate) this.getApplication();
 		webView = (WebView)findViewById(R.id.myWebView);
 		
 		//for local data..
 		appGuid = appDelegate.currentApp.guid;
		appApiKey = appDelegate.currentApp.apiKey;
    	saveAsFileName = appGuid + "_about.html";
    	remoteDataCommand = "aboutViewController";
    	
        //back button..
        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
           }
        });
        
        //info button
        ImageButton btnInfo = (ImageButton) findViewById(R.id.btnInfo);
        btnInfo.setVisibility(INVISIBLE);
      	
    	//title
        ((TextView)findViewById(R.id.myTitle)).setText("About This App");
			
		if(webView != null){
			
			//webView settings
			webView.setBackgroundColor(0);
			webView.setInitialScale(0);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setSupportZoom(true);
			webView.getSettings().setBuiltInZoomControls(true);
			webView.getSettings().setJavaScriptEnabled(true);
	        webView.setWebChromeClient(new WebChromeClient() {
	            public void onProgressChanged(WebView view, int progress){
	                
	            }
	        });
	 
	        webView.setWebViewClient(new WebViewClient(){
	            @Override
	            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
	            	hideProgress();
	            }
	 
	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url){
	                view.loadUrl(url);
	                return true;
	            }
	        });
			
			
		    
		}
		
        
    }
	
	///////////////////////////////////////////////////
	//activity life-cycle overrides
	
	
	//on configuration changes
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
		Log.i("ZZ", thisActivityName + ":onConfigurationChanged called");   
	}		
	
    //onResume
    @Override
    public void onResume() {
    	super.onResume();
		//Log.i("ZZ", thisActivityName + ":onResume");
		//local or remote JSON text...
		if(appDelegate.fileExists(saveAsFileName)){
			String theText = appDelegate.getLocalText(saveAsFileName);
			loadDataString(theText);
		}else{
			downloadData();			
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
	
	//loadURL
	public void loadURL(String theUrl){
		if(webView != null){
			try{
				showProgress("Loading..","This screen will load faster next time.");
				webView.loadUrl(theUrl);
			}catch(Exception je){
				hideProgress();
				showAlert("Error Loading?","There was a problem loading some data. Please check your internet connection then try again.");
			}				
		}
	}


	//load string in web-view
	public void loadDataString(String theString){
		//Log.i("ZZ", thisActivityName + ":loadDataString: " + theString); 
		webView.loadDataWithBaseURL(null, theString, "text/html", "utf-8", "about:blank");
		hideProgress();
	}

	//after download..
	Handler downloadTextHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			if(JSONData.length() < 1){
				hideProgress();
				showAlert("Error Downloading", "Please check your internet connection then try again.");
			}else{
				loadDataString(JSONData);
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







