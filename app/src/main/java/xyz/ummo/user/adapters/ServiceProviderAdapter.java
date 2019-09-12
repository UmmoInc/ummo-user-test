package xyz.ummo.user.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import xyz.ummo.user.AddCard;
import xyz.ummo.user.Product;
import xyz.ummo.user.R;
import xyz.ummo.user.ServiceProvider;

public class ServiceProviderAdapter extends RecyclerView.Adapter<ServiceProviderAdapter.MyViewHolder> {

    private List<ServiceProvider> serviceProviderList;
    ArrayList<Product> providers= new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView serviceProviderName, moreText;
        public RelativeLayout serviceProviderBackground;
        ProductAdapter productAdapter;
        RecyclerView productsRecyclerView;

        public MyViewHolder(View view) {
            super(view);
            Context context = itemView.getContext();
            serviceProviderName = (TextView) view.findViewById(R.id.service_provider_name);
            moreText = (TextView) view.findViewById(R.id.more);
            serviceProviderBackground= (RelativeLayout) view.findViewById(R.id.service_provider_background);
            productsRecyclerView= view.findViewById(R.id.products_rv);

            addProduct();

            productAdapter = new ProductAdapter(providers);

            //Set productsRecyclerView
            productsRecyclerView.setAdapter(productAdapter);
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            productsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    public ServiceProviderAdapter(List<ServiceProvider> serviceProviderList){
        this.serviceProviderList= serviceProviderList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_provider_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ServiceProvider serviceProvider= serviceProviderList.get(position);

        holder.serviceProviderName.setText(serviceProvider.serviceProviderName);
    }

    @Override
    public int getItemCount() {
        return serviceProviderList.size();

    }

    public  void addProduct(){
        Product product = new Product("Home Affairs", "Mbabane",
                "+204 5466", "www.homeaffairs.org.sz");
        providers.add(product);

        product = new Product("Mbabane Hospital", "Mbabane",
                "+204 0092", "www.mbabanehospital.org.sz");
        providers.add(product);

        product = new Product("Manzini Hospital", "Manzini",
                "+204 0488", "www.manzinihospital.org.sz");
        providers.add(product);

        product = new Product("Central Bank of Eswatini", "Mbabane",
                "+204 9900", "www.centralbankeswatini.org.sz");
        providers.add(product);

        product = new Product("Mbabane Library", "Mbabane",
                "+204 9900", "www.mbabanenationallibrary.org.sz");
        providers.add(product);

    }

}
