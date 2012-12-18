package com.v1_4.mydiaryapp.com;


import org.json.JSONObject;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;


public class Screen_ShareFacebook extends Act_ActivityBase implements DialogListener, OnClickListener{

	
    private Facebook facebookClient;
    String postMessage = "";
    ImageButton btnFacebook;
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_sharefacebook);

        //appDelegate holds current screen
	    this.appDelegate = (AppDelegate) this.getApplication();
        appGuid = appDelegate.currentApp.guid;
		appApiKey = appDelegate.currentApp.apiKey;
    	screenGuid = appDelegate.currentScreen.screenGuid;
    	
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
        ((TextView)findViewById(R.id.myTitle)).setText("Facebook Connect");
       
        //button
        btnFacebook = (ImageButton) findViewById(R.id.btnFacebook);
        btnFacebook.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	startFacebook();
           }
        });        
        
        
        //get post message
    	try{
			JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);
    		Log.i("ZZ", thisActivityName + ": " + appDelegate.currentScreen.jsonScreenOptions);   
    		postMessage = raw.getString("facebookMessage");
			
		}catch (Exception je){
			hideProgress();
    		Log.i("ZZ", thisActivityName + ":downloadData appDelegate.currentScreen.jsonScreenOptions is not well formed");   
    	}
        
		
		//start facebook when screen loads..
		startFacebook();
    
    }
	
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
    public void onResume() {
    	super.onResume();
    	//Log.i("ZZ", thisActivityName + ":onResume");
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

    public void onComplete(Bundle values)
    {

        if (values.isEmpty()){
            //"skip" clicked ?
            return;
        }

        // if facebookClient.authorize(...) was successful, this runs
        // this also runs after successful post
        // after posting, "post_id" is added to the values bundle
        // I use that to differentiate between a call from
        // faceBook.authorize(...) and a call from a successful post
        // is there a better way of doing this?
        
        if (!values.containsKey("post_id")){
        	
            try{
            	
                Bundle parameters = new Bundle();
                parameters.putString("message", postMessage);// the message to post to the wall
                facebookClient.dialog(this, "stream.publish", parameters, this);// "stream.publish" is an API call
            
            }catch (Exception e){
            	showAlert("Facebook Error","There was a problem communicating with the Facebook servers?");
            	System.out.println(e.getMessage());
            }

        }
    }
    
    //start facebook...
    public void startFacebook(){
    	
		//facebook screen loads automatically. User uses this screen to login and post message
        facebookClient = new Facebook();
        // replace APP_API_ID with your own
        facebookClient.authorize(this, this.getString(R.string.facebookAPIKey), new String[] {"publish_stream"}, this);

    }

    public void onError(DialogError e){
    	showAlert("Facebook Error?","There was a problem communicating with Facebook");
	    //Log.i("ZZ", thisActivityName + "onError: " + e.getMessage());
    }

    public void onFacebookError(FacebookError e){
    	showAlert("Facebook Error?","Facebook rejected your login information, please try again.");
    	//Log.i("ZZ", thisActivityName + "onFacebookError: " + e.getMessage());
    }

    public void onCancel(){
    	//Log.i("ZZ", thisActivityName + "onCancel");
    }

    public void onClick(View v){
        //listener is required by implementation...unused
    }    
    
	/////////////////////////////////////////////////////
	//options menu
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
		
		 //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.menu_blank);
        dialog.setTitle("This screen has no options");
        
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








