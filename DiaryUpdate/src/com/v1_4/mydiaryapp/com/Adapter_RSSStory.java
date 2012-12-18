package com.v1_4.mydiaryapp.com;
import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Adapter_RSSStory extends ArrayAdapter<Obj_RSSStory> {

  int resource;

  public Adapter_RSSStory(Context _context, int _resource, List<Obj_RSSStory> _items) {
    super(_context, _resource, _items);
    resource = _resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout itemView;

    Obj_RSSStory item = getItem(position);

    String title = item.getTitle();
    String link = item.getLink();
    String description = item.getDescription();
    int showAsSelected = item.getShowAsSelected();
    
    if (convertView == null) {
    	itemView = new LinearLayout(getContext());
    	String inflater = Context.LAYOUT_INFLATER_SERVICE;
    	LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
    	vi.inflate(resource, itemView, true);
    } else {
    	itemView = (LinearLayout) convertView;
    }

    //title
    TextView titleView = (TextView)itemView.findViewById(R.id.txtTitle);
    titleView.setText(title);
    
    //description
    TextView descView = (TextView)itemView.findViewById(R.id.txtDescription);
    descView.setText(description);
    
    //icon (defaults to blank.png in xml layout)
    ImageView iconView = (ImageView)itemView.findViewById(R.id.imgIcon);
   
    //chevron (defaults to blank.png in xml layout unless we have a title)
    ImageView chevronView = (ImageView)itemView.findViewById(R.id.imgChevron);
    if(title.length() > 1){

        if(showAsSelected == 1){
        	chevronView.setImageResource(R.drawable.chevron_1_red);
        }else{
        	chevronView.setImageResource(R.drawable.chevron_1);
        }
    }  
    
    
    return itemView;
  }
  
  
}





