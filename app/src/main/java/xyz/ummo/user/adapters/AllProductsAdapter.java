package xyz.ummo.user.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xyz.ummo.user.Product;
import xyz.ummo.user.R;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.ui.detailedService.DetailedProduct;
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel;

public class AllProductsAdapter extends RecyclerView.Adapter<AllProductsAdapter.MyViewHolder> {

    private List<Product> productList;
    private static final String TAG = "ProductAdapter";
    private DetailedProductViewModel detailedProductViewModel;
    private ProductEntity productEntity = new ProductEntity();
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView providerName, providerLocation, serviceProvider;

        RelativeLayout bg;

        MyViewHolder(View view, RelativeLayout bg) {
            super(view);
            this.bg = bg;
            providerName = view.findViewById(R.id.product_name);
            providerLocation = view.findViewById(R.id.product_location);
            serviceProvider = view.findViewById(R.id.service_provider);
        }
    }

    public AllProductsAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;

        detailedProductViewModel = ViewModelProviders.
                of((FragmentActivity) context).get(DetailedProductViewModel.class);
    }

    @NonNull
    @Override
    public AllProductsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_products_list, parent, false);

        Context context = itemView.getContext();

        RelativeLayout productBackground = itemView.findViewById(R.id.product);

        return new AllProductsAdapter.MyViewHolder(itemView, productBackground);
    }

    @Override
    public void onBindViewHolder(AllProductsAdapter.MyViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.providerName.setText(product.getProviderName());
        holder.providerLocation.setText(product.getLocation());

    }
    @Override
    public int getItemCount() {
        return productList.size();
    }
}
