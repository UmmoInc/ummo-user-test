package xyz.ummo.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class AddCard extends AppCompatActivity {

    SlidingDrawer slidingDrawer;
    RelativeLayout mainScreen;
    EditText date, cvv, card_number;
    TextView slidingDrawerTitle, slidingDrawerText;
    Button slidingDrawerOkButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Add Card");

        card_number = findViewById(R.id.card_number);

        slidingDrawerOkButton = findViewById(R.id.sliding_drawer_ok_btn);

        slidingDrawerTitle = findViewById(R.id.sliding_drawer_title);
        slidingDrawerText = findViewById(R.id.sliding_drawer_text);


        slidingDrawer = findViewById(R.id.sliding_drawer);
        mainScreen = findViewById(R.id.main_box);
        slidingDrawer.bringToFront();

        date = findViewById(R.id.date);
        cvv = findViewById(R.id.cvv);

        if(card_number.isFocused()){



        }

        date.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (date.getRight() - date.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

                        slidingDrawerTitle.setText("Expiry date");
                        slidingDrawerText.setText("You should be able to find this date on the front of" +
                                " your card, under your card number");

                        slidingDrawer.open();

                        return true;
                    }
                }
                return false;
            }
        });

        cvv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (cvv.getRight() - cvv.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

                        slidingDrawerTitle.setText("CVV");
                        slidingDrawerText.setText("A three-digit code on your credit card which you can find on the back of your card");

                        slidingDrawer.open();

                        return true;
                    }
                }
                return false;
            }
        });

        slidingDrawerOkButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                slidingDrawer.close();

            }
        });

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,AddPaymentMethod.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, AddPaymentMethod.class);
                startActivity(intent);
                finish();
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
