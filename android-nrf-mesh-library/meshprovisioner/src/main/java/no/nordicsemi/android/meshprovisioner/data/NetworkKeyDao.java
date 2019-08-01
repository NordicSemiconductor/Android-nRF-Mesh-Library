package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.NetworkKey;

@SuppressWarnings("unused")
@Dao
public interface NetworkKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final NetworkKey networkKey);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final NetworkKey networkKey);

    @Delete
    void delete(final NetworkKey networkKey);
}
