package xyz.ummo.user.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import xyz.ummo.user.Progress;
import xyz.ummo.user.R;

import static com.parse.Parse.getApplicationContext;

public class CustomAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<Progress> progressList;
    int checkedProgresses = 0;
    int totalNumOfCheckboxes;
    int progressPercentage = 0;
    private ProgressBar progressBar;

    public CustomAdapter(Context context, ArrayList<Progress> progressList, ProgressBar progressBar) {

        //initialise context, arraylist and progressbar
        this.mContext = context;
        this.progressBar = progressBar;
        this.progressList = progressList;

    }

    private class ViewHolder {
        TextView processName;
        CheckBox progressCheck;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent){
        // TODO Auto-generated method stub

        ViewHolder holder = null;
        Log.v("Convert View", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            convertView = inflater.inflate(R.layout.progress_list, null);

            totalNumOfCheckboxes = progressList.size();

            holder = new ViewHolder();
            holder.processName = (TextView) convertView.findViewById(R.id.process_name);
            holder.progressCheck = (CheckBox) convertView.findViewById(R.id.progress_check);

            convertView.setTag(holder);

            holder.progressCheck.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                    CheckBox cb = (CheckBox) v;
                    Progress progress = (Progress) cb.getTag();

                    if(cb.isChecked()){

                        //when a checkbox is checked increase the progress percentage and up date the progressbar

                        progress.setSelected(cb.isChecked());
                        checkedProgresses++;

                        progressPercentage = (checkedProgresses * progressBar.getMax()) /totalNumOfCheckboxes;

                        progressBar.setProgress(progressPercentage);
                    }

                    else{

                        //when a checkbox is unchecked reduce the progress percentage and update the progressbar

                        progress.setSelected(false);
                        checkedProgresses--;

                        progressPercentage = (checkedProgresses * progressBar.getMax()) /totalNumOfCheckboxes;

                        progressBar.setProgress(progressPercentage);
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Progress progress = progressList.get(position);
        holder.processName.setText("(" + progress.getProcessName() + ")");
        holder.progressCheck.setChecked(progress.isSelected());
        holder.progressCheck.setTag(progress);
        holder.processName.setText(progress.getProcessName());

        return convertView;
    }


    @Override
    public int getCount() {
        return progressList.size();
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





