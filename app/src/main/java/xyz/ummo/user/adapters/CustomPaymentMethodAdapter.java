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
import xyz.ummo.user.PaymentMethod;
import xyz.ummo.user.Progress;
import xyz.ummo.user.R;

public class CustomPaymentMethodAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<PaymentMethod> paymentMethods;

    public CustomPaymentMethodAdapter(Context context, ArrayList<PaymentMethod> paymentMethods) {

        //initialise context, arraylist and progressbar
        this.mContext = context;
        this.paymentMethods = paymentMethods;


    }

    private class ViewHolder {
        TextView paymentMethod;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent){
        // TODO Auto-generated method stub

        ViewHolder holder = null;
        Log.v("Convert View", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            convertView = inflater.inflate(R.layout.payment_methods_list, null);

            holder = new ViewHolder();
            holder.paymentMethod = (TextView) convertView.findViewById(R.id.payment_method_name);

            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.paymentMethod.setText(paymentMethod.getMethodName());

        return convertView;
    }


    @Override
    public int getCount() {
        return paymentMethods.size();
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





