package xyz.ummo.user.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.List;

import xyz.ummo.user.Agent;
import xyz.ummo.user.AgentRequest;
import xyz.ummo.user.DetailedService;
import xyz.ummo.user.Product;
import xyz.ummo.user.R;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {

    private List<Product> productList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView providerName, providerLocation, providerContact;

        public RelativeLayout bg;

        public MyViewHolder(View view, RelativeLayout bg) {
            super(view);
            this.bg = bg;
            providerName = (TextView) view.findViewById(R.id.product_name);
            providerLocation = (TextView) view.findViewById(R.id.product_location);
            providerContact = (TextView) view.findViewById(R.id.product_contact);

        }
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_list, parent, false);


        Context context = itemView.getContext();


        RelativeLayout productBackground = itemView.findViewById(R.id.product_background);


        return new MyViewHolder(itemView, productBackground);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.providerName.setText(product.getProviderName());
        holder.providerLocation.setText(product.getLocation());
        holder.providerContact.setText(product.getContact());
        holder.bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailedService.class);
                Product p = productList.get(position);

                intent.putExtra("serviceName", p.getProviderName());
                intent.putExtra("description", p.getDescription());
                intent.putExtra("cost", "cost");
                intent.putExtra("steps", p.getSteps());
                intent.putExtra("duration", p.getDuration());
                intent.putExtra("docs", p.getDocs());
                intent.putExtra("id",p.getId());
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();

    }

}
