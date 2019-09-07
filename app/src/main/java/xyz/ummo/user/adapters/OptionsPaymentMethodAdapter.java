package xyz.ummo.user.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import xyz.ummo.user.AddCard;
import xyz.ummo.user.PaymentMethod;
import xyz.ummo.user.R;

public class OptionsPaymentMethodAdapter extends BaseAdapter {

    private Context mContext;
    public ArrayList<PaymentMethod> paymentMethods;

    public OptionsPaymentMethodAdapter(Context context, ArrayList<PaymentMethod> paymentMethods) {

        //initialise context, arraylist and progressbar
        this.mContext = context;
        this.paymentMethods = paymentMethods;


    }

    private class ViewHolder {
        TextView paymentMethod;
        RelativeLayout paymentMethodWrapper;
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
            holder.paymentMethodWrapper = convertView.findViewById(R.id.payment_method_wrapper);

            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.paymentMethod.setText(paymentMethod.getMethodName());

        holder.paymentMethodWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setPaymentMethod(paymentMethod.getMethodName());

                goToPaymentMethodDetails();
            }
        });

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

    public void setPaymentMethod(String paymentMethod){

        new PaymentMethod(paymentMethod);


    }

    public void goToPaymentMethodDetails(){

        Intent intent = new Intent(mContext, AddCard.class);
        ((Activity)mContext).finish();
        mContext.startActivity(intent);

    }


}





