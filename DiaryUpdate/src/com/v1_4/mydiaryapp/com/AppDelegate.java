package com.v1_4.mydiaryapp.com;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


public class AppDelegate extends Application  {

	   	//persisted strings
		public int downloadInProgress = 0;
		public int foundUpdatedLocation = 0;
		public int selectedTab = 0;
	   
		//persisted objects
		public static Obj_Device currentDevice;
		public static Obj_User currentUser;
		public static Obj_App currentApp;
		public static Obj_Screen currentScreen;
		public static ArrayList<String> root_iconsFileNames;
		public static ArrayList<Bitmap> root_iconsBitmaps;

		@Override
		public void onCreate(){
			super.onCreate();
			//Log.i("ZZ", "AppDelegate:onCreate---------------------");
			
			//init root application, user, device, icons (icons are loaded into memory for performance)
			downloadInProgress = 0;
			initRootAppVars();
			initUser();
			initDevice();
			initIcons();
			
		}
		
		//init root application variables
		public void initRootAppVars(){
	    	try{
	    		InputStream is = this.getResources().openRawResource(R.raw.config);
				byte [] buffer = new byte[is.available()];
				while (is.read(buffer) != -1);
				String json = new String(buffer);
	            JSONObject obj = new JSONObject(json);
	            
	            //appData..
	            JSONObject rootObj =  obj.getJSONObject("appData");
              	
               	//init current app
	    	    currentApp = new Obj_App(rootObj.getString("appGuid"));
               	currentApp.setApiKey(rootObj.getString("appApiKey"));
	    	    currentApp.setDataUrl(rootObj.getString("appDataURL"));
               	
  	        }catch (Exception je){
  	        	Log.i("ZZ", "AppDelegate:initAppVars: ERROR: " + je.getMessage());      
	        }
		}
		
	    //read zipped directory of icons and caches them in memory for later use..
	    public void initIcons(){
	    	root_iconsFileNames = new ArrayList<String>();
	        root_iconsBitmaps = new ArrayList<Bitmap>();        
    		try{
    			Resources res = this.getResources();
    			InputStream iconInputStream = res.openRawResource(R.raw.icons);
    			ZipInputStream zipStream = new ZipInputStream(iconInputStream);
    		    ZipEntry entry;

    		    while ((entry = zipStream.getNextEntry()) != null) {
    		    	//file name may start with MACOSX. This is strange, ignore it.
    		    	String fileName = entry.getName();
    		    	if(fileName.length() > 1){
	    		    	if(fileName.contains(".png") && !entry.isDirectory()){
	    		    		if(!fileName.contains("MACOSX")){ //OSX adds junk sometimes, ignore it
	    		    			fileName = fileName.replace("icons/","");
	    		    			Bitmap iconBitmap = BitmapFactory.decodeStream(zipStream); 
	    		    			root_iconsFileNames.add(fileName);
	    		    			root_iconsBitmaps.add(iconBitmap);
	    		    			//Log.i("ZZ", "loading bitmaps: " + fileName); 
	    		    		}
	    		    	} //macosx
	    		    } // fileName
    		    }//end while
    		    
    		    //clean up
    		    if(zipStream != null){
    		    	zipStream.close();
    		    }
    		    
    		}catch(Exception ex){
 			   Log.i("ZZ", "getZippedIcon: " + ex.toString());      
    		}	 
	    }
		
		
		//init user
		public void initUser(){
	    	try{
	    		this.currentUser = new Obj_User();
			    this.currentUser.userGuid = "";
			    this.currentUser.userEmail = "";
			    this.currentUser.contactName = "David Book";
			    this.currentUser.contactEmail = "";
			    this.currentUser.contactPhone = "";
			    this.currentUser.defaultAppGuid = "";
			    this.currentUser.isLoggedIn = 0;	 
		    }catch (Exception je){
            	Log.i("ZZ", "AppDelegate:initUser: ERROR: " + je.getMessage());      
	        }
		}
			
