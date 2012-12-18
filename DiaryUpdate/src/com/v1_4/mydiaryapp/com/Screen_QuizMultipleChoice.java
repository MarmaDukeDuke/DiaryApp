package com.v1_4.mydiaryapp.com;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Screen_QuizMultipleChoice extends Act_ActivityBase {

	public DownloadThread downloadThread;
	public ArrayList<Obj_QuizQuestion> questions;
	public ArrayList<Obj_QuizQuestion> questionPool;
	public ArrayList<Bitmap> bonusImages;
	
	//media player for sound
	public MediaPlayer rightSoundPlayer;

	//quiz vars..
	public int questionDelay;
	public int numberRight;
	public int numberWrong;
	public int numberStreak;
	public int numberOfQuestions;
	public int currentQuestion;
	public int totalSeconds;
	public int totalScore;
	public Long startTime;
	public int allowNext;
	public int quizIsComplete;
	
	
	//components	
	public ImageView imgBackground;
	public ImageView imgRight;
	public ImageView imgWrong;
	public ImageView imgBonus;
	public TextView txtQuestion;
	public TextView txtScore;
	public TextView txtTimer;
	public Button btnQuestion_1;
	public Button btnQuestion_2;
	public Button btnQuestion_3;
	public Button btnQuestion_4;
	public RelativeLayout btnPanel;
	
	//onCreate
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_quizmultiplechoice);

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
        
        //set properties
    	thisActivityName = "Screen_QuizMultipleChoice";
    	remoteDataCommand = "startQuizViewController";
    	
    	//tell android we want device to control volume
    	this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

    	//init quiz numbers
    	questionDelay = 0;
    	numberRight = 0;
    	numberWrong = 0;
    	numberStreak = 0;
    	numberOfQuestions = 0;
    	currentQuestion = 0;
    	totalSeconds = 0;
    	totalScore = 0;
    	allowNext = 0;
    	quizIsComplete = 0;
  		questions = new ArrayList<Obj_QuizQuestion>(); 
  		questionPool = new ArrayList<Obj_QuizQuestion>(); 
  		bonusImages = new ArrayList<Bitmap>(); 
  		
         //appDelegate holds current screen
	    this.appDelegate = (AppDelegate) this.getApplication();
        appGuid = appDelegate.currentScreen.appGuid;
		appApiKey = appDelegate.currentApp.apiKey;
    	screenGuid = appDelegate.currentScreen.screenGuid;
    	saveAsFileName = appDelegate.currentScreen.screenGuid + "_quiz.txt";
    	
        //title
        ((TextView)findViewById(R.id.myTitle)).setText("Multiple Choice");

        //quiz properties are in appDelegate.currentScreen.jsonScreenOptions 
    	try{
    		JSONObject raw = new JSONObject(appDelegate.currentScreen.jsonScreenOptions);
   			numberOfQuestions = Integer.parseInt(raw.getString("numberOfQuestions")); 
   			questionDelay = Integer.parseInt(raw.getString("questionDelay")); 
   			
   		}catch (Exception je){
       		Log.i("ZZ", thisActivityName + ":onCreate error : " + appDelegate.currentScreen.jsonScreenOptions);   
       	}        	
        
        
        //image views
    	imgBackground = (ImageView) findViewById(R.id.imgBackground);
    	imgRight = (ImageView) findViewById(R.id.imgRight);
    	imgRight.setVisibility(INVISIBLE);
    	imgWrong = (ImageView) findViewById(R.id.imgWrong);
    	imgWrong.setVisibility(INVISIBLE);
    	imgBonus = (ImageView) findViewById(R.id.imgBonus);
    	imgBonus.setVisibility(INVISIBLE);
    	
        //round background image
        Bitmap quizImage = appDelegate.currentApp.image;				
        quizImage = appDelegate.roundImage(quizImage, 10);
        imgBackground.setImageBitmap(quizImage);
        
    	//text views    	
    	txtQuestion = (TextView) findViewById(R.id.txtQuestion);
    	txtScore = (TextView) findViewById(R.id.txtScore);
    	txtTimer = (TextView) findViewById(R.id.txtTimer);
    	
    	//buttons
        btnQuestion_1 = (Button) findViewById(R.id.btnQuestion_1);
        btnQuestion_2 = (Button) findViewById(R.id.btnQuestion_2);
        btnQuestion_3 = (Button) findViewById(R.id.btnQuestion_3);
        btnQuestion_4 = (Button) findViewById(R.id.btnQuestion_4);
        btnPanel = (RelativeLayout) findViewById(R.id.layoutButtons);
        btnPanel.setVisibility(INVISIBLE);
         
        //setup click handlers
        btnQuestion_1.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	answerClick(v);
            }
        });  
        btnQuestion_2.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	answerClick(v);
            }
        });         
        btnQuestion_3.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	answerClick(v);
            }
        }); 
        btnQuestion_4.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	answerClick(v);
            }
        }); 
        
        //load up bonus images for better performance
		Bitmap tmpImg;
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.app_blank);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.app_blank);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.app_blank);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_3x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_4x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_5x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_6x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_7x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_8x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_9x);				
			bonusImages.add(tmpImg);
		tmpImg = BitmapFactory.decodeResource(getResources(),R.drawable.quiz_9x);				
			bonusImages.add(tmpImg);        
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
		if(updateTimeHandler != null){
			updateTimeHandler.removeCallbacks(mUpdateTimeTask);
		}
		if(delayHandler != null){
			delayHandler.removeCallbacks(mDelayTask);
		}
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
	}
	
	
	//answer button clicks
	public void answerClick(View v){
	   Button clickedButton = (Button) v;
	   
	   //buttons depend on whether quiz is over or not
	   if(quizIsComplete < 1){
		   String selectedAnswer = clickedButton.getText().toString();
		   
		   //prevents super fast button taps..
		   if(allowNext > 0){
			   if(currentQuestion > -1 && questions.size() > 0){
				  
				   //flag..
				   allowNext = 0;
				   
				  Obj_QuizQuestion tmpQuestion = questions.get(currentQuestion);
				   String correctAnswer = tmpQuestion.getCorrectAnswer();
				   
				   //change button backgrounds, red for wrong, green for right
				   if(correctAnswer.equals(btnQuestion_1.getText().toString())){
					   btnQuestion_1.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_green));
				   }else{
					   btnQuestion_1.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_red));
				   }
				   if(correctAnswer.equals(btnQuestion_2.getText().toString())){
					   btnQuestion_2.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_green));
				   }else{
					   btnQuestion_2.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_red));
				   }
				   if(correctAnswer.equals(btnQuestion_3.getText().toString())){
					   btnQuestion_3.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_green));
				   }else{
					   btnQuestion_3.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_red));
				   }
				   if(correctAnswer.equals(btnQuestion_4.getText().toString())){
					   btnQuestion_4.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_green));
				   }else{
					   btnQuestion_4.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_red));
				   }
			   
				   //show image, calculate score..
				   if(selectedAnswer.equals(correctAnswer)){
					   
					   //counts..
					   numberRight = (numberRight + 1);
					   numberStreak = (numberStreak + 1);
					   if(numberStreak > 10) numberStreak = 10;
					   
					   //set score
					   int pointsPerRight = 150;
					   if(numberStreak > 2){
						   pointsPerRight = (pointsPerRight * numberStreak);
					   }
		
					   //increment score
					   totalScore = (totalScore + pointsPerRight);
					   txtScore.setText("" + totalScore);
					   
					   //set bonus image
					   imgBonus.setImageBitmap(bonusImages.get(numberStreak));
					   			   
					   //play sounds
					   if(rightSoundPlayer != null){
						   rightSoundPlayer.release();
					   }
					   rightSoundPlayer = MediaPlayer.create(getBaseContext(), R.raw.right);
					   rightSoundPlayer.start();
					   
					   fadeImage(imgRight);
					   fadeImage(imgBonus);
					   
				   }else{
					   numberWrong = (numberWrong + 1);
					   numberStreak = 0;
					   fadeImage(imgWrong);
				   }
			   
				   	//increment question
				   	currentQuestion = (currentQuestion + 1);
					
				   	//timer handle's showNextQuestion
				   	if(questionDelay > 10) questionDelay = 2;
			        delayHandler.removeCallbacks(mDelayTask);
			        delayHandler.postDelayed(mDelayTask, (questionDelay * 1000));
				   	
			   }else{
				   showAlert("Quiz Not Started", "Try using the refresh button on the devices menu?");
			   }
		   }//allowNext	   
		   
	   }else{
		   
		   //quiz is complete....
		   if(clickedButton.getId() == R.id.btnQuestion_1){
			   startCountdown();
		   }
		   if(clickedButton.getId() == R.id.btnQuestion_2){
			   showHighScores();
		   }	   
		   if(clickedButton.getId() == R.id.btnQuestion_3){
			  finish();
		   }
		   if(clickedButton.getId() == R.id.btnQuestion_4){
			   downloadData();
		   }
	   }
		  
	}
	
	/////////////////////////////////////////////////////////////////////
	//handles question delay updates after each answer
	Handler delayHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			delayHandler.removeCallbacks(mUpdateTimeTask);
		}
	};		
	
	private Runnable mDelayTask = new Runnable() {
		public void run() {
		    updateTimeHandler.sendMessage(delayHandler.obtainMessage());
		    showNextQuestion();
		}
	};	
	//end timer stuff
	/////////////////////////////////////////////////////////////////////
	
	//start countdown...
	public void startCountdown(){
		
		//hide controls
		txtTimer.setVisibility(INVISIBLE);
		txtScore.setVisibility(VISIBLE);
		txtQuestion.setVisibility(INVISIBLE);		
		btnPanel.setVisibility(INVISIBLE);		
		
		txtScore.setText("Quiz starting...");
	    	// SLEEP 2 SECONDS HERE ...
		    Handler handler = new Handler(); 
		    handler.postDelayed(new Runnable() { 
		         public void run() { 
		        	 startQuiz();
		         } 
		    }, 2000); 
	}
	
	//starts quiz..
	public void startQuiz(){
		
		//reset quiz
		numberRight = 0;
		numberWrong = 0;
		numberStreak = 0;
		totalSeconds = 0;
		currentQuestion = 0;
		allowNext = 1;
		quizIsComplete = 0;
		
		//reset text
		txtQuestion.setText("");
		txtScore.setText("");
		txtTimer.setText("");
		txtTimer.setVisibility(VISIBLE);
		txtScore.setVisibility(VISIBLE);
		txtQuestion.setVisibility(VISIBLE);

		btnQuestion_1.setText("");
		btnQuestion_2.setText("");
		btnQuestion_3.setText("");
		btnQuestion_4.setText("");
		
		
		//randomize questions from pool then grab "x" number for quiz
		if(questionPool.size() > 0){
			Collections.shuffle(questionPool);
	  		questions = new ArrayList<Obj_QuizQuestion>(); 
	        for (int i = 0; i < questionPool.size(); i++){
	        	if(i < numberOfQuestions){
	        		Obj_QuizQuestion thisQuestion = questionPool.get(i);
	        		questions.add(thisQuestion);
	        	}else{
	        		break;
	        	}
	        }//end for each
		}else{
			showAlert("No Questions?", "This quiz does not have any questions associated with it?");
		}
		

		//start timer...
        startTime = System.currentTimeMillis();
        updateTimeHandler.removeCallbacks(mUpdateTimeTask);
        updateTimeHandler.postDelayed(mUpdateTimeTask, 100);
        
        //dim background image
        imgBackground.setAlpha(35);
        
        //show next question
        showNextQuestion();
	}
	
		

	//end quiz
	public void endQuiz(){

		
		//re-display image
		imgBackground.setAlpha(255);
		
		//flag
		quizIsComplete = 1;
		
		
		//clear values
		currentQuestion = 0;
		txtQuestion.setText("");
		txtScore.setText("Quiz Complete");
		btnQuestion_1.setText("Try Again");
		btnQuestion_2.setText("Show Recent Scores");
		btnQuestion_3.setText("Quit");
		btnQuestion_4.setText("Refresh Questions");

		
		//hide controls
		txtTimer.setVisibility(INVISIBLE);
		txtScore.setVisibility(INVISIBLE);
		txtQuestion.setVisibility(INVISIBLE);		
		btnPanel.setVisibility(INVISIBLE);
		
		//turn off timer
		updateTimeHandler.removeCallbacks(mUpdateTimeTask);
		
		//show panel (buttons have different options now)
		slideButtons();
		
		//elapsed time
	    String tmpTime = "";
		long millis = System.currentTimeMillis() - startTime;
	    int seconds = (int) (millis / 1000);
	    int minutes = seconds / 60;
	    seconds = seconds % 60;
	    if(seconds < 10) {
	    	tmpTime =  minutes + ":0" + seconds;
	    }else{
	    	tmpTime = minutes + ":" + seconds;            
	    }
	    
	    totalSeconds = seconds;
	    
		//show alert
		String tmpResults = "Points: " + totalScore;
		tmpResults += "\nCorrect / Incorrect: " +  numberRight + " / " + questions.size();
		tmpResults += "\nElapsed Time: " + tmpTime;
		tmpResults += "\n\nClick OK to try again, show recent scores, refresh the questions, or quit.";
		showAlert("Quiz Complete", tmpResults);


		
	}
	
	//shows next question
	public void showNextQuestion(){
		
		//txtScore.setText("Curr: " + currentQuestion + "Tot: " + questions.size());

		//set button backgrounds
		btnQuestion_1.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_gray));
		btnQuestion_2.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_gray));
		btnQuestion_3.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_gray));
		btnQuestion_4.setBackgroundDrawable(getResources().getDrawable(R.drawable.quiz_btn_gray));
		btnQuestion_1.setVisibility(VISIBLE);
		btnQuestion_2.setVisibility(VISIBLE);
		btnQuestion_3.setVisibility(VISIBLE);
		btnQuestion_4.setVisibility(VISIBLE);

		
		
		//quiz may be over
		if(currentQuestion >= questions.size()){
			endQuiz();
		}else{
			
			
			
			//slide in buttons..
			slideButtons();
	        			
			//slide in question
			slideQuestion();
			
			Obj_QuizQuestion nextQuestion = questions.get(currentQuestion);
			
			//question
			txtQuestion.setText(nextQuestion.getQuestion());
			
			//randomize array of answers on buttons
			ArrayList answers = new ArrayList<String>();
			answers.add(nextQuestion.getWrong_1());
			answers.add(nextQuestion.getWrong_2());
			answers.add(nextQuestion.getWrong_3());
			answers.add(nextQuestion.getCorrectAnswer());
			Collections.shuffle(answers);
			
			//show on buttons
			btnQuestion_1.setText(answers.get(0).toString());
			btnQuestion_2.setText(answers.get(1).toString());
			btnQuestion_3.setText(answers.get(2).toString());
			btnQuestion_4.setText(answers.get(3).toString());
			
		}
		
		//flag allow next
		allowNext = 1;
		
		
	}
	

	/////////////////////////////////////////////////////////////////////
	//handles timer updates for quiz "running time"
	Handler updateTimeHandler = new Handler(){
		@Override public void handleMessage(Message msg){
	        
			//final long start = startTime;
		    long millis = System.currentTimeMillis() - startTime;
		    int seconds = (int) (millis / 1000);
		    int minutes = seconds / 60;
		    seconds = seconds % 60;
		    if(seconds < 10) {
		        txtTimer.setText("" + minutes + ":0" + seconds);
		    }else{
		    	txtTimer.setText("" + minutes + ":" + seconds);            
		    }
		        
		    //re-trigger timer
	        updateTimeHandler.removeCallbacks(mUpdateTimeTask);
	        updateTimeHandler.postDelayed(mUpdateTimeTask, 100);	
		}
	};		
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
		    updateTimeHandler.sendMessage(updateTimeHandler.obtainMessage());
		}
	};	
	//end timer stuff
	/////////////////////////////////////////////////////////////////////

		
		
	//download data
	public void downloadData(){
		//show progress
		showProgress("Loading...", "Gathering and randomizing quiz quesitons.");

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
	}
	
	//parse data..
	public void parseData(String theJSONString){
		//Log.i("ZZ", thisActivityName + ":parseData: " + theJSONString);      
		
		//hide controls
		txtTimer.setVisibility(INVISIBLE);
		txtScore.setVisibility(INVISIBLE);
		txtQuestion.setVisibility(INVISIBLE);		
		btnPanel.setVisibility(INVISIBLE);		
		
		//parse JSON string
    	try{

    		questionPool = new ArrayList<Obj_QuizQuestion>(); 
				
			JSONObject objRoot = new JSONObject(theJSONString);
			JSONArray tmpQuestions = objRoot.getJSONArray("questions");
	            
	        //loop    
	        for (int i = 0; i < tmpQuestions.length(); i++){
	        	
            		JSONObject tmpJson = tmpQuestions.getJSONObject(i);
            		Obj_QuizQuestion thisQuestion = new Obj_QuizQuestion(tmpJson.getString("questionText"));
            		thisQuestion.setWrong_1(tmpJson.getString("wrong1"));
            		thisQuestion.setWrong_2(tmpJson.getString("wrong2"));
            		thisQuestion.setWrong_3(tmpJson.getString("wrong3"));
            		thisQuestion.setCorrectAnswer(tmpJson.getString("correctAnswer"));
	            	
            		//add to questions array
            		questionPool.add(thisQuestion);

	        }//end for each
		        
    	}catch (Exception je){
    		showAlert("Data Format Error", "There was a problem reading  quiz data?");
    	}
		
		//hide progress
		hideProgress();   
		
		//start countdown
		startCountdown();
		
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
	
	
	//fade image in
	public void fadeImage(ImageView theImageView){
		try{
			theImageView.setVisibility(VISIBLE);
			Animation animation = new AlphaAnimation(1.0f, 0.0f);
			animation.setDuration(1200);
			theImageView.startAnimation(animation); 
			theImageView.setVisibility(INVISIBLE);
		}catch(Exception ex){
			
		}
	}	
	
	//slide buttons
	public void slideButtons(){
		try{
			btnPanel.setVisibility(VISIBLE);
			Animation animation = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
			animation.setDuration(500);
			btnPanel.startAnimation(animation); 
		}catch(Exception ex){
			
		}
	}		
	
	//slide question
	public void slideQuestion(){
		try{
			txtQuestion.setVisibility(VISIBLE);
			Animation animation = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
					Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
			animation.setDuration(500);
			txtQuestion.startAnimation(animation); 
		}catch(Exception ex){
			
		}
	}		


	//show high scores
	public void showHighScores(){
		
		//create an Obj_Screen from quiz results data..
		Obj_Screen tmpItem = new Obj_Screen("0000", screenGuid);
		tmpItem.setScreenType("screen_quizScores");
		tmpItem.setScreenTitle("Recent Scores");
        
        //create JSON from screen data
        String tmpKeys = "{";
        	tmpKeys += "\"scoreDate\":\"comesFromServer\",";
        	tmpKeys += "\"totalPoints\":\"" + totalScore + "\",";
        	tmpKeys += "\"totalSeconds\":\"" + totalSeconds + "\",";
        	tmpKeys += "\"numberQuestions\":\"" + questions.size() + "\",";
        	tmpKeys += "\"numberRight\":\"" + numberRight + "\",";
        	tmpKeys += "\"numberWrong\":\"" + numberWrong + "\"";
        	tmpKeys += "}";
        tmpItem.setJsonScreenOptions(tmpKeys);
		Log.i("ZZ", thisActivityName + ":keys: " + tmpKeys);      
   		//fire menuClick (method in Act_ActivityBase) to launch Screen_QuizScores
   		menuTap(tmpItem);
		
	}
	
	
	/////////////////////////////////////////////////////
	//options menu
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
		
		 //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.menu_quizmultiplechoice);
        dialog.setTitle("Screen Options");
        
        //high scores ..
        Button btnHighScores = (Button) dialog.findViewById(R.id.btnHighScores);
        btnHighScores.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
            	showHighScores();
            }
        });

        //refresh ..
        Button btnRefresh = (Button) dialog.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
                downloadData();
            }
        });

        //quit ..
        Button btnQuit = (Button) dialog.findViewById(R.id.btnQuit);
        btnRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	dialog.cancel();
                finish();
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







