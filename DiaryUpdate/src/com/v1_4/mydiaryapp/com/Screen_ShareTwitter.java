package com.v1_4.mydiaryapp.com;
import org.json.JSONObject;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Screen_ShareTwitter extends Act_ActivityBase{

    Twitter twitter;
    EditText twitterId;
    EditText twitterPass;
    EditText twitterMessage;
    Button btnSubmit;
    

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_sharetwitter);

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
        ((TextView)findViewById(R.id.myTitle)).setText("Share on Twitter");
        
        //text / buttons
        twitterId = (EditText) findViewById(R.id.twitterId);
        twitterPass = (EditText) findViewById(R.id.twitterPass);
        twitterMessage = (EditText) findViewById(R.id.twitterMessage);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        //submit button click
	    btnSubmit = (Button)findViewById(R.id.btnSubmit);
	    btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                postToTwitter();
            }
        });	        
        
         	   
		//appDelegate.currentScreen.jsonScreenOptions holds variables for RSS feed
		try{
			JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);
    		Log.i("ZZ", thisActivityName + ": " + appDelegate.currentScreen.jsonScreenOptions);   
			twitterMessage.setText(raw.getString("twitterMessage"));
			
			//if we have a remember twitterName...
			

		}catch (Exception je){
			hideProgress();
    		Log.i("ZZ", thisActivityName + ":downloadData appDelegate.currentScreen.jsonScreenOptions is not well formed");   
    	}
	       
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

    public void postToTwitter(){
    	
    	InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(twitterId.getWindowToken(), 0);	
    	
    	 String id = twitterId.getText().toString();
    	 String pass = twitterPass.getText().toString();
    	 String message = twitterMessage.getText().toString();
    	 String error = "";
    	 
    	 if(twitterId.getText().length() < 1){
    		 error += "\nTwitter Id required.";
    	 }
    	 if(twitterPass.getText().length() < 1){
    		 error += "\nTwitter Password required.";
    	 }
    	 if(twitterMessage.getText().length() > 140 || twitterMessage.getText().length() < 1){
    		 error += "\nTwitter Message too short or too long (max 140 chars)";
    	 }
    	 if(!error.equals("")){
    		 showAlert("Please correct...", error);
    	 }else{
    		 
    		 //send tweet..
		     showProgress("Sending...","Waiting for response from Twitter's servers...");
    		 twitter = new Twitter(id, pass);
    		 try{
    			 twitter.setStatus(message);
    		     twitterMessage.setText("");
    		     hideProgress();
    		     showAlert("Success!", "Your tweet was posted to Twitter successfully!");
    		 }catch(TwitterException.E401 e){
    			 hideProgress();
    			 showAlert("Rats!", "Your Twitter username or password (or both) could not be validated. Please try again.");
	         }catch(Exception e){
    			 hideProgress();
    			 showAlert("Internet Error", "Twitter does not seem to be responding. Please check your internet connection then try again.");
    		 }
    		 
    	 }
    	
   
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








