package xyz.ummo.user.data.repo;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;
import xyz.ummo.user.data.dao.DelegatedServiceDao;
import xyz.ummo.user.data.dao.ProductDao;
import xyz.ummo.user.data.dao.ProfileDao;
import xyz.ummo.user.data.dao.ServiceCategoryDao;
import xyz.ummo.user.data.dao.ServiceDao;
import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.db.UserRoomDatabase;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.entity.ProfileEntity;
import xyz.ummo.user.data.entity.ServiceCategoryEntity;
import xyz.ummo.user.data.entity.ServiceEntity;
import xyz.ummo.user.data.entity.ServiceProviderEntity;

//import xyz.ummo.user.data.dao.UserDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
//import xyz.ummo.user.data.entity.UserEntity;

public class AppRepository {

    private final ProfileDao profileDao;
    private final DelegatedServiceDao delegatedServiceDao;
    private final ProductDao productDao;
    private final ServiceProviderDao serviceProviderDao;
    private final ServiceDao serviceDao;
    private final ServiceCategoryDao serviceCategoryDao;

    private LiveData<DelegatedServiceEntity> delegatedServiceEntityLiveData;
    private final LiveData<ProfileEntity> profileEntityLiveData;
    //    private final List<ProfileEntity> profileEntityListData;
    private LiveData<ProductEntity> productEntityLiveData;
    private LiveData<ServiceProviderEntity> serviceProviderEntityLiveData;
    private LiveData<ServiceEntity> serviceEntityLiveData;
    private List<ServiceCategoryEntity> serviceCategoryEntities;
    private List<ServiceEntity> delegatableServices;
    private List<ServiceEntity> nonDelegatableServices;
    private List<ServiceEntity> bookmarkedServiceEntityListData;
    private List<ServiceEntity> serviceQueryResponses;
//    private List<ServiceProviderEntityOld> serviceProviders = new ArrayList<>();

    public AppRepository(Application application) {
        UserRoomDatabase userRoomDatabase = UserRoomDatabase.getUserDatabase(application);

        profileDao = userRoomDatabase.profileDao();
        profileEntityLiveData = profileDao.getProfileLiveData();
//        profileEntityListData = profileDao.getProfileListData();
//        Log.e("AppRepo", "User->"+ profileEntityLiveData);

        delegatedServiceDao = userRoomDatabase.delegatedServiceDao();
        delegatedServiceEntityLiveData = delegatedServiceDao.getDelegatedService();

//        Log.e("AppRepo", "DelegatedServiceModel->"+ delegatedServiceEntityLiveData);

        productDao = userRoomDatabase.productDao();
        productEntityLiveData = productDao.getProductLiveData();
//        Timber.e("Product->%s", productEntityLiveData);

        serviceProviderDao = userRoomDatabase.serviceProviderDao();
        serviceProviderEntityLiveData = serviceProviderDao.getServiceProviderLiveData();

        serviceDao = userRoomDatabase.serviceDao();
        serviceEntityLiveData = serviceDao.getServiceLiveData();

        serviceQueryResponses = serviceDao.getServiceListData();

        serviceCategoryDao = userRoomDatabase.serviceCategoryDao();
//        serviceCategoryEntities = serviceCategoryDao.getAllCategories();
    }

