package ng.riby.androidtest.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UserLocationDao {

    @Query("SELECT * FROM user_locations")
    List<UserLocation> getAll();

    @Insert
    void insert(UserLocation userLocation);

    @Delete
    void delete(UserLocation userLocation);

    @Update
    void update(UserLocation userLocation);

}
