package xyz.ummo.user.data.repo;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
//import xyz.ummo.user.data.dao.UserDao;
import xyz.ummo.user.data.dao.DelegatedServiceDao;
import xyz.ummo.user.data.dao.ProductDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.dao.ProfileDao;
import xyz.ummo.user.data.db.UserRoomDatabase;
//import xyz.ummo.user.data.entity.UserEntity;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.data.entity.ProductEntity;
//import xyz.ummo.user.data.entity.ServiceProviderEntity;
//import xyz.ummo.user.data.model.ServiceProvider;
//import xyz.ummo.user.delegate.CreateProduct;
import xyz.ummo.user.data.entity.ProfileEntity;
import xyz.ummo.user.delegate.GetProducts;

public class AppRepository {

    private ProfileDao profileDao;
    private DelegatedServiceDao delegatedServiceDao;
    private ProductDao productDao;
//    private ServiceProviderDao serviceProviderDao;

    private LiveData<DelegatedServiceEntity> delegatedServiceEntityLiveData;
    private LiveData<ProfileEntity> profileEntityLiveData;
    private LiveData<ProductEntity> productEntityLiveData;
//    private LiveData<ServiceProviderEntity> serviceProviderEntityLiveData;

    private static final String TAG = "AppRepo";

    public AppRepository(Application application){
        UserRoomDatabase userRoomDatabase = UserRoomDatabase.getUserDatabase(application);

        profileDao = userRoomDatabase.profileDao();
        profileEntityLiveData = profileDao.getProfileLiveData();
        Log.e("AppRepo", "User->"+ profileEntityLiveData);

        delegatedServiceDao = userRoomDatabase.delegatedServiceDao();
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedService();
        Log.e("AppRepo", "DelegatedService->"+ delegatedServiceEntityLiveData);

        productDao = userRoomDatabase.productDao();
        productEntityLiveData = productDao.getProductLiveData();
        Log.e("AppRepo", "Product->"+productEntityLiveData);

//        serviceProviderDao = agentRoomDatabase.serviceProviderDao();
//        serviceProviderEntityLiveData = serviceProviderDao.getServiceProviderLiveData();
//        Log.e("AppRepo", "serviceProvider->"+serviceProviderEntityLiveData);

    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceById(String delegatedServiceId){
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedServiceById(delegatedServiceId);
        Log.e(TAG, "getDelegatedServiceById: "+delegatedServiceEntityLiveData.getValue().getDelegatedProductId());
        return delegatedServiceEntityLiveData;
    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceByProductId(String delegatedProductId){
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedServiceByProductId(delegatedProductId);
//        Log.e(TAG, "getDelegatedServiceById: "+delegatedServiceEntityLiveData.getValue().getDelegatedProductId());
        return delegatedServiceEntityLiveData;
    }

    /*private AppRepository(final AgentRoomDatabase agentRoomDatabase){
        database = agentRoomDatabase;

    }*/

    /**
     * Repository API call for CRUDING Agent. We use the DAO to abstract the connection to the
     * AgentEntity. DAO calls implemented are C-InsertAgent; R-LiveData (exempt from AsyncOps); U-UpdateAgent && D-DeleteAgent.
     * Each are done asynchronously because RoomDB does not run on the main thread
     * */

    public void insertProfile(ProfileEntity profileEntity){
        new insertProfileAsyncTask(profileDao).execute(profileEntity);
    }

    public void deleteProfile(){
        new deleteProfileAsyncTask(profileDao).execute();
    }

    public LiveData<ProfileEntity> getProfileEntityLiveData(){
        Log.e("AppRepo", "Profile LiveData->"+ profileEntityLiveData);
        return profileEntityLiveData;
    }

    public void updateProfile(ProfileEntity profileEntity){
        new updateProfileAsyncTask(profileDao).execute(profileEntity);
    }

    private static class insertProfileAsyncTask extends AsyncTask<ProfileEntity, Void, Void>{
        private ProfileDao mProfileAsyncTaskDao;

        private insertProfileAsyncTask(ProfileDao profileDao){
            this.mProfileAsyncTaskDao = profileDao;
        }

        @Override
        protected Void doInBackground(final ProfileEntity... profileEntities) {
            mProfileAsyncTaskDao.insertProfile(profileEntities[0]);
            Log.e("AppRepo", "Inserting Profile->"+ Arrays.toString(profileEntities));
            return null;
        }
    }

    private static class deleteProfileAsyncTask extends AsyncTask<Void, Void, Void>{
        private ProfileDao mProfileAsyncTaskDao;

        deleteProfileAsyncTask(ProfileDao profileDao){
            this.mProfileAsyncTaskDao = profileDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mProfileAsyncTaskDao.deleteProfile();
            Log.e("AppRepo", "Deleting Profile!");
            return null;
        }
    }

    private static class updateProfileAsyncTask extends AsyncTask<ProfileEntity, Void, Void>{
        private ProfileDao mProfileAsyncTaskDao;

        updateProfileAsyncTask(ProfileDao profileDao){
            mProfileAsyncTaskDao = profileDao;
        }

        @Override
        protected Void doInBackground(final ProfileEntity... profileEntities) {
            mProfileAsyncTaskDao.updateProfile(profileEntities[0]);
            Log.e("AppRepo", "Updating profile->"+ Arrays.toString(profileEntities));
            return null;
        }
    }

    /**
     * Repository API call for CRUDING DelegatedService. We use the DAO to abstract the connection to the
     * DelegatedEntity. DAO calls implemented are C-InsertDelegatedService; R-LiveData (exempt from AsyncOps); U-UpdateDelegatedService && D-DeleteDelegatedService.
     * Each are done asynchronously because RoomDB does not run on the main thread
     **/

    public void insertDelegatedService(DelegatedServiceEntity delegatedServiceEntity){
        new insertDelegatedServiceAsyncTask(delegatedServiceDao).execute(delegatedServiceEntity);
    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceEntityLiveData(){
        Log.e("AppRepo", "User LiveData->"+ delegatedServiceEntityLiveData);
        return delegatedServiceEntityLiveData;
    }

    public void deleteAllDelegatedServices(){
        new deleteAllDelegatedServicesAsyncTask(delegatedServiceDao).execute();
    }

    public void updateDelegatedService(DelegatedServiceEntity delegatedServiceEntity){
        new updateDelegatedServiceAsyncTask(delegatedServiceDao).execute(delegatedServiceEntity);
    }

    private static class insertDelegatedServiceAsyncTask extends AsyncTask<DelegatedServiceEntity, Void, Void>{
        private DelegatedServiceDao mDelegatedServiceDao;

        private insertDelegatedServiceAsyncTask(DelegatedServiceDao delegatedServiceDao){
            this.mDelegatedServiceDao = delegatedServiceDao;
        }

        @Override
        protected Void doInBackground(final DelegatedServiceEntity... delegatedServiceEntities) {
            mDelegatedServiceDao.insertDelegatedService(delegatedServiceEntities[0]);
            Log.e("AppRepo", "Inserting Delegated Service->"+ Arrays.toString(delegatedServiceEntities));
            return null;
        }
    }

    private static class deleteAllDelegatedServicesAsyncTask extends AsyncTask<Void, Void, Void>{
        private DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

        deleteAllDelegatedServicesAsyncTask(DelegatedServiceDao delegatedServiceDao){
            this.mDelegatedServiceAsyncTaskDao = delegatedServiceDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDelegatedServiceAsyncTaskDao.deleteAllDelegatedServices();
            Log.e("AppRepo", "Deleting Delegated Service!");
            return null;
        }
    }

    private static class updateDelegatedServiceAsyncTask extends AsyncTask<DelegatedServiceEntity, Void, Void>{
        private DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

        updateDelegatedServiceAsyncTask(DelegatedServiceDao delegatedServiceDao){
            mDelegatedServiceAsyncTaskDao = delegatedServiceDao;
        }

        @Override
        protected Void doInBackground(final DelegatedServiceEntity... delegatedServiceEntities) {
            mDelegatedServiceAsyncTaskDao.updateDelegatedService(delegatedServiceEntities[0]);
            Log.e("AppRepo", "Updating Delegated Service->"+ Arrays.toString(delegatedServiceEntities));
            return null;
        }
    }

    /**
     * Repository API call for CRUDING Service Provider. We use the DAO to abstract the connection to the
     * ServiceProviderEntity. DAO calls implemented are C-InsertServiceProvider; R-LiveData (exempt from AsyncOps); U-UpdateServiceProvider && D-DeleteServiceProvider.
     * Each are done asynchronously because RoomDB does not run on the main thread
     **/

    /*public void insertServiceProvider(ServiceProviderEntity serviceProviderEntity){
        new insertServiceProviderAsyncTask(serviceProviderDao).execute(serviceProviderEntity);
    }*/

    /*public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveData(){
        Log.e("AppRepo", "Service Provider LiveData->"+ serviceProviderEntityLiveData);
        return serviceProviderEntityLiveData;
    }*/

    /*public void deleteServiceProvider(){
        new deleteServiceProviderAsyncTask(serviceProviderDao).execute();
    }*/

    /*public void updateServiceProvider(ServiceProviderEntity serviceProviderEntity){
        new updateServiceProviderAsyncTask(serviceProviderDao).execute(serviceProviderEntity);
    }*/

    /*private static class insertServiceProviderAsyncTask extends AsyncTask<ServiceProviderEntity, Void, Void>{
        private ServiceProviderDao mServiceProviderDao;

        private insertServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao){
            this.mServiceProviderDao = serviceProviderDao;
        }

        @Override
        protected Void doInBackground(final ServiceProviderEntity... serviceProviderEntities) {
            mServiceProviderDao.insertServiceProvider(serviceProviderEntities[0]);
            Log.e("AppRepo", "Inserting Service Provider->"+ Arrays.toString(serviceProviderEntities));
            return null;
        }
    }*/

    /*private static class deleteServiceProviderAsyncTask extends AsyncTask<Void, Void, Void>{
        private ServiceProviderDao mServiceProviderAsyncTaskDao;

        deleteServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao){
            this.mServiceProviderAsyncTaskDao = serviceProviderDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mServiceProviderAsyncTaskDao.deleteServiceProvider();
            Log.e("AppRepo", "Deleting Service Provider!");
            return null;
        }
    }*/

    /*private static class updateServiceProviderAsyncTask extends AsyncTask<ServiceProviderEntity, Void, Void>{
        private ServiceProviderDao mServiceProviderAsyncTaskDao;

        updateServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao){
            mServiceProviderAsyncTaskDao = serviceProviderDao;
        }

        @Override
        protected Void doInBackground(final ServiceProviderEntity... serviceProviderEntities) {
            mServiceProviderAsyncTaskDao.updateServiceProvider(serviceProviderEntities[0]);
            Log.e("AppRepo", "Updating Service Provider->"+ Arrays.toString(serviceProviderEntities));
            return null;
        }
    }*/
    /**
     * Repository API call for CRUDING Product. We use the DAO to abstract the connection to the
     * ProductEntity. DAO calls implemented are C-InsertProduct; R-LiveData (exempt from AsyncOps); U-UpdateProduct && D-DeleteProduct.
     * Each are done asynchronously because RoomDB does not run on the main thread
     **/

    public void insertProduct(ProductEntity productEntity){
        new insertProductAsyncTask(productDao).execute(productEntity);
    }

    public LiveData<ProductEntity> getProductEntityLiveData(){
        Log.e("AppRepo", "Product LiveData->"+productEntityLiveData);
        return productEntityLiveData;
    }

    public LiveData<ProductEntity> getProductEntityLiveDataById(String productId){
        productEntityLiveData = productDao.getProductEntityLiveDataById(productId);
        return productEntityLiveData;
    }

    public LiveData<ProductEntity> getDelegatedProduct(Boolean isDelegated){
        productEntityLiveData = productDao.getDelegatedProduct(isDelegated);
        return productEntityLiveData;
    }

    public void deleteAllProducts(){
        new deleteAllProductsAsyncTask(productDao).execute();
    }

    public void updateProduct(ProductEntity productEntity){
        new updateProductAsyncTask(productDao).execute(productEntity);
    }

    private static class insertProductAsyncTask extends AsyncTask<ProductEntity, Void, Void>{
        private ProductDao mProductDao;

        private insertProductAsyncTask(ProductDao productDao){
            this.mProductDao = productDao;
        }

        @Override
        protected Void doInBackground(final ProductEntity... productEntities) {
            mProductDao.insertProduct(productEntities[0]);
            Log.e(TAG, "Inserting Product->"+ productEntities[0].getProductName());

            /*new CreateProduct(
                    productEntities[0].getProductName()
                    ,productEntities[0].getProductDescription(),
                    productEntities[0].getProductSteps(),
                    false,
                    productEntities[0].getProductCost(),
                    productEntities[0].getProductDocuments(),
                    productEntities[0].getProductProvider(),
                    productEntities[0].getProductDuration()){

                @Override
                public void done(@NotNull byte[] data, @NotNull Number code) {
                    Log.e(TAG, "Inserting Product data->"+ Arrays.toString(data));
                    Log.e(TAG, "Inserting Product code->"+ code);
                }
            };*/
            return null;
        }
    }

    private static class deleteAllProductsAsyncTask extends AsyncTask<Void, Void, Void>{
        private ProductDao mProductAsyncTaskDao;

        deleteAllProductsAsyncTask(ProductDao productDao){
            this.mProductAsyncTaskDao = productDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mProductAsyncTaskDao.deleteAllProducts();
            Log.e("AppRepo", "Deleting Product!");
            return null;
        }
    }

    private static class updateProductAsyncTask extends AsyncTask<ProductEntity, Void, Void>{
        private ProductDao mProductAsyncTaskDao;

        updateProductAsyncTask(ProductDao productDao){
            mProductAsyncTaskDao = productDao;
        }

        @Override
        protected Void doInBackground(final ProductEntity... productEntities) {
            mProductAsyncTaskDao.updateProduct(productEntities[0]);
            Log.e("AppRepo", "Updating Product->"+ Arrays.toString(productEntities));
            return null;
        }
    }

    /*GetProducts getProducts = new GetProducts() {
        @Override
        public void done(@NotNull byte[] data, @NotNull Number code) {
            if (code.equals(200)){
                Log.e(TAG+" GetProducts", "Logging Products->"+new String(data));
            } else {
                Log.e(TAG+" GetProducts", "WTF happened->"+code);
//                logWithStaticAPI();
            }
        }
    };*/

    private void unsafeMethod(){
        throw new UnsupportedOperationException("This needs attention!");
    }

    /*private void logWithStaticAPI(){

        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("Agent made an action")
                        .build());

        Sentry.getContext().setUser(
                new UserBuilder()
                        .setUsername(agentDao.getAgentLiveData().getValue().getName())
                        .setEmail(agentDao.getAgentLiveData().getValue().getEmail())
                        .build());

        Sentry.capture("Mic check...1,2!");

        try {
            unsafeMethod();
            Log.e(TAG, "logWithStaticAPI, unsafeMethod");
        } catch (Exception e){
            Sentry.capture(e);
        }
    }*/
}
