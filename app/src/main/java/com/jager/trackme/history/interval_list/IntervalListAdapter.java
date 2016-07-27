package com.jager.trackme.history.interval_list;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jager.trackme.R;
import com.jager.trackme.history.HistoryInterval;


import java.util.HashMap;
import java.util.List;

/**
 * Created by Jager on 2016.07.09..
 */
public class IntervalListAdapter extends BaseExpandableListAdapter
{
       public IntervalListAdapter(Context context, HashMap<String, List<HistoryInterval>> intervals, List<String> intervalSelector )
       {
              this.context = context;
              this.intervals = intervals;
              this.intervalSelector = intervalSelector;
       }

       private Context context;
       private HashMap<String, List<HistoryInterval>> intervals;
       private List<String> intervalSelector;

       @Override
       public int getGroupCount()
       {
              return intervalSelector.size();
       }

       @Override
       public int getChildrenCount(int i)
       {
              return  intervals.get(intervalSelector.get(i)).size();
       }

       @Override
       public Object getGroup(int i)
       {
              return  intervalSelector.get(i);
       }

       @Override
       public Object getChild(int parent, int child)
       {
              return intervals.get(intervalSelector.get(parent)).get(child);
       }

       @Override
       public long getGroupId(int i)
       {
              return i;
       }

       @Override
       public long getChildId(int parent, int child)
       {
              return child;
       }

       @Override
       public boolean hasStableIds()
       {
              return false;
       }

       @Override
       public View getGroupView(int parent, boolean isExpanded, View convertView, ViewGroup parentView)
       {
              String groupTitle = (String)getGroup(parent);
              if(convertView == null)
              {
                     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     convertView = inflater.inflate(R.layout.explist_parent, parentView, false);
              }
              TextView txtParent = (TextView)convertView.findViewById(R.id.txt_parent);
              txtParent.setTypeface(null, Typeface.BOLD);
              txtParent.setText(groupTitle);
              return convertView;
       }

       @Override
       public View getChildView(int parent, int child, boolean lastChild, View convertView, ViewGroup parentView)
       {
              String childTitle = ((HistoryInterval) getChild(parent, child)).toString();
              if(convertView == null)
              {
                     LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     convertView = inflater.inflate(R.layout.explist_child, parentView, false);
              }
              TextView txtChild = (TextView) convertView.findViewById(R.id.txt_child);
              txtChild.setText(childTitle);
              return convertView;
       }

       @Override
       public boolean isChildSelectable(int i, int i1)
       {
              return true;
       }
}
