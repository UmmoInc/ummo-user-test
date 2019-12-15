package xyz.ummo.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.github.nkzawa.emitter.Emitter;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import xyz.ummo.user.delegate.GetService;
import xyz.ummo.user.delegate.Service;


import xyz.ummo.user.adapters.MessageAdapter;
import xyz.ummo.user.delegate.SendChatMessage;
import xyz.ummo.user.delegate.SocketIO;
import xyz.ummo.user.delegate.User;
import xyz.ummo.user.ui.MainScreen;

public class DelegationChat extends AppCompatActivity {

    private boolean isVisible = true;
    RelativeLayout confirmInitiationBox;
    RelativeLayout confirmInitiationContentBox;
    private ExpandOrCollapse mAnimationManager;
    private ImageView arrow;
    private boolean hasCheckedServiceInitConfirmation = false;
    private boolean hasInitiatedService;
    private ProgressBar circularProgressBar;
    private ImageView homeButton;

    private ListView listView;
    private View btnSend;
    private EditText editText;
    boolean myMessage = true;
    private List<ChatBubble> ChatBubbles= new ArrayList<ChatBubble>();
    private ArrayAdapter<ChatBubble> adapter;
    private static final String TAG = "DelegationChat";

    @Override
    protected void onDestroy() {
      //  SocketIO.INSTANCE.getMSocket().off("message");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
      //  SocketIO.INSTANCE.getMSocket().off("message");
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegation_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        setTitle(Objects.requireNonNull(intent.getExtras()).getString("AGENT_NAME"));
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(intent.getExtras().getString("SERVICE_NAME"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String service_id = getIntent().getStringExtra("SERVICE_ID");

        //initiate the home and progressbar icon in the toolbar
        circularProgressBar = findViewById(R.id.circular_progressbar_btn);

        //check if the service has been initiated

        arrow = findViewById(R.id.arrow_down_up);

        confirmInitiationBox = findViewById(R.id.confirm_service_initiation_box);
        confirmInitiationContentBox = findViewById(R.id.confirm_initiation_content_box);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAnimationManager = new ExpandOrCollapse();
        confirmInitiationBox.setOnClickListener(v -> {
            if (isVisible) {

                ExpandOrCollapse.expand(confirmInitiationContentBox, 500);
                isVisible = false;
                rotate(-180);

                if(!hasCheckedServiceInitConfirmation){

//                        ViewTooltip
//                                .on(chatRoom)
//                                .position(ViewTooltip.Position.TOP)
//                                .text(getResources().getString(R.string.follow_up_agent_string))
//                                .show();

                    hasCheckedServiceInitConfirmation = true;
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

            } else {

                ExpandOrCollapse.expand(confirmInitiationContentBox, 500);
                isVisible = true;
                rotate(360);
            }
        });
//
//        if(hasInitiatedService){
//
//            ExpandOrCollapse.expand(confirmInitiationBox, 100);
//            ExpandOrCollapse.expand(confirmInitiationContentBox, 100);
//            circularProgressBar.setVisibility(View.VISIBLE);
//
//
//        }

        //set ListView adapter first
        adapter = new MessageAdapter(this, R.layout.left_chat_bubble, ChatBubbles);
        listView = findViewById(R.id.list_msg);
        listView.setAdapter(adapter);
        btnSend = findViewById(R.id.send_btn);
        editText = findViewById(R.id.message);
     //   Log.e("Connected", SocketIO.INSTANCE.getMSocket().connected()+"");

        new GetService(this,service_id){
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(new  String(data));
                        Log.e(TAG,obj.toString());
                        JSONArray chatArr = obj.getJSONArray("chat");
                        for (int i = 0;i<chatArr.length(); i++){
                            JSONObject message = chatArr.getJSONObject(i);
                            ChatBubble chatBubble = new ChatBubble(message.getString("message"), message.getString("from").equals("user"));
                            ChatBubbles.add(chatBubble);
                        }
                        adapter.notifyDataSetChanged();

                    }catch (JSONException e){
                        Log.e(TAG, "JSON-Exc"+e.toString());
                    }

                });

            }
        };

        SocketIO.INSTANCE.getMSocket().on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG,"Received");
                try {
                    JSONObject message = new JSONObject(args[0].toString());
                    Log.e(TAG,"Equals"+message.getString("from").equals("agent"));
                    ChatBubble chatBubble = new ChatBubble(message.getString("message"), message.getString("from").equals("user"));

                    runOnUiThread(() -> {
                        ChatBubbles.add(chatBubble);
                        adapter.notifyDataSetChanged();
                    });

                }catch (JSONException e){
                    Log.e(TAG, "MESS-ERR"+ e.toString());
                }
            }
        });

        //event for button SEND
        btnSend.setOnClickListener(v -> {

            MixpanelAPI mixpanel =
                    MixpanelAPI.getInstance(this,
                            getResources().getString(R.string.mixpanelToken));

            String message = editText.getText().toString().trim();

            if (message.equals("")) {
                Toast.makeText(DelegationChat.this, "Please input some text...", Toast.LENGTH_SHORT).show();
            } else {
                new SendChatMessage(message, service_id){
                    @Override
                    public void done(@NotNull byte[] data, @NotNull Number code) {

                        JSONObject messageObject = new JSONObject();

                        try {
                            messageObject.put("message", message);
                            messageObject.put("serviceId", service_id);

                            if (mixpanel != null) {
                                mixpanel.track("sendMessageTapped", messageObject);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e(TAG, "SENT-MESSAGE->"+new String(data));
                    }
                };
            }
            editText.setText("");
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rotate(float degree) {
        final RotateAnimation rotateAnim = new RotateAnimation(0.0f, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(10);
        rotateAnim.setFillAfter(true);
        arrow.startAnimation(rotateAnim);
    }

    // TODO: 10/22/19 -> Investigate the intended intent of this function
    public void goToDelegateProgress(){
        Intent intent = new Intent(this, DelegationProgress.class);
        finish();
        startActivity(intent);
    }

    public void goHome(){
        Intent intent = new Intent(this, MainScreen.class);
        finish();
        startActivity(intent);
    }
}
