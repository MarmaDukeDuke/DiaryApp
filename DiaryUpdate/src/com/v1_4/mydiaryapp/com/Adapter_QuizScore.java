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

public class Adapter_QuizScore extends ArrayAdapter<Obj_QuizScore> {

  int resource;

  public Adapter_QuizScore(Context _context, int _resource, List<Obj_QuizScore> _items) {
    super(_context, _resource, _items);
    resource = _resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout itemView;

    Obj_QuizScore item = getItem(position);

    String scoreDate = item.getScoreDate();
    String totalPoints = item.getTotalPoints();
    String totalSeconds = item.getTotalSeconds();
    String numberQuestions = item.getNumberQuestions();
    String numberRight = item.getNumberRight();
    String numberWrong = item.getNumberWrong();
     
    String showTitle = scoreDate;
    String showDescription = totalPoints + " pts. " + numberRight + " / " + numberQuestions;
    showDescription += " in " + totalSeconds + " secs.";
    
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
    titleView.setText(showTitle);
    
    //description
    TextView descView = (TextView)itemView.findViewById(R.id.txtDescription);
    descView.setText(showDescription);
    
    
    return itemView;
  }
  
  
}





