package xyz.ummo.user.data.repo;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.lang.ref.WeakReference;

import com.github.nkzawa.emitter.Emitter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

//import xyz.ummo.user.data.dao.UserDao;
import timber.log.Timber;
import xyz.ummo.user.data.dao.DelegatedServiceDao;
import xyz.ummo.user.data.dao.ProductDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.dao.ProfileDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.db.UserRoomDatabase;
//import xyz.ummo.user.data.entity.UserEntity;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.entity.ServiceProviderEntity;
import xyz.ummo.user.data.entity.ProfileEntity;
import xyz.ummo.user.delegate.Service;
import xyz.ummo.user.delegate.SocketIO;

public class AppRepository {

    private ProfileDao profileDao;
    private DelegatedServiceDao delegatedServiceDao;
    private ProductDao productDao;
    private ServiceProviderDao serviceProviderDao;

    private LiveData<DelegatedServiceEntity> delegatedServiceEntityLiveData;
    private LiveData<ProfileEntity> profileEntityLiveData;
    private LiveData<ProductEntity> productEntityLiveData;
    private LiveData<ServiceProviderEntity> serviceProviderEntityLiveData;
//    private List<ServiceProviderEntityOld> serviceProviders = new ArrayList<>();

    public AppRepository(Application application) {
        UserRoomDatabase userRoomDatabase = UserRoomDatabase.getUserDatabase(application);

        profileDao = userRoomDatabase.profileDao();
        profileEntityLiveData = profileDao.getProfileLiveData();
//        Log.e("AppRepo", "User->"+ profileEntityLiveData);

        delegatedServiceDao = userRoomDatabase.delegatedServiceDao();
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedService();
//        Log.e("AppRepo", "DelegatedServiceModel->"+ delegatedServiceEntityLiveData);

        productDao = userRoomDatabase.productDao();
        productEntityLiveData = productDao.getProductLiveData();
//        Timber.e("Product->%s", productEntityLiveData);

        serviceProviderDao = userRoomDatabase.serviceProviderDao();
        serviceProviderEntityLiveData = serviceProviderDao.getServiceProviderLiveData();
        Timber.e("Service Provider ->%s", serviceProviderEntityLiveData);

        /*new Service(application) {
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {

//                Timber.e("done: %s", new String(data));
                if (code.intValue() != 200) {
                    return;
                }
                try {
                    JSONArray myServices = new JSONArray(new String(data));
//                    Timber.e("done: Services%s", myServices.toString());
                    //deleteAllDelegatedServices();
                    for (int i = 0; i < myServices.length(); i++) {
                        DelegatedServiceEntity entity = new DelegatedServiceEntity();
                        JSONObject s = myServices.getJSONObject(i);
                        JSONObject p = s.getJSONObject("product");
                        entity.setDelegatedProductId(s.getJSONObject("product").getString("_id"));
                        entity.setServiceAgentId(s.getJSONObject("agent").getString("_id"));
                        entity.setServiceId(s.getString("_id"));
                        entity.setServiceProgress(listFromJSONArray(s.getJSONArray("progress")));
                        insertDelegatedService(entity);

                        ProductEntity productEntity = new ProductEntity();
                        productEntity.setIsDelegated(true);
                        productEntity.setProductCost(p.getJSONObject("requirements").getString("procurement_cost"));
                        productEntity.setProductDescription("");
                        if (p.has("product_description")) {
                            productEntity.setProductDescription(p.getString("product_description"));
                        }
                        */

        /*productEntity.setProductDocuments(listFromJSONArray(p.getJSONObject("requirements").getJSONArray("documents")));
                        productEntity.setProductDuration(p.getString("duration"));*/

        /*
                        productEntity.setProductId(p.getString("_id"));
                        productEntity.setProductName(p.getString("product_name"));
                        productEntity.setProductProvider(p.getString("public_service"));
                        productEntity.setProductSteps(listFromJSONArray(p.getJSONArray("procurement_process")));
                        insertProduct(productEntity);

                    }

                    SocketIO.INSTANCE.getMSocket().on("updated-service", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            try {
                                JSONObject s = new JSONObject(args[0].toString());
                                DelegatedServiceEntity entity = new DelegatedServiceEntity();
                                entity.setDelegatedProductId(s.getString("product"));
                                entity.setServiceAgentId(s.getString("agent"));
                                entity.setServiceId(s.getString("_id"));
                                entity.setServiceProgress(listFromJSONArray(s.getJSONArray("progress")));
                                insertDelegatedService(entity);
                            } catch (JSONException e) {
//                                Timber.e("call: %s", e.toString());
                            }

                            //updateDelegatedService();
                        }
                    });
                } catch (JSONException e) {
//                    Timber.e("done: %s", e.toString());
                }
            }
        };*/

        /*serviceProviderDao = userRoomDatabase.serviceProviderDao();
        serviceProviderEntityLiveData = serviceProviderDao.getServiceProviderLiveData();*/
//        serviceProviders = serviceProviderDao.getServiceProviders();
//        Log.e("AppRepo", "ServiceProviders [COUNT]->"+serviceProviders.size());
    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceById(String delegatedServiceId) {
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedServiceById(delegatedServiceId);
//        Log.e(TAG, "getDelegatedServiceById: "+delegatedServiceEntityLiveData.getValue().getDelegatedProductId());
        return delegatedServiceEntityLiveData;
    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceByProductId(String delegatedProductId) {
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedServiceByProductId(delegatedProductId);
//        Log.e(TAG, "getDelegatedServiceById: "+delegatedServiceEntityLiveData.getValue().getDelegatedProductId());
        return delegatedServiceEntityLiveData;
    }

    /**
     * Repository API call for CRUDING Agent. We use the DAO to abstract the connection to the
     * AgentEntity. DAO calls implemented are C-InsertAgent; R-LiveData (exempt from AsyncOps); U-UpdateAgent && D-DeleteAgent.
     * Each are done asynchronously because RoomDB does not run on the main thread
     */

    public void insertProfile(ProfileEntity profileEntity) {
        new insertProfileAsyncTask(profileDao).execute(profileEntity);
    }

    public void deleteProfile() {
        new deleteProfileAsyncTask(profileDao).execute();
    }

    public LiveData<ProfileEntity> getProfileEntityLiveData() {
//        Timber.e("ProfileModel LiveData->%s", profileEntityLiveData);
        return profileEntityLiveData;
    }

    public void updateProfile(ProfileEntity profileEntity) {
        new updateProfileAsyncTask(profileDao).execute(profileEntity);
    }

    private static class insertProfileAsyncTask extends AsyncTask<ProfileEntity, Void, Void> {
        private ProfileDao mProfileAsyncTaskDao;

        private insertProfileAsyncTask(ProfileDao profileDao) {
            this.mProfileAsyncTaskDao = profileDao;
        }

        @Override
        protected Void doInBackground(final ProfileEntity... profileEntities) {
            mProfileAsyncTaskDao.insertProfile(profileEntities[0]);
//            Timber.e("Inserting ProfileModel->%s", Arrays.toString(profileEntities));
            return null;
        }
    }

    private static class deleteProfileAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProfileDao mProfileAsyncTaskDao;

        deleteProfileAsyncTask(ProfileDao profileDao) {
            this.mProfileAsyncTaskDao = profileDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mProfileAsyncTaskDao.deleteProfile();
//            Timber.e("Deleting ProfileModel!");
            return null;
        }
    }

    private static class updateProfileAsyncTask extends AsyncTask<ProfileEntity, Void, Void> {
        private ProfileDao mProfileAsyncTaskDao;

        updateProfileAsyncTask(ProfileDao profileDao) {
            mProfileAsyncTaskDao = profileDao;
        }

        @Override
        protected Void doInBackground(final ProfileEntity... profileEntities) {
            mProfileAsyncTaskDao.updateProfile(profileEntities[0]);
//            Timber.e("Updating profile->%s", Arrays.toString(profileEntities));
            return null;
        }
    }

    /**
     * Repository API call for CRUDING DelegatedServiceModel. We use the DAO to abstract the connection to the
     * DelegatedEntity. DAO calls implemented are C-InsertDelegatedService; R-LiveData (exempt from AsyncOps); U-UpdateDelegatedService && D-DeleteDelegatedService.
     * Each are done asynchronously because RoomDB does not run on the main thread
     **/

    public void insertDelegatedService(DelegatedServiceEntity delegatedServiceEntity) {
//        Timber.e("insertDelegatedService: INSERTING DELEGATED-SERVICE->%s", delegatedServiceEntity);
        new insertDelegatedServiceAsyncTask(delegatedServiceDao).execute(delegatedServiceEntity);
    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceEntityLiveData() {
        try {
//            Log.e(TAG, "getDelegatedServiceEntity: DELEGATED-SERVICE->"+delegatedServiceDao.getDelegatedService().getValue().getServiceId());
            return new getDelegatedServiceEntityAsyncTask(delegatedServiceDao).execute().get();
        } catch (ExecutionException | InterruptedException exe) {
//            Timber.e("getDelegatedServiceEntity: %s", exe.toString());
            return null;
        }
    }

    public void deleteAllDelegatedServices() {
        new deleteAllDelegatedServicesAsyncTask(delegatedServiceDao).execute();
    }

    public void updateDelegatedService(DelegatedServiceEntity delegatedServiceEntity) {
        new updateDelegatedServiceAsyncTask(delegatedServiceDao).execute(delegatedServiceEntity);
    }

    private static class getDelegatedServiceEntityAsyncTask extends AsyncTask<Void, Void, LiveData<DelegatedServiceEntity>> {
        private DelegatedServiceDao mDelegatedServiceDao;

        private getDelegatedServiceEntityAsyncTask(DelegatedServiceDao delegatedServiceDao) {
            this.mDelegatedServiceDao = delegatedServiceDao;
        }

        @Override
        protected LiveData<DelegatedServiceEntity> doInBackground(Void... voids) {
            mDelegatedServiceDao.getDelegatedService();
//            Timber.e("Getting Delegated Service->");
//            return delegatedServiceEntities[0];
            return mDelegatedServiceDao.getDelegatedService();
        }
    }

    private static class insertDelegatedServiceAsyncTask extends AsyncTask<DelegatedServiceEntity, Void, Void> {
        private DelegatedServiceDao mDelegatedServiceDao;

        private insertDelegatedServiceAsyncTask(DelegatedServiceDao delegatedServiceDao) {
            this.mDelegatedServiceDao = delegatedServiceDao;
        }

        @Override
        protected Void doInBackground(final DelegatedServiceEntity... delegatedServiceEntities) {
            mDelegatedServiceDao.insertDelegatedService(delegatedServiceEntities[0]);
//            Timber.e("Inserting Delegated Service->%s", Arrays.toString(delegatedServiceEntities));
            return null;
        }
    }

    private static class deleteAllDelegatedServicesAsyncTask extends AsyncTask<Void, Void, Void> {
        private DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

        deleteAllDelegatedServicesAsyncTask(DelegatedServiceDao delegatedServiceDao) {
            this.mDelegatedServiceAsyncTaskDao = delegatedServiceDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDelegatedServiceAsyncTaskDao.deleteAllDelegatedServices();
//            Timber.e("Deleting Delegated Service!");
            return null;
        }
    }

    private static class updateDelegatedServiceAsyncTask extends AsyncTask<DelegatedServiceEntity, Void, Void> {
        private DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

        updateDelegatedServiceAsyncTask(DelegatedServiceDao delegatedServiceDao) {
            mDelegatedServiceAsyncTaskDao = delegatedServiceDao;
        }

        @Override
        protected Void doInBackground(final DelegatedServiceEntity... delegatedServiceEntities) {
            mDelegatedServiceAsyncTaskDao.updateDelegatedService(delegatedServiceEntities[0]);
//            Timber.e("Updating Delegated Service->%s", Arrays.toString(delegatedServiceEntities));
            return null;
        }
    }

    /**
     * Repository API call for CRUDING Service Provider. We use the DAO to abstract the connection to the
     * ServiceProviderEntity. DAO calls implemented are C-InsertServiceProvider; R-LiveData (exempt from AsyncOps); U-UpdateServiceProvider && D-DeleteServiceProvider.
     * Each are done asynchronously because RoomDB does not run on the main thread
     **/

    public void insertServiceProvider(ServiceProviderEntity serviceProviderEntityOld) {
        new insertServiceProviderAsyncTask(serviceProviderDao).execute(serviceProviderEntityOld);
    }

    public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveData() {
//        Timber.e("Service Provider LiveData->%s", serviceProviderEntityLiveData);
        return serviceProviderEntityLiveData;
    }

    public List<ServiceProviderEntity> getServiceProviders() {
        try {
            return new getServiceProvidersAsyncTask(serviceProviderDao, this).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveDataById(String serviceId) {
        serviceProviderEntityLiveData = serviceProviderDao.getServiceProviderLiveDataById(serviceId);
//        Timber.e("Service Provider LiveData->%s", serviceProviderEntityLiveData.getValue().getServiceProviderId());
        return serviceProviderEntityLiveData;
    }

    public void deleteServiceProvider() {
        new deleteServiceProviderAsyncTask(serviceProviderDao).execute();
    }

    /*public void countServiceProviders() {
        new getServiceProvidersAsyncTask.countServiceProviderAsyncTask(serviceProviderDao).execute();
    }*/

    /*public void updateServiceProvider(ServiceProviderEntity serviceProviderEntityOld) {
        new getServiceProvidersAsyncTask.updateServiceProviderAsyncTask(serviceProviderDao).execute(serviceProviderEntityOld);
    }*/

    private static class insertServiceProviderAsyncTask extends AsyncTask<ServiceProviderEntity, Void, Void> {
        private ServiceProviderDao mServiceProviderDao;

        private insertServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao) {
            this.mServiceProviderDao = serviceProviderDao;
        }

        @Override
        protected Void doInBackground(final ServiceProviderEntity... serviceProviderEntities) {
            mServiceProviderDao.insertServiceProvider(serviceProviderEntities[0]);
//            Timber.e("Inserting Service Provider->%s", serviceProviderEntities[0].getServiceProviderName());
            return null;
        }
    }

    private static class deleteServiceProviderAsyncTask extends AsyncTask<Void, Void, Void> {
        private ServiceProviderDao mServiceProviderAsyncTaskDao;

        deleteServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao) {
            this.mServiceProviderAsyncTaskDao = serviceProviderDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mServiceProviderAsyncTaskDao.deleteAllServiceProviders();
//            Timber.e("Deleting Service Provider!");
            return null;
        }
    }

    private static class getServiceProvidersAsyncTask extends AsyncTask<Void, Void, List<ServiceProviderEntity>> {
        private final ServiceProviderDao mServiceProviderAsyncTaskDao;
        private final List<ServiceProviderEntity> serviceProviderEntityList = new ArrayList<>();
        private WeakReference<AppRepository> appRepositoryWeakReference;

        getServiceProvidersAsyncTask(ServiceProviderDao serviceProviderDao, AppRepository appRepository) {
            appRepositoryWeakReference = new WeakReference<>(appRepository);
            this.mServiceProviderAsyncTaskDao = serviceProviderDao;
        }

        @Override
        protected List<ServiceProviderEntity> doInBackground(Void... voids) {
            serviceProviderEntityList.addAll((Collection<? extends ServiceProviderEntity>) mServiceProviderAsyncTaskDao.getServiceProviderLiveData());
            return serviceProviderEntityList;
        }

        /**
         * This `onPostExecute does nothing significant, I'm reserving it to refer on how to use
         * "WeakReferences" to access a class' data-members from within another when one class is
         * defined as `static`
         **//*
        @Override
        protected void onPostExecute(List<ServiceProviderEntityOld> serviceProviderEntities) {
            super.onPostExecute(serviceProviderEntities);
            AppRepository appRepository = appRepositoryWeakReference.get();
            appRepository.serviceProviders.addAll(serviceProviderEntityOldList);

        }*/
    }

        /*private static class countServiceProviderAsyncTask extends AsyncTask<Void, Void, Void> {
            private final ServiceProviderDao mServiceProviderAsyncTaskDao;

            countServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao) {
                this.mServiceProviderAsyncTaskDao = serviceProviderDao;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                mServiceProviderAsyncTaskDao.getServiceProviderCount();
                Timber.e("Counting Service Providers...");
                return null;
            }
        }*/

        private static class updateServiceProviderAsyncTask extends AsyncTask<ServiceProviderEntity, Void, Void> {
            private ServiceProviderDao mServiceProviderAsyncTaskDao;

            updateServiceProviderAsyncTask(ServiceProviderDao serviceProviderDao) {
                mServiceProviderAsyncTaskDao = serviceProviderDao;
            }

            @Override
            protected Void doInBackground(final ServiceProviderEntity... serviceProviderEntities) {
                mServiceProviderAsyncTaskDao.updateServiceProviders(serviceProviderEntities[0]);
//            Timber.e("Updating Service Provider->%s", Arrays.toString(serviceProviderEntities));
                return null;
            }
        }

        /**
         * Repository API call for CRUDING ProductModel. We use the DAO to abstract the connection to the
         * ProductEntity. DAO calls implemented are C-InsertProduct; R-LiveData (exempt from AsyncOps); U-UpdateProduct && D-DeleteProduct.
         * Each are done asynchronously because RoomDB does not run on the main thread
         **/

        public void insertProduct(ProductEntity productEntity) {
            new insertProductAsyncTask(productDao).execute(productEntity);
        }

        public LiveData<ProductEntity> getProductEntityLiveData() {
//        Timber.e("ProductModel LiveData->%s", productEntityLiveData);
            return productEntityLiveData;
        }

        public LiveData<ProductEntity> getProductEntityLiveDataById(String productId) {
            productEntityLiveData = productDao.getProductEntityLiveDataById(productId);
            return productEntityLiveData;
        }

        public LiveData<ProductEntity> getDelegatedProduct(Boolean isDelegated) {
            productEntityLiveData = productDao.getDelegatedProduct(isDelegated);
            return productEntityLiveData;
        }

        public void deleteAllProducts() {
            new deleteAllProductsAsyncTask(productDao).execute();
        }

        public void updateProduct(ProductEntity productEntity) {
            new updateProductAsyncTask(productDao).execute(productEntity);
        }

        private static class insertProductAsyncTask extends AsyncTask<ProductEntity, Void, Void> {
            private ProductDao mProductDao;

            private insertProductAsyncTask(ProductDao productDao) {
                this.mProductDao = productDao;
            }

            @Override
            protected Void doInBackground(final ProductEntity... productEntities) {
                mProductDao.insertProduct(productEntities[0]);
//            Timber.e("Inserting ProductModel->%s", productEntities[0].getProductName());

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
                    Log.e(TAG, "Inserting ProductModel data->"+ Arrays.toString(data));
                    Log.e(TAG, "Inserting ProductModel code->"+ code);
                }
            };*/
                return null;
            }
        }

        private static class deleteAllProductsAsyncTask extends AsyncTask<Void, Void, Void> {
            private ProductDao mProductAsyncTaskDao;

            deleteAllProductsAsyncTask(ProductDao productDao) {
                this.mProductAsyncTaskDao = productDao;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                mProductAsyncTaskDao.deleteAllProducts();
//            Timber.e("Deleting ProductModel!");
                return null;
            }
        }

        private static class updateProductAsyncTask extends AsyncTask<ProductEntity, Void, Void> {
            private ProductDao mProductAsyncTaskDao;

            updateProductAsyncTask(ProductDao productDao) {
                mProductAsyncTaskDao = productDao;
            }

            @Override
            protected Void doInBackground(final ProductEntity... productEntities) {
                mProductAsyncTaskDao.updateProduct(productEntities[0]);
//            Timber.e("Updating ProductModel->%s", Arrays.toString(productEntities));
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

        private void unsafeMethod() {
            throw new UnsupportedOperationException("This needs attention!");
        }

        private ArrayList<String> listFromJSONArray(JSONArray arr) {
            try {
                ArrayList<String> tbr = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    tbr.add(arr.getString(i));
                }
                return tbr;
            } catch (JSONException e) {
                return new ArrayList<String>();
            }
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
