package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import no.nordicsemi.android.mesh.Scene;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface SceneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Scene scene);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Scene scene);

    @Delete
    void delete(final Scene scene);
}
