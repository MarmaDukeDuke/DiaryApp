package com.v1_4.mydiaryapp.com;
import android.graphics.Bitmap;

public class Obj_App{

	String guid;
	String apiKey;
	String ownerGuid;
	String name;
	String version;
	String imageName;
	String imageUrl;
	Bitmap image;
	String iconName;
	String iconUrl;
	Bitmap icon;
	String templateType;
	String viewCount;
	String dateStampUTC;
	String modifiedUTC;
	String status;
	String dataUrl;
	
	//constructor
    public Obj_App(String _guid){
		guid = _guid;
	} 
    
    //getters / setters
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getOwnerGuid() {
		return ownerGuid;
	}

	public void setOwnerGuid(String ownerGuid) {
		this.ownerGuid = ownerGuid;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getViewCount() {
		return viewCount;
	}

	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}

	public String getDateStampUTC() {
		return dateStampUTC;
	}

	public void setDateStampUTC(String dateStampUTC) {
		this.dateStampUTC = dateStampUTC;
	}

	public String getModifiedUTC() {
		return modifiedUTC;
	}

	public void setModifiedUTC(String modifiedUTC) {
		this.modifiedUTC = modifiedUTC;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}	
	
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}	
	
}



