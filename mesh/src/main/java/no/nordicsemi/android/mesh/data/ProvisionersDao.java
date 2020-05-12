package no.nordicsemi.android.mesh.data;

import java.util.List;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import no.nordicsemi.android.mesh.Provisioner;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ProvisionersDao {

    @Query("SELECT * from provisioner WHERE mesh_uuid = :meshUuid")
    List<Provisioner> getProvisioners(final String meshUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Provisioner> provisioners);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Provisioner> provisioners);

    @Query("DELETE FROM provisioner")
    void deleteAll();
}
