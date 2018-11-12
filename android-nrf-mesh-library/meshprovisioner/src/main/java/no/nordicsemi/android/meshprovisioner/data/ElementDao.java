package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ElementDao {

    @Insert
    void insert(final Element element);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Element> elements);

    @Update
    void update(final Element element);

    @Delete
    void delete(final Element element);

    @Query("DELETE FROM elements")
    void deleteAll();

}
