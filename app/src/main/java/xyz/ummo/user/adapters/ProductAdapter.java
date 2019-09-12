package xyz.ummo.user.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.ummo.user.DetailedService;
import xyz.ummo.user.Product;
import xyz.ummo.user.R;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder>{

    private List<Product> productList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView providerName, providerLocation, providerContact;

        public MyViewHolder(View view) {
            super(view);
            providerName= (TextView) view.findViewById(R.id.product_name);
            providerLocation = (TextView) view.findViewById(R.id.product_location);
            providerContact = (TextView) view.findViewById(R.id.product_contact);

        }
    }

    public ProductAdapter(List<Product> productList){
        this.productList= productList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_list, parent, false);


        Context context = itemView.getContext();


        RelativeLayout productBackground = itemView.findViewById(R.id.product_background);
        productBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, DetailedService.class);
                context.startActivity(intent);
            }
        });



        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.providerName.setText(product.getProviderName());
        holder.providerLocation.setText(product.getLocation());
        holder.providerContact.setText(product.getContact());
    }

    @Override
    public int getItemCount() {
        return productList.size();

    }

}
