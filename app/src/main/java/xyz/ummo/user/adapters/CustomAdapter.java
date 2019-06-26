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

import xyz.ummo.user.ui.MainScreen;
import xyz.ummo.user.Progress;
import xyz.ummo.user.R;

//import static com.parse.Parse.getApplicationContext;

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


                        //calculate the progress
                        progressPercentage = (checkedProgresses * progressBar.getMax()) /totalNumOfCheckboxes;

                        //update progress in to th ProgressBar
                        progressBar.setProgress(progressPercentage);


                        //check if the ProgressBar is full
                        if(progressBar.getProgress() == progressBar.getMax()){

                            // if ProgressBar is full show an alert dialog to tell the user the service is complete
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:

                                            //if the user press the 'ACCEPT' button, the user should be prompted to rate the agent
                                            showRatingAgentDiaolog();

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle(R.string.service_complete_title_string);
                            builder.setMessage(R.string.service_complete_string)
                                    .setPositiveButton("ACCEPT", dialogClickListener)
                                    .setNegativeButton("DECLINE", dialogClickListener).show();

                            AlertDialog a=builder.create();


                        }
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

    public void showRatingAgentDiaolog(){

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(mContext);

        LinearLayout linearLayout = new LinearLayout(mContext);
        final RatingBar rating = new RatingBar(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //set rating style and number of styles

        rating.setLayoutParams(lp);
        rating.setNumStars(5);
        rating.setStepSize(1);

        //create the "how can agent improve edittext"
        final EditText  improveMessage = new EditText(mContext);

        improveMessage.setLayoutParams(lp2);
        improveMessage.setHint("How can agent improve?");

        //add ratingBar to linearLayout
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(rating);
        linearLayout.addView(improveMessage);

        linearLayout.setGravity(Gravity.CENTER);


        popDialog.setTitle("Rate Agent's Service");
        popDialog.setMessage("Take a moment to rate Agent (out of 5 stars)");

        //add linearLayout to dailog
        popDialog.setView(linearLayout);



        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                System.out.println("Rated val:"+v);
            }
        });

        popDialog.setCancelable(false);
        popDialog.setPositiveButton("SUBMIT",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(mContext, MainScreen.class);
                        ((Activity)mContext).finish();
                        mContext.startActivity(intent);
                    }

                });

        popDialog.create();
        popDialog.show();

    }

    public int getProgressPercentage() {
        return progressPercentage;
    }
}





