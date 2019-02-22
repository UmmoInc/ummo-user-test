package xyz.ummo.user;

import android.content.res.ColorStateList;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import xyz.ummo.user.adapters.CustomAdapter;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import static com.parse.Parse.getApplicationContext;

public class DelegationProgress extends AppCompatActivity {

    ListView progressList;
    ArrayList<Progress> progresses = new ArrayList();
    String processes[]= {"Form filled", "Service payment", "Collection"};
    ProgressBar progressBar;
    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegation_progress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadProcesses();

        progressBar = findViewById(R.id.delegation_progress_bar);
        progressBar.setProgressTintList(ColorStateList.valueOf(R.color.ummo_4));

        customAdapter = new CustomAdapter(this, progresses, progressBar);

        progressList = findViewById(R.id.progress_list);
        progressList.setAdapter(customAdapter);

        //checkButtonClick ();




    }

    /*private void checkButtonClick () {
        Button nextButton = (Button) findViewById(R.id.selected_done);
        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                StringBuffer display = new StringBuffer();
                display.append("the selected progress are");

                ArrayList<Progress> progressList = customAdapter.progressList;

                for (int i = 0; i < progressList.size(); i++){
                    Progress selected = progressList.get(i);
                    if (selected.isSelected()) {
                        display.append("\n" + selected.getProcessName());
                    }
                }

                Toast.makeText(getApplicationContext(), display, Toast.LENGTH_LONG).show();
            }
        });
    }*/


    public void loadProcesses(){

        Progress progress = new Progress("Form filled");
        progresses.add(progress);

        progress = new Progress("Service payment");
        progresses.add(progress);

        progress = new Progress("Collection");
        progresses.add(progress);



    }

}
