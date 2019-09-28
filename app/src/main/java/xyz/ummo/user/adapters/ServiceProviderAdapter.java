package xyz.ummo.user.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import xyz.ummo.user.AddCard;
import xyz.ummo.user.DetailedService;
import xyz.ummo.user.Product;
import xyz.ummo.user.R;
import xyz.ummo.user.ServiceProvider;
import xyz.ummo.user.delegate.GetProducts;
import xyz.ummo.user.delegate.PublicServiceData;
import xyz.ummo.user.delegate.get;

public class ServiceProviderAdapter extends RecyclerView.Adapter<ServiceProviderAdapter.MyViewHolder> {

    private List<PublicServiceData> serviceProviderList;


    Activity context;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView serviceProviderName, moreText;
        public RelativeLayout serviceProviderBackground;
        ProductAdapter productAdapter;
        RecyclerView productsRecyclerView;
        PublicServiceData publicServiceData;
        ArrayList<Product> providers = new ArrayList<>();

        public MyViewHolder(View view) {
            super(view);
            Context context = itemView.getContext();
            serviceProviderName = (TextView) view.findViewById(R.id.service_provider_name);
            moreText = (TextView) view.findViewById(R.id.more);
            serviceProviderBackground = (RelativeLayout) view.findViewById(R.id.service_provider_background);
            productsRecyclerView = view.findViewById(R.id.products_rv);


            productAdapter = new ProductAdapter(providers);

            //Set productsRecyclerView
            productsRecyclerView.setAdapter(productAdapter);
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            productsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    public ServiceProviderAdapter(List<PublicServiceData> serviceProviderList, Activity activity) {
        context = activity;
        this.serviceProviderList = serviceProviderList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_provider_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PublicServiceData serviceProvider = serviceProviderList.get(position);
        holder.publicServiceData = serviceProvider;
        holder.serviceProviderName.setText(serviceProvider.getServiceName());
        addProduct(holder);
    }

    @Override
    public int getItemCount() {
        return serviceProviderList.size();

    }

    public void addProduct(MyViewHolder holder) {

        new GetProducts(context, holder.publicServiceData.getServiceCode()) {
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {
                try {
                    JSONArray arr = new JSONArray(new String(data));
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Log.e("ServiceProviderAdapter", obj.toString());
                        holder.providers.add(
                                new Product(obj.getString("product_name"),
                                        holder.publicServiceData.getTown(),
                                        holder.publicServiceData.getProvince(),
                                        obj.getString("_id"),
                                        get.INSTANCE.get(obj, "product_description", "description").toString(),
                                        get.INSTANCE.get(obj, "procurement_process", "procurement_process").toString(),
                                        get.INSTANCE.get(obj, "duration", "duration").toString(),
                                        get.INSTANCE.get(obj, "requirements.documents", "docs").toString()
                                ));
                    }

                    holder.productAdapter.notifyDataSetChanged();

                } catch (JSONException jse) {
                    Log.e("ServiceProviderAdapter", jse.toString());
                }
            }
        };
    }

}