    public int getDelegatedServiceCount() {
        try {
            return new getDelegatedServiceCountAsyncTask(delegatedServiceDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            return 0;
        }

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

    public List<ProfileEntity> getProfileEntityListData() {
        try {
            return new getProfileListAsyncTask(profileDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            Timber.e("Getting PROFILE failed because: %s", ie);
            return null;
        }
    }

    public void updateProfile(ProfileEntity profileEntity) {
        new updateProfileAsyncTask(profileDao).execute(profileEntity);
    }

    private static class insertProfileAsyncTask extends AsyncTask<ProfileEntity, Void, Void> {
        private final ProfileDao mProfileAsyncTaskDao;

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
        private final ProfileDao mProfileAsyncTaskDao;

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
        private final ProfileDao mProfileAsyncTaskDao;

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

    private static class getProfileListAsyncTask extends AsyncTask<Void, Void, List<ProfileEntity>> {
        private final ProfileDao mProfileDao;
        private final List<ProfileEntity> mProfileEntityList = new ArrayList<>();

        getProfileListAsyncTask(ProfileDao profileDao) {
            this.mProfileDao = profileDao;
        }

        @Override
        protected List<ProfileEntity> doInBackground(Void... voids) {
            mProfileEntityList.addAll(Objects.requireNonNull(mProfileDao.getProfileListData()));
            return mProfileEntityList;
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
        private final DelegatedServiceDao mDelegatedServiceDao;

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
        private final DelegatedServiceDao mDelegatedServiceDao;

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
        private final DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

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

    /**
     * Getting delegatedServiceCount Async Task
     **/
    private static class getDelegatedServiceCountAsyncTask extends AsyncTask<Void, Void, Integer> {
        private final DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

        getDelegatedServiceCountAsyncTask(DelegatedServiceDao delegatedServiceDao) {
            this.mDelegatedServiceAsyncTaskDao = delegatedServiceDao;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return mDelegatedServiceAsyncTaskDao.getDelegatedServicesCount();
        }
    }

    private static class updateDelegatedServiceAsyncTask extends AsyncTask<DelegatedServiceEntity, Void, Void> {
        private final DelegatedServiceDao mDelegatedServiceAsyncTaskDao;

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
     * Repository API call for CRUD'ing Service Provider. We use the DAO to abstract the connection
     * to the ServiceProviderEntity. DAO calls implemented are C-InsertServiceProvider;
     * R-LiveData (exempt from AsyncOps); U-UpdateServiceProvider && D-DeleteServiceProvider.
     * Each are done asynchronously because RoomDB does not run on the main thread
     **/

    /**
     * 1
     **/
    public void insertServiceProvider(ServiceProviderEntity serviceProviderEntityOld) {
        new insertServiceProviderAsyncTask(serviceProviderDao).execute(serviceProviderEntityOld);
    }

    /**
     * 2
     **/
    public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveData() {
        Timber.e("Service Provider LiveData->%s", serviceProviderEntityLiveData.getValue());
        return serviceProviderEntityLiveData;
    }

    /**
     * 3
     **/
    public List<ServiceProviderEntity> getServiceProviders() {
        try {
            return new getServiceProvidersAsyncTask(serviceProviderDao, this).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.e("Could not get Service Providers -> %s", e);
            return null;
        }
    }

    /**
     * 4
     **/
    public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveDataById(String serviceProviderId) {
        serviceProviderEntityLiveData = serviceProviderDao.getServiceProviderLiveDataById(serviceProviderId);
//        Timber.e("Service Provider LiveData->%s", serviceProviderEntityLiveData.getValue().getServiceProviderId());
        return serviceProviderEntityLiveData;
    }

    /**
     * 5
     **/
    public void deleteServiceProvider() {
        new deleteServiceProviderAsyncTask(serviceProviderDao).execute();
    }

    /*public void countServiceProviders() {
        new getServiceProvidersAsyncTask.countServiceProviderAsyncTask(serviceProviderDao).execute();
    }*/

    /*public void updateServiceProvider(ServiceProviderEntity serviceProviderEntityOld) {
        new getServiceProvidersAsyncTask.updateServiceProviderAsyncTask(serviceProviderDao).execute(serviceProviderEntityOld);
    }*/

    /**
     * 1.1
     **/
    private static class insertServiceProviderAsyncTask extends AsyncTask<ServiceProviderEntity, Void, Void> {
        private final ServiceProviderDao mServiceProviderDao;

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

    /**
     * 5.1
     **/
    private static class deleteServiceProviderAsyncTask extends AsyncTask<Void, Void, Void> {
        private final ServiceProviderDao mServiceProviderAsyncTaskDao;

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

    /**
     * 3.1
     **/
    private static class getServiceProvidersAsyncTask extends AsyncTask<Void, Void, List<ServiceProviderEntity>> {
        private final ServiceProviderDao mServiceProviderAsyncTaskDao;
        private final List<ServiceProviderEntity> serviceProviderEntityList = new ArrayList<>();
        private final WeakReference<AppRepository> appRepositoryWeakReference;

        getServiceProvidersAsyncTask(ServiceProviderDao serviceProviderDao, AppRepository appRepository) {
            appRepositoryWeakReference = new WeakReference<>(appRepository);
            this.mServiceProviderAsyncTaskDao = serviceProviderDao;
        }

        @Override
        protected List<ServiceProviderEntity> doInBackground(Void... voids) {
            serviceProviderEntityList.addAll(mServiceProviderAsyncTaskDao.getServiceProviderListData());
            Timber.e("SERVICE-PROVIDER-LIST ==> %s", serviceProviderEntityList);
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
        private final ServiceProviderDao mServiceProviderAsyncTaskDao;

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

    /** Repository API call for CRUD'ing ServiceModel **/
    /**
     * 1
     **/
    public void insertService(ServiceEntity serviceEntity) {
        new insertServiceAsyncTask(serviceDao).execute(serviceEntity);
    }

    /**
     * 2
     **/
    public LiveData<ServiceEntity> getServiceEntityLiveData() {
        return serviceEntityLiveData;
    }

    /**
     * 3
     **/
    public List<ServiceEntity> getServices() {
        try {
            return new getServicesAsyncTask(serviceDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            Timber.e("Getting Services failed because: %s", ie);
            return null;
        }
    }

    public List<ServiceEntity> getBookmarkedServiceList(Boolean bookmarked) {
        try {
            return new getBookmarkedServiceListAsyncTask(serviceDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            Timber.e("Getting Services failed because: %s", ie);
            return null;
        }
        /*bookmarkedServiceEntityListData = serviceDao.getBookmarkedServicesList(bookmarked);
        return bookmarkedServiceEntityListData;*/
    }

    /**
     * 4
     **/
    public LiveData<ServiceEntity> getServiceEntityLiveDataById(String serviceId) {
        serviceEntityLiveData = serviceDao.getServiceLiveDataById(serviceId);
        Timber.e("Getting Service:" + serviceEntityLiveData + " By ID: " + serviceId);
        return serviceEntityLiveData;
    }

    /**
     * 4.1
     **/
    public List<ServiceEntity> getDelegatableServices() {
        try {
            return new getDelegatableServicesAsyncTask(serviceDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            Timber.e("Getting Delegatable Services failed because: %s", ie);
            return null;
        }
    }

    /**
     * 4.2
     **/
    public List<ServiceEntity> getNonDelegatableServices() {
        try {
            return new getNonDelegatableServicesAsyncTask(serviceDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            Timber.e("Getting Delegatable Services failed because: %s", ie);
            return null;
        }
    }

    /**
     * 5
     **/
    public void deleteService() {
        //TODO: deleteServiceAsyncTask
    }

    /**
     * 6
     **/
    public void updateService(ServiceEntity serviceEntity) {
        new updateServiceAsyncTask(serviceDao).execute(serviceEntity);
    }
    /*public void incrementApprovalCount(ServiceEntity serviceEntity) {
        new incrementApprovalCountAsync(serviceDao).execute(serviceEntity);
    }*/

    /**
     * 1.1
     **/
    private static class insertServiceAsyncTask extends AsyncTask<ServiceEntity, Void, Void> {
        private final ServiceDao mServiceDao;

        private insertServiceAsyncTask(ServiceDao serviceDao) {
            this.mServiceDao = serviceDao;
        }

        @Override
        protected Void doInBackground(ServiceEntity... serviceEntities) {

            for (ServiceEntity serviceEntity : serviceEntities) {
                mServiceDao.insertService(serviceEntity); //TODO: There's an issue here!

                Timber.e("ASYNC SAVE - NAME %s", serviceEntity.getServiceName());
                Timber.e("ASYNC SAVE - DEL. %s", serviceEntity.getDelegatable());
                Timber.e("ASYNC SAVE - UPVOTE %s", serviceEntity.getUsefulCount());
            }
            return null;
        }

    }

    /*private static class incrementApprovalCountAsync extends AsyncTask<ServiceEntity, Void, Void> {
        private final ServiceDao mServiceDao;

        private incrementApprovalCountAsync(ServiceDao serviceDao) {
            this.mServiceDao = serviceDao;
        }

        @Override
        protected Void doInBackground(ServiceEntity... serviceEntities) {
            mServiceDao.incrementApprovalCount(serviceEntities);
            return null;
        }
    }*/

    private static class updateServiceAsyncTask extends AsyncTask<ServiceEntity, Void, Void> {
        private final ServiceDao mServiceAsyncTaskDao;

        updateServiceAsyncTask(ServiceDao serviceDao) {
            mServiceAsyncTaskDao = serviceDao;
        }

        @Override
        protected Void doInBackground(final ServiceEntity... serviceEntities) {
            Timber.e("Updating ProductModel->%s", Arrays.toString(serviceEntities));
            mServiceAsyncTaskDao.updateService(serviceEntities[0]);
            return null;
        }
    }

    /**
     * 3.1
     **/
    private static class getServicesAsyncTask extends AsyncTask<Void, Void, List<ServiceEntity>> {
        private final ServiceDao mServiceAsyncTaskDao;
        private final List<ServiceEntity> mServiceEntityList = new ArrayList<>();

        getServicesAsyncTask(ServiceDao serviceDao) {
            this.mServiceAsyncTaskDao = serviceDao;
        }

        @Override
        protected List<ServiceEntity> doInBackground(Void... voids) {
            mServiceEntityList.addAll(mServiceAsyncTaskDao.getServiceListData());
            return mServiceEntityList;
        }
    }

    private static class getDelegatableServicesAsyncTask extends AsyncTask<Void, Void, List<ServiceEntity>> {
        private final ServiceDao mServiceAsyncTaskDao;
        private final List<ServiceEntity> mDelegatableServicesEntityList = new ArrayList<>();

        getDelegatableServicesAsyncTask(ServiceDao serviceDao) {
            this.mServiceAsyncTaskDao = serviceDao;
        }

        @Override
        protected List<ServiceEntity> doInBackground(Void... voids) {
            mDelegatableServicesEntityList.addAll(mServiceAsyncTaskDao.getDelegatableServices(true));
            return mDelegatableServicesEntityList;
        }
    }

    private static class getNonDelegatableServicesAsyncTask extends AsyncTask<Void, Void, List<ServiceEntity>> {
        private final ServiceDao mServiceAsyncTaskDao;
        private final List<ServiceEntity> mDelegatableServicesEntityList = new ArrayList<>();

        getNonDelegatableServicesAsyncTask(ServiceDao serviceDao) {
            this.mServiceAsyncTaskDao = serviceDao;
        }

        @Override
        protected List<ServiceEntity> doInBackground(Void... voids) {
            mDelegatableServicesEntityList.addAll(mServiceAsyncTaskDao.getNonDelegatableServices(false));
            return mDelegatableServicesEntityList;
        }
    }

    private static class getBookmarkedServiceListAsyncTask extends AsyncTask<Void, Void, List<ServiceEntity>> {
        private final ServiceDao mServiceAsyncTaskDao;
        private final List<ServiceEntity> mBookmarkedServicesList = new ArrayList<>();

        getBookmarkedServiceListAsyncTask(ServiceDao serviceDao) {
            this.mServiceAsyncTaskDao = serviceDao;
        }

        @Override
        protected List<ServiceEntity> doInBackground(Void... voids) {
            mBookmarkedServicesList.addAll(mServiceAsyncTaskDao.getBookmarkedServicesList(true));
            Timber.e("BOOKMARKED SERVICES -> %s", mBookmarkedServicesList);
            return mBookmarkedServicesList;
        }
    }

    /*private static class searchDatabaseAsyncTask extends AsyncTask<Void, Void, List<ServiceEntity>> {
        private final ServiceDao mServiceDaoAsyncTask;
        private final List<ServiceEntity> mDBSearchedServicesList = new ArrayList<>();

        searchDatabaseAsyncTask(ServiceDao serviceDao) {
            this.mServiceDaoAsyncTask = serviceDao;
        }

        @Override
        protected List<ServiceEntity> doInBackground(Void... voids) {
            mDBSearchedServicesList.addAll(mServiceDaoAsyncTask.searchRoomDB(voids));
            Timber.e("SEARCHING FOR SERVICES -> %s", mDBSearchedServicesList);
            return mDBSearchedServicesList;
        }
    }*/

    public List<ServiceEntity> searchDatabase(String query) {
        serviceQueryResponses = serviceDao.searchRoomDB(query);
        Timber.e("SERVICE SEARCHES ->" + serviceQueryResponses);
        return serviceQueryResponses;
    }

    /*private static class searchDatabaseAsyncTask extends AsyncTask<Void, Void, List<ServiceEntity>> {
        private final ServiceDao mServiceAsyncTaskDao;
        private final List<ServiceEntity> mDBSearchedServicesList = new ArrayList<>();

        searchDatabaseAsyncTask(ServiceDao serviceDao) {this.mDBSearchedServicesList = serviceDao}

        @Override
        protected List<ServiceEntity> doInBackground(Void... voids) {
            mServiceAsyncTaskDao.searchRoomDB()
        }
    }*/

    /**
     * 5.1
     **/
    private static class deleteServicesAsyncTask extends AsyncTask<Void, Void, Void> {
        private final ServiceDao mServiceAsyncTaskDao;

        deleteServicesAsyncTask(ServiceDao serviceDao) {
            this.mServiceAsyncTaskDao = serviceDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mServiceAsyncTaskDao.deleteServices();
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
        private final ProductDao mProductDao;

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
        private final ProductDao mProductAsyncTaskDao;

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
        private final ProductDao mProductAsyncTaskDao;

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

    public void insertServiceCategory(ServiceCategoryEntity serviceCategoryEntity) {
        new insertServiceCategoryAsyncTask(serviceCategoryDao).execute(serviceCategoryEntity);
    }

    public List<ServiceCategoryEntity> getServiceCategoryEntities() {
        try {
            return new getServiceCategoriesAsyncTask(serviceCategoryDao).execute().get();
        } catch (ExecutionException | InterruptedException ie) {
            return null;
        }
    }

    /**
     * Inserting Service Categories into Room
     **/
    private static class insertServiceCategoryAsyncTask extends AsyncTask<ServiceCategoryEntity, Void, Void> {
        private final ServiceCategoryDao mServiceCategoryDao;

        private insertServiceCategoryAsyncTask(ServiceCategoryDao serviceCategoryDao) {
            this.mServiceCategoryDao = serviceCategoryDao;
        }

        @Override
        protected Void doInBackground(final ServiceCategoryEntity... serviceCategoryEntities) {
            mServiceCategoryDao.insertCategory(serviceCategoryEntities[0]);
            return null;
        }
    }

    private static class getServiceCategoriesAsyncTask extends AsyncTask<Void, Void, List<ServiceCategoryEntity>> {
        private final ServiceCategoryDao mServiceCategoryAsyncTaskDao;
        private final List<ServiceCategoryEntity> mServiceCategoryEntityList = new ArrayList<>();

        getServiceCategoriesAsyncTask(ServiceCategoryDao serviceCategoryDao) {
            this.mServiceCategoryAsyncTaskDao = serviceCategoryDao;
        }

        @Override
        protected List<ServiceCategoryEntity> doInBackground(Void... voids) {
            mServiceCategoryEntityList.addAll(mServiceCategoryAsyncTaskDao.getAllCategories());
            return mServiceCategoryEntityList;
        }
    }

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
