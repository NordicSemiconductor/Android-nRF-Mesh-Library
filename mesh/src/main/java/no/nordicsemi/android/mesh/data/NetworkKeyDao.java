package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import no.nordicsemi.android.mesh.NetworkKey;

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
