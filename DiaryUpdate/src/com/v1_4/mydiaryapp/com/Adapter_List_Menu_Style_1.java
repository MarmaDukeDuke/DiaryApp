package com.v1_4.mydiaryapp.com;
import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Adapter_List_Menu_Style_1 extends ArrayAdapter<Obj_Screen> {

  int resource;

  public Adapter_List_Menu_Style_1(Context _context, int _resource, List<Obj_Screen> _items) {
    super(_context, _resource, _items);
    resource = _resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout itemView;

    Obj_Screen item = getItem(position);

    String iconFileName = item.getMenuIcon();
    String titleString = item.getMenuText();
    Bitmap imgMenuIcon = item.getImgMenuIcon();
    int showAsSelected = item.getShowAsSelected();
    
    if (convertView == null) {
    	itemView = new LinearLayout(getContext());
    	String inflater = Context.LAYOUT_INFLATER_SERVICE;
    	LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
    	vi.inflate(resource, itemView, true);
    } else {
    	itemView = (LinearLayout) convertView;
    }

 
    //icon (defaults to blank.png in xml layout)
    ImageView iconView = (ImageView)itemView.findViewById(R.id.imgIcon);
    if(imgMenuIcon != null){
    	iconView.setImageBitmap(imgMenuIcon);
    }
   
    //text view
    TextView titleView = (TextView)itemView.findViewById(R.id.txtTitle);
    titleView.setText(titleString);
    
     
    //chevron (defaults to blank.png in xml layout unless we have a title)
    ImageView chevronView = (ImageView)itemView.findViewById(R.id.imgChevron);
    if(titleString.length() > 1){

        if(showAsSelected == 1){
        	chevronView.setImageResource(R.drawable.chevron_1_red);
        }else{
        	chevronView.setImageResource(R.drawable.chevron_1);
        }
    	
    }
    return itemView;
  }
  
  
}





