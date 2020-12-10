package xyz.ummo.user.data.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import xyz.ummo.user.data.dao.ProfileDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.dao.ServiceDao;
import xyz.ummo.user.data.dao.ServiceProviderDao;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.entity.ProfileEntity;
//import xyz.ummo.user.data.entity.ServiceProviderEntityOld;
import xyz.ummo.user.data.entity.ServiceEntity;
import xyz.ummo.user.data.entity.ServiceProviderEntity;
import xyz.ummo.user.data.utils.Converters;
//import xyz.ummo.user.data.dao.AgentDao;
import xyz.ummo.user.data.dao.DelegatedServiceDao;
import xyz.ummo.user.data.dao.ProductDao;
//import xyz.ummo.user.data.dao.ServiceProviderDao;
//import xyz.ummo.user.data.entity.AgentEntity;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
//import xyz.ummo.user.data.entity.ProductEntity;
//import xyz.ummo.user.data.entity.ServiceProviderEntity;

import static androidx.room.Room.databaseBuilder;

@Database(entities = {
        DelegatedServiceEntity.class,
        ProductEntity.class,
        ProfileEntity.class,
        ServiceProviderEntity.class,
        ServiceEntity.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class UserRoomDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "UMMO-USER-DB";
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public abstract ProfileDao profileDao();

    public abstract DelegatedServiceDao delegatedServiceDao();

    public abstract ProductDao productDao();

    public abstract ServiceProviderDao serviceProviderDao();

    public abstract ServiceDao serviceDao();

    //    public abstract ServiceProviderDao serviceProviderDao();
    private static volatile UserRoomDatabase INSTANCE;

/*    public static AgentRoomDatabase getInstance(final Context context,
                                                final AppExecutors appExecutors){
        if (INSTANCE == null){
            synchronized (AgentRoomDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = buildDatabase(context, appExecutors);
                    INSTANCE.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }*/

    public static UserRoomDatabase getUserDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserRoomDatabase.class) {
                //Create database here
                INSTANCE = databaseBuilder(context.getApplicationContext(),
                        UserRoomDatabase.class,
                        "user_database")
                        .addCallback(roomDatabaseCallback)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        return INSTANCE;
    }

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    /*private static void insertUserData(final UserRoomDatabase database, final UserEntity userEntity){
        database.runInTransaction(() -> {
            database.userDao().insertAgent(userEntity);
        });
    }*/

    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final ProfileDao profileDao;
        private DelegatedServiceDao delegatedServiceDao;
        private ProductDao productDao;
        private ServiceProviderDao serviceProviderDao;
        private ServiceDao serviceDao;

        private PopulateDbAsync(UserRoomDatabase userRoomDatabase) {
            profileDao = userRoomDatabase.profileDao();
            delegatedServiceDao = userRoomDatabase.delegatedServiceDao();
            productDao = userRoomDatabase.productDao();
            serviceProviderDao = userRoomDatabase.serviceProviderDao();
            serviceDao = userRoomDatabase.serviceDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            agentDao.deleteAgent();
            /*agentDao.insertAgent(new AgentEntity("76157688","Jose","rego@ummo.xyz",
                    "920514","Carlos"));*/

/*            delegatedServiceDao.insertDelegatedService(new DelegatedServiceEntity("12345",
                    "4321",
                    "Licence Renewal", "This is it...",
                    "Doc 1 & Form 1", "R150",
                    "4 hours","Step 1; Step 2; Step 3"));*/
            return null;
        }
    }
}
