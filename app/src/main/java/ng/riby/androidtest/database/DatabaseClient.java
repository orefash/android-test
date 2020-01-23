package ng.riby.androidtest.database;

import android.arch.persistence.room.Room;
import android.content.Context;

public class DatabaseClient {

    private Context mCtx;
    private static DatabaseClient mInstance;

    //our app database object
    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        //creating the app database with Room database builder
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "riby_db").build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

}
