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

public class Screen_QuizScores extends Act_ActivityBase {

	public DownloadThread downloadThread;
	public ListView myListView;
	public ArrayList<Obj_QuizScore> menuItems;
	public Adapter_QuizScore myListAdapter;
	public int selectedIndex;

	//onCreate
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_global_list);

        //activity name
        thisActivityName = "Screen_QuizScores";
        
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
        
        //appDelegate holds current screen
	    this.appDelegate = (AppDelegate) this.getApplication();
        appGuid = appDelegate.currentApp.guid;
		appApiKey = appDelegate.currentApp.apiKey;
    	screenGuid = appDelegate.currentScreen.screenGuid;
    	saveAsFileName = appDelegate.currentScreen.screenGuid + "_quizScores.txt";
    	
    	//data..
    	remoteDataCommand = "showQuizScoreboard";
    	JSONData = "";   	
    	
        //title
        ((TextView)findViewById(R.id.myTitle)).setText("Recent Scores");
        
        //init selected index
        selectedIndex = -1;       
        
        //list view
        myListView = (ListView)findViewById(R.id.myList);
        
         //array of items
        menuItems = new ArrayList<Obj_QuizScore>();
        
        //setup data adapter
        int resID = R.layout.list_quizscore;
        myListAdapter = new Adapter_QuizScore(this, resID, menuItems);
        myListView.setAdapter(myListAdapter);
        
    }


	//onStart
	@Override 
	protected void onStart() {
		super.onStart();
			
		
		//NOT USING LOCAL DATA, always fetch remote scores..
		downloadData();
		
		/*
		
		//local or remote JSON text...
		if(appDelegate.fileExists(saveAsFileName)){
			Log.i("ZZ", thisActivityName + ":onStart local data exists");      
			JSONData = appDelegate.getLocalText(saveAsFileName);
			parseData(JSONData);
		}else{
			//parse called after when done
			Log.i("ZZ", thisActivityName + ":onStart downloading data");      
			downloadData();			
		}
		*/
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
		
	//download data
	public void downloadData(){
		//show progress
		showProgress("Loading...", "Gathering recent quiz results..");
		
		//appDelegate.currentScreen.jsonScreenOptions holds variables for RSS feed
		try{
			JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);
			
			//build URL
			String tmpURL = appDelegate.currentApp.dataUrl;
			tmpURL += "?appGuid=" + appDelegate.currentApp.guid;
			tmpURL += "&screenGuid=" + screenGuid;
			tmpURL += "&appApiKey=" + appApiKey;	
			tmpURL += "&command=" + remoteDataCommand;
			
			//add scores...
			tmpURL += "&totalPoints=" + raw.getString("totalPoints");
			tmpURL += "&totalSeconds=" + raw.getString("totalSeconds");
			tmpURL += "&numberQuestions=" + raw.getString("numberQuestions");
			tmpURL += "&numberRight=" + raw.getString("numberRight");
			tmpURL += "&numberWrong=" + raw.getString("numberWrong");
			
			Log.i("ZZ", thisActivityName + ":url: " + tmpURL);      
			
			downloadThread = new DownloadThread();
			downloadThread.setDownloadURL(tmpURL);
			downloadThread.setSaveAsFileName(saveAsFileName);
			downloadThread.setDownloadType("text");
			downloadThread.setThreadRunning(true);
			downloadThread.start();
			
		}catch (Exception je){
			hideProgress();
			showAlert("Download Error", "The data url is not well formed?");
    	}
		
			
	}
	
	//parse data..
	public void parseData(String theJSONString){
		//parse JSON string
		Log.i("ZZ", thisActivityName + ":parseData: " + theJSONString);      
    	
		//empty list view data adapter
		myListAdapter.clear();
		myListView.invalidate();
		
		try{

    		//empty data if previously filled...
    		menuItems.clear();

    		//create json objects from data string...
    		JSONObject raw = new JSONObject(theJSONString);
            JSONArray scores =  raw.getJSONArray("scores");
            
            //loop    
            int i = 0;
            for (i = 0; i < scores.length(); i++){
            	JSONObject tmpJson = scores.getJSONObject(i);
            	
            	//quiz data
    	        Obj_QuizScore tmpItem = new Obj_QuizScore();
    	        tmpItem.setScoreDate(tmpJson.getString("scoreDate"));
    	        tmpItem.setTotalPoints(tmpJson.getString("totalPoints"));
    	        tmpItem.setTotalSeconds(tmpJson.getString("totalSeconds"));
    	        tmpItem.setNumberQuestions(tmpJson.getString("numberQuestions"));
    	        tmpItem.setNumberRight(tmpJson.getString("numberRight"));
    	        tmpItem.setNumberWrong(tmpJson.getString("numberWrong"));
   	        
    	        menuItems.add(i, tmpItem);
            
            }//end for
             
            
            //setup data adapter
            int resID = R.layout.list_quizscore;
            myListAdapter = new Adapter_QuizScore(this, resID, menuItems);
            myListView.setAdapter(myListAdapter);
     	        
	        //setup list click listener
	    	final OnItemClickListener listItemClickHandler = new OnItemClickListener() {
	            public void onItemClick(AdapterView parent, View v, int position, long id){
	            	
	            	//clicked item
	            	Obj_QuizScore tmpScore = (Obj_QuizScore) menuItems.get(position);
	               	
	               		//tmp info...
	            		String tmp = tmpScore.getScoreDate();
	            		tmp += "\nPoints: " + tmpScore.getTotalPoints();
	            		tmp += "\nTime: " + tmpScore.getTotalSeconds();
	            		tmp += "\nCorrect: " + tmpScore.getNumberRight();
	            		tmp += "\nIncorrect: " + tmpScore.getNumberWrong();
	            		tmp += "\nNum. Questions: " + tmpScore.getNumberQuestions();
	            		showAlert("Quiz Results", tmp);
	        	        
	               		//remember selected index to select this row on resume...
	               		selectedIndex = position;
	        	        
	               	
	               
	           	}
	        };    
	        myListView.setOnItemClickListener(listItemClickHandler);             
     
            
    	}catch (Exception je){
    		//showAlert("Data Format Error", "There was a problem reading data associated with this app.");
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
	
	//download class in new thread / class
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
			
			if(downloadType == "text"){
				JSONData = appDelegate.downloadText(downloadURL);
				appDelegate.saveText(saveAsFileName, JSONData);
				downloadTextHandler.sendMessage(downloadTextHandler.obtainMessage());
				this.setThreadRunning(false);
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







