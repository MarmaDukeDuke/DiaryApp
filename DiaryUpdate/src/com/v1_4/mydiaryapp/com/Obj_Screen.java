package com.v1_4.mydiaryapp.com;
import android.graphics.Bitmap;

public class Obj_Screen{

	String guid = "";
	String appGuid = "";  
    String screenGuid = "";  
    String menuText = "";  
    String screenTitle = "";
    String screenType = "";  
    String menuIcon = "";
    String jsonScreenOptions = "";
    Bitmap imgMenuIcon;
    int showAsSelected = 0;
    
	//constructor
    public Obj_Screen(String _appGuid, String _screenGuid){
    	guid = _screenGuid;
    	appGuid = _appGuid;
		screenGuid = _screenGuid;
	} 
	
    //getters / setters
	public String getAppGuid() {
		return appGuid;
	}
	public void setAppGuid(String appGuid) {
		this.appGuid = appGuid;
	}
	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return guid;
	}
	public String getScreenGuid() {
		return screenGuid;
	}
	public void setScreenGuid(String screenGuid) {
		this.screenGuid = screenGuid;
	}
	public String getMenuText() {
		return menuText;
	}
	public void setMenuText(String menuText) {
		this.menuText = menuText;
	}
	public String getScreenTitle() {
		return screenTitle;
	}
	public void setScreenTitle(String screenTitle) {
		this.screenTitle = screenTitle;
	}
	public String getScreenType() {
		return screenType;
	}
	public void setScreenType(String screenType) {
		this.screenType = screenType;
	}
	public String getMenuIcon() {
		return menuIcon;
	}
	public void setMenuIcon(String menuIcon) {
		this.menuIcon = menuIcon;
	}  
	public String getJsonScreenOptions() {
		return menuIcon;
	}
	public void setJsonScreenOptions(String jsonScreenOptions) {
		this.jsonScreenOptions = jsonScreenOptions;
	}  
	public Bitmap getImgMenuIcon() {
		return imgMenuIcon;
	}
	public void setImgMenuIcon(Bitmap imgMenuIcon) {
		this.imgMenuIcon = imgMenuIcon;
	}  
	public int getShowAsSelected() {
		return showAsSelected;
	}
	public void setShowAsSelected(int theInt) {
		this.showAsSelected = theInt;
	}    
	
}
