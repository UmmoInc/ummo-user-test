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

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.delegate.get;
import xyz.ummo.user.ui.detailedService.DetailedProduct;
import xyz.ummo.user.Product;
import xyz.ummo.user.R;
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {

    private List<Product> productList;
    private static final String TAG = "ProductAdapter";
    private DetailedProductViewModel detailedProductViewModel;
    private ProductEntity productEntity = new ProductEntity();
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView providerName, providerLocation, providerContact;

        RelativeLayout bg;

        MyViewHolder(View view, RelativeLayout bg) {
            super(view);
            this.bg = bg;
            providerName = view.findViewById(R.id.product_name);
            providerLocation = view.findViewById(R.id.product_location);
            providerContact = view.findViewById(R.id.product_contact);
        }
    }

    ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;

        detailedProductViewModel = ViewModelProviders.
                of((FragmentActivity) context).get(DetailedProductViewModel.class);
    }

    @NonNull
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
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(context,
                        context.getResources().getString(R.string.mixpanelToken));
        Product product = productList.get(position);

        holder.providerName.setText(product.getProviderName());
        holder.providerLocation.setText(product.getLocation());
        holder.providerContact.setText(product.getContact());
        holder.bg.setOnClickListener(v -> {

            JSONObject productObject = new JSONObject();
            try {
                productObject.put("productId", product.getId());
                productObject.put("productName", product.getProviderName());
                //Mixpanel event tracker for `productSelected`
                if (mixpanel != null) {
                    mixpanel.track("productSelected", productObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(v.getContext(), DetailedProduct.class);
            Product p = productList.get(position);

            intent.putExtra("product_id",p.getId());
            v.getContext().startActivity(intent);
            Timber.e("onBindViewHolder: onClick->%s", intent.getExtras().toString());

            String stepsWithBlocks = p.getSteps();
            String unpackedSteps = unpackBlockedString(stepsWithBlocks);

            String docsWithBlocks = p.getDocs();
            String unpackedDocs = unpackBlockedString(docsWithBlocks);

            ArrayList<String> stepsArrayList = new ArrayList<>(Arrays.asList(unpackedSteps.split(",")));
            ArrayList<String> docsArrayList = new ArrayList<>(Arrays.asList(unpackedDocs.split(",")));

            productEntity.setProductId(p.getId());
            productEntity.setProductName(p.getProviderName());
            productEntity.setProductDescription(p.getDescription());
            productEntity.setProductCost(p.getCost());
            productEntity.setProductSteps(stepsArrayList);
            productEntity.setProductDocuments(docsArrayList);
            productEntity.setProductDuration(p.getDuration());
            productEntity.setIsDelegated(false);
            detailedProductViewModel.insertProduct(productEntity);
        });
    }

    private String unpackBlockedString(String blockedString){
        String blockedString1 = blockedString.replace("[","");
        String blockedString2 = blockedString1.replace("]","");
        return blockedString2.replace("\"\"", ""); // TODO: 10/17/19 finish off, get it right!
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}
