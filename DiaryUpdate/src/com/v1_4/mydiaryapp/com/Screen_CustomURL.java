package com.v1_4.mydiaryapp.com;
import java.util.List;

import org.json.JSONObject;
import android.content.res.Configuration;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Screen_CustomURL extends Act_ActivityBase {

	public WebView browser;
	public String loadUrl;

	//onCreate
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_customurl);

        //activity name
    	thisActivityName = "Screen_CustomURL";

        //appDelegate holds current screen
	    this.appDelegate = (AppDelegate) this.getApplication();
 		browser = (WebView)findViewById(R.id.myWebView);
		
        //back button..
        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
           }
        });
        //info button
        ImageButton btnInfo = (ImageButton) findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	showInfo();
           }
        }); 		
 		
		//title from currently selected Obj_Screens jsonScreenOptions
        ((TextView)findViewById(R.id.myTitle)).setText(appDelegate.currentScreen.screenTitle);
		
        //appDelegate.currentScreen.jsonScreenOptions holds variables 
		try{
			JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);

			//url from currently selected Obj_Screens jsonScreenOptions
    		showProgress("Loading..","please wait...");
			loadUrl = raw.getString("url");
			
		}catch (Exception je){
    		Log.i("ZZ", thisActivityName + ":onCreate error : " + appDelegate.currentScreen.jsonScreenOptions);   
    	}
		
		if(browser != null && loadUrl != null){
			
			//webView settings
			browser.setBackgroundColor(0);
			browser.setInitialScale(75);
			browser.getSettings().setJavaScriptEnabled(true);
			browser.getSettings().setSupportZoom(true);
			browser.getSettings().setBuiltInZoomControls(true);
			browser.getSettings().setPluginsEnabled(true);
		    browser.setWebViewClient(new WebViewClient() {
		    	
 	
		    	@Override
	        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    		
		    		Log.i("ZZ", thisActivityName + ":shouldOverrideUrlLoading: " + url);   
					
		    		//check for key strings in URL to determine next action.
		    		int useNativeIntent = 0;
		    		int bolDone = 0;
		    		
		    		//youtube video
		    		if(url.contains("youtube.com")){
		    			useNativeIntent = 1;
		    		}
					
		    		//anything with an .mp3 extension
		    		if(url.contains(".mp3")){
		    			useNativeIntent = 1;
		    		}	
					
		    		//anything with an .zip extension
		    		if(url.contains(".zip")){
		    			useNativeIntent = 1;
		    		}			    		
					
		    		//allow loading of this URL
		    		if(useNativeIntent == 0){
			    		//showProgress("Loading..","This may take a moment if you have a slow internet connection.");
		    			loadBrowser(url);
		    			return true;
		    		}
		    		
		    		//let phone detemrine intent to use
		    		if(bolDone == 0){
		    			//let phone determine which intent to launch...
		    			Intent theIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		    			startActivity(theIntent);
		    			return false;
		    		}
		    		return true;
	            }
	            
		    	@Override
	            public void onPageFinished(WebView view, String url) {
	            	hideProgress();
	            }
	            
		    	@Override
	            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					hideProgress();
	            	//showAlert("Error Loading?","There was a problem loading some data. Please check your internet connection then try again.");
	            }
	            
	         });
	         loadBrowser(loadUrl);
		}
		
        
    }
	
	//loadBrowser
	public void loadBrowser(String theUrl){
		if(browser != null){
			try{
				browser.loadUrl(theUrl);
			}catch(Exception je){
				hideProgress();
				showAlert("Error Loading?","There was a problem loading some data. Please check your internet connection then try again.");
			}				
		}
	}
	
	//refresh
	public void refreshBrowser(){
		showProgress("Loading...", "This should only take a moment...");
		browser.reload();
	}
	
	//on configuration changes
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
		Log.i("ZZ", thisActivityName + ":onConfigurationChanged called");   
	}		


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
                refreshBrowser();
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














