package com.example.qw.memo;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MemoAdapter extends ArrayAdapter<OneMemo>{

    private int resourceId;
    int[] color={Color.parseColor("#F5EFA0"), Color.parseColor("#8296D5"),Color.parseColor("#95C77E"),Color.parseColor("#F49393"),Color.parseColor("#FFFFFF")};

    public MemoAdapter(Context context, int resource, List<OneMemo> objects) {
        super(context, resource, objects);
        resourceId=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OneMemo memo=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent, false);

        ImageView tag=(ImageView)view.findViewById(R.id.tag);
        TextView textDate=(TextView)view.findViewById(R.id.textDate);
        TextView textTime=(TextView)view.findViewById(R.id.textTime);
        ImageView alarm=(ImageView) view.findViewById(R.id.alarm);
        TextView textTitle = view.findViewById(R.id.textTitle);
        TextView mainText=(TextView)view.findViewById(R.id.mainText);

        if(memo.getTag()<color.length)
            tag.setBackgroundColor(color[memo.getTag()]);
        textDate.setText(memo.getTextDate());
        textTime.setText(memo.getTextTime());
        if(memo.getAlarm()) {
            alarm.setVisibility(View.VISIBLE);
        }
        else {
            alarm.setVisibility(View.GONE);
        }
        textTitle.setText(memo.getTextTitle());
        mainText.setText(memo.getMainText());

        return view;
    }
}