		//init device
		public void initDevice(){
			try{
				this.currentDevice = new Obj_Device();
				
				//telephony info for device and internet connection info...
			    TelephonyManager TelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
		        String uuid = TelephonyManager.getDeviceId();
		        String phoneNumber= TelephonyManager.getLine1Number();
		        String softwareVer = TelephonyManager.getDeviceSoftwareVersion();
		        String simSerial = TelephonyManager.getSimSerialNumber();
		        
				//connection info...
				String connectionType = "none";
				ConnectivityManager mConnectivity = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo info = mConnectivity.getActiveNetworkInfo(); 
				if (info == null || !mConnectivity.getBackgroundDataSetting()){
					//no connection..
	            	Log.i("ZZ", "AppDelegate:initDevice: NO CONNECTION");      
				}else{			
					int netType = info.getType();
					if (netType == ConnectivityManager.TYPE_WIFI) {
					    connectionType = "WIFI";
					} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					       connectionType = "CELL";
					} else {
					    connectionType = "none";
					}
				}
				
		        //display info
				Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
				
				//get rid of spaces in model
				String tmpDeviceModel = Build.BRAND + "-" + Build.MODEL;
				tmpDeviceModel = tmpDeviceModel.replaceAll(" ","-");
				
				//get rid of spaces in brand
				String tmpDeviceBrand = Build.BRAND;
				tmpDeviceBrand = tmpDeviceBrand.replaceAll(" ","");
				
				
				//set vars..
			    this.currentDevice.deviceId = uuid;
			    this.currentDevice.deviceModel = tmpDeviceModel;
			    this.currentDevice.deviceVersion = Build.VERSION.RELEASE;
			    this.currentDevice.deviceConnectionType = connectionType;
			    this.currentDevice.devicePhoneNumber = phoneNumber;
			    this.currentDevice.deviceCarrier = tmpDeviceBrand;
			    this.currentDevice.deviceHeight = display.getHeight();
				this.currentDevice.deviceWidth = display.getWidth();
			    this.currentDevice.deviceCustomId = "";
				this.currentDevice.deviceLatitude = "";
			    this.currentDevice.deviceLongitude = "";
								
		
		    }catch (Exception je){
            	Log.i("ZZ", "AppDelegate:initDevice: ERROR: " + je.getMessage());      
	        }
			
		}
		
		
		////////////////////////////////////////////////////////////////////
		//file methods
		
		//get image from file system
		public Bitmap getLocalImage(String fileName){
			Bitmap img = null;
			try{
				FileInputStream fin = openFileInput(fileName);
				if(fin != null){
					img = BitmapFactory.decodeStream(fin);
					fin.close();
				}
			}catch (Exception je){
				img = null;
				Log.i("ZZ", "AppDelegate:getLocalImage: ERROR: " + je.getMessage());      
			}
    		//Log.i("ZZ", "AppDelegate:getLocalImage: " + fileName);      
			return img;
		}
		
		//get text from file system
		public String getLocalText(String fileName){
			String r = "";
			int BUFFER_SIZE = 2000;
	    	if(fileName.length() > 1){
	    		try{
	    	    	FileInputStream fin = openFileInput(fileName);
	    	    	if(fin != null){
 	    	    		InputStreamReader isr = new InputStreamReader(fin);
 	    	    	int charRead;
	    	        	char[] inputBuffer = new char[BUFFER_SIZE];          
	    	        	try{
	    	            	while ((charRead = isr.read(inputBuffer))>0){                    
	    	            		String readString = String.copyValueOf(inputBuffer, 0, charRead);                    
	    	                	r += readString;
	    	                	inputBuffer = new char[BUFFER_SIZE];
	    	            	}
	    	            	fin.close();
	    	        	}catch(IOException e){
	    	        		r = "";
	                		Log.i("ZZ", "AppDelegate:getLocalText: ERROR - 1: " + e.getMessage());      
	    	        	}    	    	    	
	    	    	}
	        	}catch (Exception je){
	        		r = "";
            		//Log.i("ZZ", "AppDelegate:getLocalText: ERROR - 2: " + je.getMessage());      
	        	}
	    	}else{
	    		r = "";
	    	}
    		//Log.i("ZZ", "AppDelegate:getLocalText: " + fileName);      
	        return r;
		}
		
		//save image
		public void saveBitmap(String fileName, Bitmap theImage){
			try{
				if(fileName.length() > 5 && theImage != null){
					FileOutputStream fos = super.openFileOutput(fileName, MODE_WORLD_READABLE);
					theImage.compress(CompressFormat.JPEG, 100, fos);
					fos.flush();
					fos.close();
				}
			}catch (Exception je) {
		        Log.i("ZZ", "AppDelegate:saveBitmap ERROR: " + je.getMessage());      
			}
		}
		
		//save text file
		public void saveText(String fileName, String theText){
			try{
				if(theText.length() > 5 && fileName.length() > 5){
					FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
					fos.write(theText.getBytes());
					fos.close();
				}
			}catch (Exception je) {
		        Log.i("ZZ", "AppDelegate:saveText ERROR: " + je.getMessage());      
			}
		}
		
		
		//check if file exists
		public boolean fileExists(String fileName){
			boolean exists = false;
			try{
	    	    FileInputStream fin = openFileInput(fileName);
				if(fin == null){
					exists = false;
				}else{
					exists = true;
					fin.close();
				}
			}catch (Exception je) {
		        //Log.i("ZZ", "AppDelegate:fileExists ERROR: " + je.getMessage()); 
		        exists = false;
			}
			return exists;
		}
		
		
		//list local data
		public void showLocalData(){
	    	try{
	    		PackageManager pm = getPackageManager();
	    		ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
	    		File sharedPrefsDir = new File(appInfo.dataDir + "/files");
	    		
	    		File[] prefFiles = sharedPrefsDir.listFiles();
	    		for (File f : prefFiles) {
	            	Log.i("ZZ", "showLocalData: file name: " + f.getName());      
	    		}
	    	}catch (Exception je){
            	Log.i("ZZ", "showLocalData: ERROR: " + je.getMessage());      
	        }
		}
		

		//delete local data
		public void deleteLocalData(){
	    	try{
	    		PackageManager pm = getPackageManager();
	    		ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
	    		File sharedPrefsDir = new File(appInfo.dataDir + "/files");
	    		File[] prefFiles = sharedPrefsDir.listFiles();
	    		for (File f : prefFiles){
	    	        //Log.i("ZZ", "deleteLocalData: file: " + f.getName());      
	    			f.delete();
	    		}
	    	}catch (Exception je){
            	Log.i("ZZ", "deleteLocalData: ERROR: " + je.getMessage());      
	        }
			
		}

		//delete local prefs
		public void deleteLocalPrefs(){
	    	try{
	    		PackageManager pm = getPackageManager();
	    		ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
	    		File sharedPrefsDir = new File(appInfo.dataDir + "/shared_prefs");
	    		File[] prefFiles = sharedPrefsDir.listFiles();
	    		for (File f : prefFiles){
	    			f.delete();
	    		}
	    	}catch (Exception je){
            	Log.i("ZZ", "deleteLocalPrefs: ERROR: " + je.getMessage());      
	        }
			
		}


		
		//end file methods
		////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////
	    //download methods
	    public Bitmap downloadImage(String theUrl){  
	    	//Log.i("ZZ", "AppDelegate downloadImage: " + theUrl);  
	    	downloadInProgress = 1;
	        InputStream in = null;  
	    	Bitmap bitmap = null;
	        try{
	        	in = Obj_Downloader.OpenHttpConnection(theUrl);
		        bitmap = BitmapFactory.decodeStream(in);
	        }catch (Exception e) {
            	Log.i("ZZ", "AppDelegate:downloadImage: ERROR: " + e.getMessage());      
	            //return null;
	        }
	        downloadInProgress = 0;
	        return bitmap;                
	    } 	    
			
	    public String downloadText(String URL){
 		    //Log.i("ZZ", "App Delegate:downloadText URL: " + URL);  
	    	downloadInProgress = 1;
 		    int BUFFER_SIZE = 2000;
	        InputStream in = null;
	        String str = "";
	        try {
	            in = Obj_Downloader.OpenHttpConnection(URL);
	            InputStreamReader isr = new InputStreamReader(in);
	            int charRead;
	            char[] inputBuffer = new char[BUFFER_SIZE];          
	            try{
	            	while ((charRead = isr.read(inputBuffer))>0){                    
	                //---convert the chars to a String---
	                String readString = String.copyValueOf(inputBuffer, 0, charRead);                    
	                str += readString;
	                inputBuffer = new char[BUFFER_SIZE];
	            }
	            in.close();
	            }catch(IOException e){
	 			   Log.i("ZZ", "AppDelegate:downloadText: ERROR - 1: " + e.getMessage());      
	            	str = "";
	            }
	       }catch (Exception je){
			   Log.i("ZZ", "AppDelegate:downloadText: ERROR - 2: " + je.getMessage());      
		   }  
	       downloadInProgress = 0;
	       return str;   
	    }	
	    
	    //download / save binary file
	    public boolean downloadAndSaveBinaryFile(String theUrl, String saveAsFileName){
 		    //Log.i("ZZ", "AppDelegate:downloadAndSaveBinaryFile: " + URL);  
	       	try {
		          
	            	URL u = new URL(theUrl);
	                URLConnection uc = u.openConnection();
	                String contentType = uc.getContentType();
	                int contentLength = uc.getContentLength();
	                if (contentType.startsWith("text/") || contentLength == -1) {
	                  return false; // not binary
	                }
	                InputStream raw = uc.getInputStream();
	                InputStream in = new BufferedInputStream(raw);
	                byte[] data = new byte[contentLength];
	                int bytesRead = 0;
	                int offset = 0;
	                while (offset < contentLength) {
	                  bytesRead = in.read(data, offset, data.length - offset);
	                  if (bytesRead == -1)
	                    break;
	                  offset += bytesRead;
	                }
	                in.close();
	 			   	
	 			   
	                if (offset != contentLength) {
		                  return false; //problem reading stream?
	                }
	                

	                //save file
					FileOutputStream fos = super.openFileOutput(saveAsFileName, MODE_WORLD_READABLE);
					fos.write(data);
					fos.flush();
					fos.close();
	                
	                return true;
	       }catch (Exception je){
			   Log.i("ZZ", "AppDelegate:downloadAndSaveBinaryFile: ERROR - 2: " + je.getMessage());      
	    	   return false;
		   }  
	    }	    
		    
		//returns image with rounded corners
		public Bitmap roundImage(Bitmap bitmap, int pixels) {
	        			
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);

	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        final RectF rectF = new RectF(rect);
	        final float roundPx = pixels;

	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

	        
	        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);

	        return output;
	    }	    
	    
	
}












