package xyz.ummo.user;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import xyz.ummo.user.adapters.CustomPaymentMethodAdapter;
import xyz.ummo.user.adapters.OptionsPaymentMethodAdapter;

public class AddPaymentMethod extends AppCompatActivity {

    private ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();
    private ListView paymentMethodsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment_method);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Add Payment Method");

        //load payment methods
        loadPaymentMethods();

        //set the payment method list with the custom payment method adapter
        paymentMethodsList = findViewById(R.id.payment_methods_list);
        OptionsPaymentMethodAdapter optionsPaymentMethodAdapter = new OptionsPaymentMethodAdapter(this, paymentMethods);
        paymentMethodsList.setAdapter(optionsPaymentMethodAdapter);

    }

    public void loadPaymentMethods(){

        PaymentMethod paymentMethod = new PaymentMethod("Credit or Debit Card");
        paymentMethods.add(paymentMethod);

    }
}
