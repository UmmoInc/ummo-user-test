package xyz.ummo.user.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import xyz.ummo.user.Product;
import xyz.ummo.user.R;
//import xyz.ummo.user.data.entity.ServiceProviderEntityOld;
import xyz.ummo.user.delegate.GetProducts;
import xyz.ummo.user.models.PublicServiceData;
import xyz.ummo.user.delegate.Get;
//import xyz.ummo.user.utilities.ServiceProviderViewModel;

public class ServiceProviderAdapter extends RecyclerView.Adapter<ServiceProviderAdapter.MyViewHolder> {

    private List<PublicServiceData> serviceProviderList;
//    private ServiceProviderEntityOld serviceProviderEntityOld = new ServiceProviderEntityOld();
//    private ServiceProviderViewModel serviceProviderViewModel;

    Activity context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView serviceProviderName, moreText;
        RelativeLayout serviceProviderBackground;
        ProductAdapter productAdapter;
        RecyclerView productsRecyclerView;
        PublicServiceData publicServiceData;
        ArrayList<Product> providers = new ArrayList<>();

        public MyViewHolder(View view) {
            super(view);
            Context context = itemView.getContext();
            serviceProviderName = view.findViewById(R.id.service_provider_name);
            moreText = view.findViewById(R.id.more);
            serviceProviderBackground = view.findViewById(R.id.service_provider_background);
            productsRecyclerView = view.findViewById(R.id.products_rv);

            productAdapter = new ProductAdapter(context, providers);

            //Set productsRecyclerView
            productsRecyclerView.setAdapter(productAdapter);
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            productsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    public ServiceProviderAdapter(List<PublicServiceData> serviceProviderList, Activity activity) {
        context = activity;
        this.serviceProviderList = serviceProviderList;

//        serviceProviderViewModel = ViewModelProviders.of((FragmentActivity) context).get(ServiceProviderViewModel.class);
    }

    @NonNull
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

        /*
        * Inserting `ServiceProvider` into RoomDB
        * */
//        serviceProviderEntityOld.setServiceProviderId(serviceProviderList.get(position).getServiceCode());
//        serviceProviderEntityOld.setServiceProviderName(serviceProviderList.get(position).getServiceName());
//        serviceProviderEntityOld.setServiceProviderProvince(serviceProviderList.get(position).getProvince());
//        serviceProviderEntityOld.setServiceProviderMunicipality(serviceProviderList.get(position).getMunicipality());
//        serviceProviderEntityOld.setServiceProviderTown(serviceProviderList.get(position).getTown());
//        serviceProviderViewModel.insertServiceProvider(serviceProviderEntityOld);

        Timber.e("onBindViewHolder: SERVICE-PROVIDER-LIST->%s", serviceProviderList.get(position).getServiceName());
        addProduct(holder);
    }

    @Override
    public int getItemCount() {
        return serviceProviderList.size();
    }

    public void addProduct(MyViewHolder holder) {

        new GetProducts(context, holder.publicServiceData.getServiceCode()) {
            @SuppressLint("TimberArgCount")
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {
                try {
                    JSONArray arr = new JSONArray(new String(data));

                    Timber.e("PRODUCT JSON-ARRAY ->%s", arr);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Timber.e("PRODUCT JSON-OBJECT ->%s", obj);
                        holder.providers.add(
                                new Product(obj.getString("product_name"),
                                        holder.publicServiceData.getTown(),
                                        holder.publicServiceData.getProvince(),
                                        obj.getString("_id"),
                                        Get.INSTANCE.get(obj, "product_description", "description").toString(),
                                        Get.INSTANCE.get(obj, "procurement_process", "procurement_process").toString(),
                                        Get.INSTANCE.get(obj, "duration", "duration").toString(),
                                        Get.INSTANCE.get(obj, "requirements.documents", "docs").toString(),
                                        Get.INSTANCE.get(obj, "requirements.procurement_cost", "cost").toString()
//                                        get.INSTANCE.get(obj, "")
                                        // TODO: 10/16/19 -> Insert `procurement_cost`
                                        // TODO: 10/16/19 -> Use ArrayLists where needed
                                ));
//                        Log.e(TAG, "done: DOCS->"+ get.INSTANCE.get(obj, "requirements.documents", "docs").toString());
                    }

                    holder.productAdapter.notifyDataSetChanged();

                } catch (JSONException jse) {
                    Timber.e("ServiceProviderAdapter", jse.toString());
                }
            }
        };
    }
}
