package xyz.ummo.user.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.ummo.user.MainScreen;
import xyz.ummo.user.Progress;
import xyz.ummo.user.R;

public class CustomStepsAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<String> steps = new ArrayList<>();

    public CustomStepsAdapter(Context context, ArrayList<String> steps) {

        this.mContext = context;
        this.steps = steps;

    }

    private class ViewHolder {
        TextView step;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent){
        // TODO Auto-generated method stub

        ViewHolder holder = null;
        Log.v("Convert View", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            convertView = inflater.inflate(R.layout.steps_list, null);

            holder = new ViewHolder();
            holder.step = convertView.findViewById(R.id.step);

            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        String step = steps.get(position);

        holder.step.setText(position + 1 + ". " + step);



        return convertView;
    }


    @Override
    public int getCount() {
        return steps.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}





