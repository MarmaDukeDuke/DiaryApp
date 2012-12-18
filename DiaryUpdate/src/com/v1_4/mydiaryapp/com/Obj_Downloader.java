package com.v1_4.mydiaryapp.com;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;


public class Obj_Downloader {
    
	
	 public static InputStream OpenHttpConnection(String urlString) 
	    throws IOException{
	        InputStream in = null;
	        int response = -1;
	               
	        URL url = new URL(urlString); 
	        URLConnection conn = url.openConnection();
	                 
	        if (!(conn instanceof HttpURLConnection))                     
	            throw new IOException("Not an HTTP connection");
	        try{
	            HttpURLConnection httpConn = (HttpURLConnection) conn;
	            httpConn.setAllowUserInteraction(false);
	            httpConn.setInstanceFollowRedirects(true);
	            httpConn.setRequestMethod("GET");
	            httpConn.connect(); 
	            response = httpConn.getResponseCode();                 
	            if (response == HttpURLConnection.HTTP_OK) {
	                in = httpConn.getInputStream();                                 
	            }     
	        }catch (Exception ex){
            	Log.i("ZZ: Act_MyAgent:onStop ERROR", ex.getMessage());      
	            throw new IOException("Error connecting");            
	        }
	        return in;     
	    }
    
}


