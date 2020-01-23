package ng.riby.androidtest.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {UserLocation.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserLocationDao userLocationDao();
}
