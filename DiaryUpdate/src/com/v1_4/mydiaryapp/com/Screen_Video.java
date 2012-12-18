package com.v1_4.mydiaryapp.com;
import org.json.JSONObject;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

public class Screen_Video extends Act_ActivityBase{

	public String thisActivityName = "Screen_Video";
	
	//movie controller
	String videoUrl;
	VideoView videoView;
	MediaController mediaController;
	public int isLoading;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_video);

	    //appDelegate holds current screen
	    this.appDelegate = (AppDelegate) this.getApplication();
    	
    	
        //appDelegate.currentScreen.jsonScreenOptions holds variables 
		try{
			JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);

			//url from currently selected Obj_Screens jsonScreenOptions
			videoUrl = raw.getString("videoUrl");
			
		}catch (Exception je){
    		Log.i("ZZ", thisActivityName + ":onCreate error : " + appDelegate.currentScreen.jsonScreenOptions);   
    	}
		//Log.i("ZZ", thisActivityName + ":videoUrl : " + videoUrl);   
		
		videoView = (VideoView) findViewById(R.id.videoView);
		mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		isLoading = 0;

	}	
	

	
	
	///////////////////////////////////////////////////
	//activity life-cycle overrides
	
	//onStart
	@Override 
	protected void onStart(){
		super.onStart();
		//Log.i("ZZ", thisActivityName + ":onStart");
	}
	
    //onResume
    @Override
    public void onResume() {
    	super.onResume();
    	hideProgress();
    	if(isLoading == 1){
    		if(videoView != null){
    			videoView = null;
    		}
    	}
		startMovie();
		//Log.i("ZZ", thisActivityName + ":onResume");
    }
    
    //onPause
    @Override
    public void onPause() {
	    //Log.i("ZZ", thisActivityName + ":onPause");
    	super.onPause();
    	hideProgress();
   		if(videoView != null){
   			videoView = null;
    	}
    }
    
    //onStop
	@Override 
	protected void onStop(){
		super.onStop();
		hideProgress();
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


    //starts movie
    public void startMovie(){
    	
  		//show progress for a few seconds..
		showProgress("Starting video stream...", "Please be patient. If you're on 3G this could take a bit.\nWi-Fi is much better for streaming video.");
		isLoading = 1;
    	
    	try{
    		//set video link (mp4 format )
    		mediaController = new MediaController(this);
    		Uri video = Uri.parse(videoUrl);
    		videoView.setMediaController(mediaController);
    		videoView.setVideoURI(video);
            videoView.setOnPreparedListener(mOnPreparedListener);
    		videoView.start();
    		
    	}catch(Exception je){
    		hideProgress();
    		showAlert("Invalid Video URL","The URL to the video could not be determined. This screen will close.");
    		finish();
    	}

    }
    
    //onPrepared...
	private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener(){
        public void onPrepared(MediaPlayer mp){
    	    //Log.i("ZZ", thisActivityName + ":OnPreparedListener");
        	isLoading = 0;
        	hideProgress();
        }
    };
    
	/////////////////////////////////////////////////////
	//options menu
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
		
		 //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.menu_refresh);
        dialog.setTitle("Screen Options");
        
       //close app..
        Button btnRefresh = (Button) dialog.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
                startMovie();
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











