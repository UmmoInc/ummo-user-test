package xyz.ummo.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class DelegationChat extends AppCompatActivity {

    private boolean isVisible = true;
    RelativeLayout confirmInitiationBox;
    RelativeLayout confirmInitiationContentBox;
    private ExpandOrCollapse mAnimationManager;
    private ImageView arrow;
    private ImageView sendButton;
    private ScrollView chatRoom;
    private boolean hasCheckedServiceInitConfirmation = false;
    private boolean hasInitiatedService;
    private ProgressBar circularProgressBar;
    private ImageView homeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegation_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Delegation Chat");

        //initiate the home and progressbar icon in the toolbar
        circularProgressBar = findViewById(R.id.circular_progressbar_btn);
        homeButton = findViewById(R.id.home_icon_button);

        //check if the service has been initiated
        hasInitiatedService = getIntent().getExtras().getBoolean("hasInitiatedService");

        chatRoom = findViewById(R.id.chat_room);

        arrow = findViewById(R.id.arrow_down_up);
        sendButton = findViewById(R.id.send_btn);

        confirmInitiationBox = findViewById(R.id.confirm_service_initiation_box);
        confirmInitiationContentBox = findViewById(R.id.confirm_initiation_content_box);


        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAnimationManager = new ExpandOrCollapse();
        confirmInitiationBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVisible) {

                    ExpandOrCollapse.expand(confirmInitiationContentBox, 500);
                    isVisible = false;
                    rotate(-180);

                    if(!hasCheckedServiceInitConfirmation){

                        ViewTooltip
                                .on(chatRoom)
                                .position(ViewTooltip.Position.TOP)
                                .text(getResources().getString(R.string.follow_up_agent_string))
                                .show();

                        hasCheckedServiceInitConfirmation = true;

                    }

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

                } else if (!isVisible){

                    ExpandOrCollapse.expand(confirmInitiationContentBox, 500);
                    isVisible = true;
                    rotate(360);

                }
            }
        });

        if(hasInitiatedService){

            ExpandOrCollapse.expand(confirmInitiationBox, 100);
            ExpandOrCollapse.expand(confirmInitiationContentBox, 100);
            circularProgressBar.setVisibility(View.VISIBLE);


        }


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDelegatePogress();

            }


        });

        //set the home icon onclick method
        homeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                goToHome();

            }
        });

        //set the circular progress bar icon onclick method
        circularProgressBar.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                goToDelegatePogress();

            }
        });
    }

    private void rotate(float degree) {
        final RotateAnimation rotateAnim = new RotateAnimation(0.0f, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(10);
        rotateAnim.setFillAfter(true);
        arrow.startAnimation(rotateAnim);
    }

    public void goToDelegatePogress(){

        Intent intent = new Intent(this, DelegationProgress.class);
        finish();
        startActivity(intent);

    }

    public void goToHome(){

        Intent intent = new Intent(this, MainScreen.class);
        finish();
        startActivity(intent);

    }

}
