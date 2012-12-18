package com.v1_4.mydiaryapp.com;
import android.graphics.Bitmap;

public class Obj_QuizQuestion{

	String question;
	String wrong_1;
	String wrong_2;
	String wrong_3;
	String correctAnswer;
	
	//constructor
    public Obj_QuizQuestion(String _question){
    	question = _question;
	}

    //getters / setters

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getWrong_1() {
		return wrong_1;
	}

	public void setWrong_1(String wrong_1) {
		this.wrong_1 = wrong_1;
	}

	public String getWrong_2() {
		return wrong_2;
	}

	public void setWrong_2(String wrong_2) {
		this.wrong_2 = wrong_2;
	}

	public String getWrong_3() {
		return wrong_3;
	}

	public void setWrong_3(String wrong_3) {
		this.wrong_3 = wrong_3;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}

    
	
	
}



