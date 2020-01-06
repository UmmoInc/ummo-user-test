package xyz.ummo.user.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import xyz.ummo.user.data.model.ProductModel;

@Entity(tableName = "product")
public class ProductEntity implements ProductModel {
    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "product_id")
    private String productId;

    @NonNull
    @ColumnInfo(name = "product_name")
    private String productName = "Default Name";

    @NonNull
    @ColumnInfo(name = "product_description")
    private String productDescription = "Default Description";

    @NonNull
    @ColumnInfo(name = "product_provider")
    private String productProvider = "Default Provider";

    @NonNull
    @ColumnInfo(name = "product_documents")
    private ArrayList<String> productDocuments = new ArrayList<>();

    @NonNull
    @ColumnInfo(name = "product_cost")
    private String productCost = "Default Cost";

    @NonNull
    @ColumnInfo(name = "product_duration")
    private String productDuration = "Default Duration";

    @NonNull
    @ColumnInfo(name = "procurement_steps")
    private ArrayList<String> productSteps = new ArrayList<>();

    @NonNull
    @ColumnInfo(name = "is_delegated")
    private Boolean isDelegated = false;

    public ProductEntity(@NonNull String _productId,
                         @NonNull String _productName,
                         @NonNull String _productDescription,
                         @NonNull String _productProvider,
                         @NonNull ArrayList<String> _productDocuments,
                         @NonNull String _productCost,
                         @NonNull String _productDuration,
                         @NonNull ArrayList<String> _productSteps,
                         @NonNull Boolean _isDelegated){
        this.productId = _productId;
        this.productName = _productName;
        this.productDescription = _productDescription;
        this.productProvider = _productProvider;
        this.productDocuments = _productDocuments;
        this.productCost = _productCost;
        this.productDuration = _productDuration;
        this.productSteps = _productSteps;
        this.isDelegated = _isDelegated;
    }

    public ProductEntity(){}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    @NonNull
    public String getProductName() {
        return productName;
    }

    public void setProductName(@NonNull String productName) {
        this.productName = productName;
    }

    @Override
    @NonNull
    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(@NonNull String productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    @NonNull
    public String getProductProvider() {
        return productProvider;
    }

    public void setProductProvider(@NonNull String productProvider) {
        this.productProvider = productProvider;
    }

    @Override
    @NonNull
    public ArrayList<String> getProductDocuments() {
        return productDocuments;
    }

    public void setProductDocuments(@NonNull ArrayList<String> productDocuments) {
        this.productDocuments = productDocuments;
    }

    public void setProductSteps(@NonNull ArrayList<String> productSteps) {
        this.productSteps = productSteps;
    }

    @Override
    @NonNull
    public ArrayList<String> getProductSteps() {
        return productSteps;
    }

    @Override
    @NonNull
    public String getProductCost() {
        return productCost;
    }

    public void setProductCost(@NonNull String productCost) {
        this.productCost = productCost;
    }

    @Override
    @NonNull
    public String getProductDuration() {
        return productDuration;
    }

    public void setProductDuration(@NonNull String productDuration) {
        this.productDuration = productDuration;
    }

    @NonNull
    public Boolean getIsDelegated() {
        return isDelegated;
    }

    public void setIsDelegated(@NonNull Boolean delegated) {
        isDelegated = delegated;
    }
}
