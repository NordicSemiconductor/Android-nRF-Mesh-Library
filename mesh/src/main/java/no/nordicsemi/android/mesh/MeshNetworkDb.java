package no.nordicsemi.android.mesh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import no.nordicsemi.android.mesh.data.ApplicationKeyDao;
import no.nordicsemi.android.mesh.data.ApplicationKeysDao;
import no.nordicsemi.android.mesh.data.GroupDao;
import no.nordicsemi.android.mesh.data.GroupsDao;
import no.nordicsemi.android.mesh.data.MeshNetworkDao;
import no.nordicsemi.android.mesh.data.NetworkKeyDao;
import no.nordicsemi.android.mesh.data.NetworkKeysDao;
import no.nordicsemi.android.mesh.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.mesh.data.ProvisionedMeshNodesDao;
import no.nordicsemi.android.mesh.data.ProvisionerDao;
import no.nordicsemi.android.mesh.data.ProvisionersDao;
import no.nordicsemi.android.mesh.data.SceneDao;
import no.nordicsemi.android.mesh.data.ScenesDao;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Database(entities = {
        MeshNetwork.class,
        NetworkKey.class,
        ApplicationKey.class,
        Provisioner.class,
        ProvisionedMeshNode.class,
        Group.class,
        Scene.class},
        version = 12)
abstract class MeshNetworkDb extends RoomDatabase {

    private static final String TAG = MeshNetworkDb.class.getSimpleName();

    abstract MeshNetworkDao meshNetworkDao();

    abstract NetworkKeyDao networkKeyDao();

    abstract NetworkKeysDao networkKeysDao();

    abstract ApplicationKeyDao applicationKeyDao();

    abstract ApplicationKeysDao applicationKeysDao();

    abstract ProvisionerDao provisionerDao();

    abstract ProvisionersDao provisionersDao();

    abstract ProvisionedMeshNodeDao provisionedMeshNodeDao();

    abstract ProvisionedMeshNodesDao provisionedMeshNodesDao();

    abstract GroupsDao groupsDao();

    abstract GroupDao groupDao();

    abstract ScenesDao scenesDao();

    abstract SceneDao sceneDao();

    private static volatile MeshNetworkDb INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Returns the mesh database
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static MeshNetworkDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeshNetworkDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeshNetworkDb.class, "mesh_network_database.db")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .addMigrations(MIGRATION_8_9)
                            .addMigrations(MIGRATION_9_10)
                            .addMigrations(MIGRATION_10_11)
                            .addMigrations(MIGRATION_11_12)
                            .build();
                }

            }
        }
        return INSTANCE;
    }


    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     * <p>
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

    void insertNetwork(@NonNull final MeshNetworkDao meshNetworkDao,
                       @NonNull final NetworkKeysDao netKeysDao,
                       @NonNull final ApplicationKeysDao appKeysDao,
                       @NonNull final ProvisionersDao provisionersDao,
                       @NonNull final ProvisionedMeshNodesDao nodesDao,
                       @NonNull final GroupsDao groupsDao,
                       @NonNull final ScenesDao scenesDao,
                       @NonNull final MeshNetwork meshNetwork) {
        databaseWriteExecutor.execute(() -> {

            meshNetworkDao.insert(meshNetwork);
            netKeysDao.insert(meshNetwork.netKeys);
            appKeysDao.insert(meshNetwork.appKeys);
            provisionersDao.insert(meshNetwork.provisioners);
            if (!meshNetwork.nodes.isEmpty()) {
                nodesDao.insert(meshNetwork.nodes);
            }

            if (meshNetwork.groups != null) {
                groupsDao.insert(meshNetwork.groups);
            }

            if (meshNetwork.scenes != null) {
                scenesDao.insert(meshNetwork.scenes);
            }
        });
    }

    void loadNetwork(@NonNull final MeshNetworkDao meshNetworkDao,
                     @NonNull final NetworkKeysDao netKeysDao,
                     @NonNull final ApplicationKeysDao appKeysDao,
                     @NonNull final ProvisionersDao provisionersDao,
                     @NonNull final ProvisionedMeshNodesDao nodesDao,
                     @NonNull final GroupsDao groupsDao,
                     @NonNull final ScenesDao scenesDao,
                     @NonNull final LoadNetworkCallbacks listener) {
        databaseWriteExecutor.execute(() -> {
            final MeshNetwork meshNetwork = meshNetworkDao.getMeshNetwork(true);
            if (meshNetwork != null) {
                meshNetwork.netKeys = netKeysDao.loadNetworkKeys(meshNetwork.getMeshUUID());
                meshNetwork.appKeys = appKeysDao.loadApplicationKeys(meshNetwork.getMeshUUID());
                meshNetwork.nodes = nodesDao.getNodes(meshNetwork.getMeshUUID());
                meshNetwork.provisioners = provisionersDao.getProvisioners(meshNetwork.getMeshUUID());
                meshNetwork.groups = groupsDao.loadGroups(meshNetwork.getMeshUUID());
                meshNetwork.scenes = scenesDao.loadScenes(meshNetwork.getMeshUUID());
            }
            listener.onNetworkLoadedFromDb(meshNetwork);
        });
    }

    MeshNetwork getMeshNetwork(@NonNull final MeshNetworkDao meshNetworkDao, @NonNull final String meshUuid) throws ExecutionException, InterruptedException {
        return databaseWriteExecutor.submit(() -> meshNetworkDao.getMeshNetwork(meshUuid)).get();
    }

    List<MeshNetwork> getMeshNetworks(@NonNull final MeshNetworkDao meshNetworkDao) throws ExecutionException, InterruptedException {
        return databaseWriteExecutor.submit(meshNetworkDao::getMeshNetworks).get();
    }

    void update(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork network) {
        databaseWriteExecutor.execute(() -> dao.update(network.meshUUID, network.meshName, network.timestamp,
                network.partial, MeshTypeConverters.ivIndexToJson(network.ivIndex),
                network.lastSelected,
                MeshTypeConverters.networkExclusionsToJson(network.networkExclusions)));
    }

    void update(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork meshNetwork, final boolean lastSelected) throws ExecutionException, InterruptedException {
        databaseWriteExecutor.submit(() -> dao.update(meshNetwork.meshUUID, lastSelected)).get();
    }

    void update(@NonNull final MeshNetworkDao dao, @NonNull final List<MeshNetwork> meshNetworks) {
        databaseWriteExecutor.execute(() -> dao.update(meshNetworks));
    }

    void update(@NonNull final MeshNetwork network,
                @NonNull final MeshNetworkDao networkDao,
                @NonNull final NetworkKeysDao netKeyDao,
                @NonNull final ApplicationKeysDao appKeyDao,
                @NonNull final ProvisionersDao provisionersDao,
                @NonNull final ProvisionedMeshNodesDao nodesDao,
                @NonNull final GroupsDao groupsDao,
                @NonNull final ScenesDao sceneDao) {
        databaseWriteExecutor.execute(() -> {
            networkDao.update(network.meshUUID, network.meshName, network.timestamp,
                    network.partial, MeshTypeConverters.ivIndexToJson(network.ivIndex),
                    network.lastSelected,
                    MeshTypeConverters.networkExclusionsToJson(network.networkExclusions));
            netKeyDao.update(network.getNetKeys());
            appKeyDao.update(network.getAppKeys());
            provisionersDao.update(network.getProvisioners());
            nodesDao.update(network.getNodes());
            groupsDao.update(network.getGroups());
            sceneDao.update(network.getScenes());
        });
    }

    void delete(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork meshNetwork) {
        databaseWriteExecutor.execute(() -> dao.delete(meshNetwork));
    }

    void insert(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        databaseWriteExecutor.execute(() -> dao.insert(networkKey));
    }

    void update(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        databaseWriteExecutor.execute(() -> dao.update(networkKey));
    }

    void delete(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        databaseWriteExecutor.execute(() -> dao.delete(networkKey.getKeyIndex()));
    }

    void insert(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        databaseWriteExecutor.execute(() -> dao.insert(applicationKey));
    }

    void update(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        databaseWriteExecutor.execute(() -> dao.update(applicationKey));
    }

    void delete(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        databaseWriteExecutor.execute(() -> dao.delete(applicationKey));
    }

    void insert(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        databaseWriteExecutor.execute(() -> dao.insert(provisioner));
    }

    void update(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        databaseWriteExecutor.execute(() -> dao.update(provisioner));
    }

    void update(@NonNull final ProvisionerDao dao, @NonNull final List<Provisioner> provisioners) {
        databaseWriteExecutor.execute(() -> dao.update(provisioners));
    }

    void delete(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        databaseWriteExecutor.execute(() -> dao.delete(provisioner));
    }

    List<ProvisionedMeshNode> getNodes(@NonNull final ProvisionedMeshNodesDao dao, @NonNull final String meshUuid) throws ExecutionException, InterruptedException {
        return databaseWriteExecutor.submit(() -> dao.getNodes(meshUuid)).get();
    }

    void insert(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        databaseWriteExecutor.execute(() -> dao.insert(node));
    }

    void update(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        databaseWriteExecutor.execute(() -> dao.update(node));
    }

    void update(@NonNull final ProvisionedMeshNodesDao dao, @NonNull final List<ProvisionedMeshNode> nodes) {
        databaseWriteExecutor.execute(() -> dao.update(nodes));
    }

    void deleteNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        databaseWriteExecutor.execute(() -> dao.delete(node));
    }

    void insert(@NonNull final GroupDao dao, @NonNull final Group group) {
        databaseWriteExecutor.execute(() -> dao.insert(group));
    }

    void update(@NonNull final GroupDao dao, @NonNull final Group group) {
        databaseWriteExecutor.execute(() -> dao.update(group));
    }

    void updateGroups(@NonNull final GroupsDao dao, @NonNull final List<Group> groups) {
        databaseWriteExecutor.execute(() -> dao.update(groups));
    }

    void delete(@NonNull final GroupDao dao, @NonNull final Group group) {
        databaseWriteExecutor.execute(() -> dao.delete(group.getAddress()));
    }

    void insert(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        databaseWriteExecutor.execute(() -> dao.insert(scene));
    }

    void update(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        databaseWriteExecutor.execute(() -> dao.update(scene));
    }

    void delete(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        databaseWriteExecutor.execute(() -> dao.delete(scene.getNumber()));
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork(database);
            migrateNodes(database);
            migrateProvisioner(database);
            migrateGroup(database);
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateGroup2_3(database);
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateNodes3_4(database);
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateProvisioner4_5(database);
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork5_6(database);
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateKeyIndexes6_7(database);
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork7_8(database);
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateProvisioner8_9(database);
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork9_10(database);
        }
    };

    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateNodes10_11(database);
        }
    };

    private static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork11_12(database);
        }
    };

    private static void migrateMeshNetwork(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `mesh_network_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`mesh_name` TEXT, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`iv_index` INTEGER NOT NULL, " +
                "`iv_update_state` INTEGER NOT NULL, " +
                "`unicast_address` INTEGER NOT NULL DEFAULT 0x0001, " +
                "`last_selected` INTEGER NOT NULL, " +
                "PRIMARY KEY(`mesh_uuid`))");

        database.execSQL(
                "INSERT INTO mesh_network_temp (mesh_uuid, mesh_name, timestamp, iv_index, iv_update_state, last_selected) " +
                        "SELECT mesh_uuid, mesh_name, timestamp, iv_index, iv_update_state, last_selected FROM mesh_network");
        final Cursor cursor = database.query("SELECT * FROM mesh_network");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final byte[] unicast = cursor.getBlob(cursor.getColumnIndex("unicast_address"));
                final int address = MeshAddress.addressBytesToInt(unicast);
                final ContentValues values = new ContentValues();
                values.put("unicast_address", address);
                database.update("mesh_network_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "mesh_uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE mesh_network");
        database.execSQL("ALTER TABLE mesh_network_temp RENAME TO mesh_network");
    }

    private static void migrateNodes(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `nodes_temp` " +
                "(`timestamp` INTEGER NOT NULL, " +
                "`mAddedNetworkKeys` TEXT, " +
                "`name` TEXT, `ttl` INTEGER, " +
                "`blacklisted` INTEGER NOT NULL, " +
                "`secureNetworkBeacon` INTEGER, " +
                "`mesh_uuid` TEXT, `uuid` TEXT NOT NULL, " +
                "`security` INTEGER NOT NULL, " +
                "`unicast_address` INTEGER NOT NULL DEFAULT 1, " +
                "`configured` INTEGER NOT NULL, " +
                "`device_key` BLOB, " +
                "`seq_number` INTEGER NOT NULL, " +
                "`cid` INTEGER, " +
                "`pid` INTEGER, " +
                "`vid` INTEGER, " +
                "`crpl` INTEGER, " +
                "`mElements` TEXT, " +
                "`mAddedApplicationKeys` TEXT, " +
                "`networkTransmitCount` INTEGER, " +
                "`networkIntervalSteps` INTEGER, " +
                "`relayTransmitCount` INTEGER, " +
                "`relayIntervalSteps` INTEGER, " +
                "`friend` INTEGER, " +
                "`lowPower` INTEGER, " +
                "`proxy` INTEGER, " +
                "`relay` INTEGER, " +
                "PRIMARY KEY(`uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO nodes_temp (timestamp, mAddedNetworkKeys, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "mAddedApplicationKeys, networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps, " +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid) " +
                        "SELECT timestamp, mAddedNetworkKeys, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "mAddedApplicationKeys, networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps," +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid FROM nodes");

        final Cursor cursor = database.query("SELECT * FROM nodes");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                final byte[] unicast = cursor.getBlob(cursor.getColumnIndex("unicast_address"));
                final int address = MeshAddress.addressBytesToInt(unicast);
                final ContentValues values = new ContentValues();
                values.put("unicast_address", address);
                database.update("nodes_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE nodes");
        database.execSQL("ALTER TABLE nodes_temp RENAME TO nodes");
        database.execSQL("CREATE INDEX index_nodes_mesh_uuid ON `nodes` (mesh_uuid)");
    }

    private static void migrateProvisioner(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `provisioner_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`provisioner_uuid` TEXT NOT NULL, " +
                "`name` TEXT, " +
                "`allocatedGroupRanges` TEXT, " +
                "`allocatedUnicastRanges` TEXT, " +
                "`allocatedSceneRanges` TEXT, " +
                "`sequence_number` INTEGER NOT NULL, " +
                "`provisioner_address` INTEGER NOT NULL DEFAULT 32767," +
                "`global_ttl` INTEGER NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, PRIMARY KEY(`provisioner_uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO provisioner_temp (mesh_uuid, provisioner_uuid, name, allocatedGroupRanges, allocatedUnicastRanges, allocatedSceneRanges, sequence_number, global_ttl, last_selected) " +
                        "SELECT mesh_uuid, provisioner_uuid, name, allocatedGroupRanges, allocatedUnicastRanges, allocatedSceneRanges, sequence_number, global_ttl, last_selected FROM provisioner");

        final Cursor cursor = database.query("SELECT * FROM provisioner");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("provisioner_uuid"));
                final byte[] unicast = cursor.getBlob(cursor.getColumnIndex("provisioner_address"));
                final int address = MeshAddress.addressBytesToInt(unicast);
                final ContentValues values = new ContentValues();
                values.put("provisioner_address", address);
                database.update("provisioner_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "provisioner_uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE provisioner");
        database.execSQL("ALTER TABLE provisioner_temp RENAME TO provisioner");
        database.execSQL("CREATE INDEX index_provisioner_mesh_uuid ON `provisioner` (mesh_uuid)");
    }

    private static void migrateGroup(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `groups_temp` " +
                "(`id` INTEGER PRIMARY KEY NOT NULL," +
                "`mesh_uuid` TEXT, " +
                "`name` TEXT, " +
                "`group_address` INTEGER NOT NULL DEFAULT 49152, " +
                "`parent_address` INTEGER NOT NULL DEFAULT 49152, " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        final Cursor cursor = database.query("SELECT * FROM groups");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final String name = cursor.getString(cursor.getColumnIndex("name"));
                final byte[] grpAddress = cursor.getBlob(cursor.getColumnIndex("group_address"));
                final byte[] pAddress = cursor.getBlob(cursor.getColumnIndex("parent_address"));
                final int groupAddress = MeshParserUtils.unsignedBytesToInt(grpAddress[1], grpAddress[0]);
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", uuid);
                values.put("name", name);
                values.put("group_address", groupAddress);
                if (pAddress != null) {
                    final int parentAddress = MeshParserUtils.unsignedBytesToInt(pAddress[1], pAddress[0]);
                    values.put("parent_address", parentAddress);
                }
                database.insert("groups_temp", SQLiteDatabase.CONFLICT_REPLACE, values);
            } while (cursor.moveToNext());
            cursor.close();
        }

        database.execSQL("DROP TABLE groups");
        database.execSQL("ALTER TABLE groups_temp RENAME TO groups");
        database.execSQL("CREATE INDEX index_groups_mesh_uuid ON `groups` (mesh_uuid)");
    }

    private static void migrateGroup2_3(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `groups_temp` " +
                "(`id` INTEGER PRIMARY KEY NOT NULL," +
                "`mesh_uuid` TEXT, " +
                "`name` TEXT, " +
                "`group_address` INTEGER NOT NULL DEFAULT 49152, " +
                "`parent_address` INTEGER NOT NULL DEFAULT 0, " +
                "`group_address_label` TEXT, " +
                "`parent_address_label` TEXT, " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO groups_temp (id, mesh_uuid, name, group_address, parent_address) " +
                        "SELECT id, mesh_uuid, name, group_address, parent_address FROM groups");

        database.execSQL("DROP TABLE groups");
        database.execSQL("ALTER TABLE groups_temp RENAME TO groups");
        database.execSQL("CREATE INDEX index_groups_mesh_uuid ON `groups` (mesh_uuid)");
    }

    private static void migrateNodes3_4(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `nodes_temp` " +
                "(`timestamp` INTEGER NOT NULL, " +
                "`name` TEXT, `ttl` INTEGER, " +
                "`blacklisted` INTEGER NOT NULL, " +
                "`secureNetworkBeacon` INTEGER, " +
                "`mesh_uuid` TEXT, `uuid` TEXT NOT NULL, " +
                "`security` INTEGER NOT NULL, " +
                "`unicast_address` INTEGER NOT NULL DEFAULT 1, " +
                "`configured` INTEGER NOT NULL, " +
                "`device_key` BLOB, " +
                "`seq_number` INTEGER NOT NULL, " +
                "`cid` INTEGER, " +
                "`pid` INTEGER, " +
                "`vid` INTEGER, " +
                "`crpl` INTEGER, " +
                "`mElements` TEXT, " +
                "`netKeys` TEXT, " +
                "`appKeys` TEXT, " +
                "`networkTransmitCount` INTEGER, " +
                "`networkIntervalSteps` INTEGER, " +
                "`relayTransmitCount` INTEGER, " +
                "`relayIntervalSteps` INTEGER, " +
                "`friend` INTEGER, " +
                "`lowPower` INTEGER, " +
                "`proxy` INTEGER, " +
                "`relay` INTEGER, " +
                "PRIMARY KEY(`uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO nodes_temp (timestamp, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, unicast_address, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps, " +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid) " +
                        "SELECT timestamp, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, unicast_address, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps," +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid FROM nodes");

        final Cursor cursor = database.query("SELECT * FROM nodes");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final ContentValues values = new ContentValues();
                final String uuid = cursor.getString(cursor.getColumnIndex("uuid"));

                final String netKeysJson = cursor.getString(cursor.getColumnIndex("mAddedNetworkKeys"));
                final List<NetworkKey> netKeys = MeshTypeConverters.fromJsonToAddedNetKeys(netKeysJson);
                final List<Integer> keyIndexes = new ArrayList<>();
                for (NetworkKey networkKey : netKeys) {
                    if (networkKey != null) {
                        keyIndexes.add(networkKey.getKeyIndex());
                    }
                }
                values.put("netKeys", MeshTypeConverters.integerToJson(keyIndexes));

                keyIndexes.clear();
                final String appKeysJson = cursor.getString(cursor.getColumnIndex("mAddedApplicationKeys"));
                final Map<Integer, ApplicationKey> appKeyMap = MeshTypeConverters.fromJsonToAddedAppKeys(appKeysJson);
                for (Map.Entry<Integer, ApplicationKey> applicationKeyEntry : appKeyMap.entrySet()) {
                    final ApplicationKey key = applicationKeyEntry.getValue();
                    if (key != null) {
                        keyIndexes.add(key.getKeyIndex());
                    }
                }
                values.put("appKeys", MeshTypeConverters.integerToJson(keyIndexes));
                database.update("nodes_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE nodes");
        database.execSQL("ALTER TABLE nodes_temp RENAME TO nodes");
        database.execSQL("CREATE INDEX index_nodes_mesh_uuid ON `nodes` (mesh_uuid)");
    }

    private static void migrateProvisioner4_5(final SupportSQLiteDatabase database) {
        final List<AllocatedUnicastRange> unicastRange = new ArrayList<>();
        final List<AllocatedGroupRange> groupRange = new ArrayList<>();
        final List<AllocatedSceneRange> sceneRange = new ArrayList<>();
        unicastRange.add(new AllocatedUnicastRange(0x0001, 0x199A));
        groupRange.add(new AllocatedGroupRange(0xC000, 0xCC9A));
        sceneRange.add(new AllocatedSceneRange(0x0001, 0x3333));

        database.execSQL("CREATE TABLE `provisioner_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`provisioner_uuid` TEXT NOT NULL, " +
                "`name` TEXT, " +
                "`allocated_unicast_ranges` TEXT NOT NULL, " +
                "`allocated_group_ranges` TEXT NOT NULL, " +
                "`allocated_scene_ranges` TEXT NOT NULL, " +
                "`sequence_number` INTEGER NOT NULL, " +
                "`provisioner_address` INTEGER," +
                "`global_ttl` INTEGER NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, PRIMARY KEY(`provisioner_uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO provisioner_temp (mesh_uuid, provisioner_uuid, name, " +
                        "allocated_unicast_ranges, allocated_group_ranges, allocated_scene_ranges, " +
                        "sequence_number, global_ttl, last_selected) " +
                        "SELECT mesh_uuid, provisioner_uuid, name, " +
                        "allocatedUnicastRanges, allocatedGroupRanges, allocatedSceneRanges," +
                        "sequence_number, global_ttl, last_selected FROM provisioner");

        final List<Provisioner> provisioners = new ArrayList<>();
        Cursor cursor = database.query("SELECT * FROM provisioner");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String meshUuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final String uuid = cursor.getString(cursor.getColumnIndex("provisioner_uuid"));
                final String name = cursor.getString(cursor.getColumnIndex("name"));
                final String unicastRanges = cursor.getString(cursor.getColumnIndex("allocatedUnicastRanges"));
                final String groupRanges = cursor.getString(cursor.getColumnIndex("allocatedGroupRanges"));
                final String sceneRanges = cursor.getString(cursor.getColumnIndex("allocatedSceneRanges"));
                final int sequenceNumber = cursor.getInt(cursor.getColumnIndex("sequence_number"));
                final int globalTtl = cursor.getInt(cursor.getColumnIndex("global_ttl"));
                final boolean lastSelected = cursor.getInt(cursor.getColumnIndex("last_selected")) == 1;
                final int unicast = cursor.getInt(cursor.getColumnIndex("provisioner_address"));
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", meshUuid);
                values.put("provisioner_uuid", uuid);
                values.put("name", name);
                values.put("sequence_number", sequenceNumber);
                values.put("global_ttl", globalTtl);
                values.put("last_selected", lastSelected);
                if (unicast == 0) {
                    final Integer t = null;
                    values.put("provisioner_address", t);
                } else {
                    values.put("provisioner_address", unicast);
                }
                values.put("allocated_unicast_ranges", unicastRanges.equalsIgnoreCase("null") ?
                        MeshTypeConverters.allocatedUnicastRangeToJson(unicastRange) : unicastRanges);
                values.put("allocated_group_ranges", groupRanges.equalsIgnoreCase("null") ?
                        MeshTypeConverters.allocatedGroupRangeToJson(groupRange) : groupRanges);
                values.put("allocated_scene_ranges", sceneRanges.equalsIgnoreCase("null") ?
                        MeshTypeConverters.allocatedSceneRangeToJson(sceneRange) : sceneRanges);
                database.update("provisioner_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "provisioner_uuid = ?", new String[]{uuid});
                final Provisioner provisioner = new Provisioner(uuid,
                        unicastRanges.equalsIgnoreCase("null") ? unicastRange : MeshTypeConverters.fromJsonToAllocatedUnicastRanges(unicastRanges),
                        groupRanges.equalsIgnoreCase("null") ? groupRange : MeshTypeConverters.fromJsonToAllocatedGroupRanges(groupRanges),
                        sceneRanges.equalsIgnoreCase("null") ? sceneRange : MeshTypeConverters.fromJsonToAllocatedSceneRanges(sceneRanges),
                        meshUuid);
                provisioner.setProvisionerName(name);
                provisioner.setProvisionerAddress(unicast);
                provisioner.setLastSelected(lastSelected);
                provisioner.setGlobalTtl(globalTtl);
                provisioners.add(provisioner);
            } while (cursor.moveToNext());
            cursor.close();
        }

        database.execSQL("DROP TABLE provisioner");
        database.execSQL("ALTER TABLE provisioner_temp RENAME TO provisioner");
        database.execSQL("CREATE INDEX index_provisioner_mesh_uuid ON `provisioner` (mesh_uuid)");
        addProvisionerNodes(database, provisioners);
    }

    private static List<NetworkKey> getNetKeys(@NonNull final SupportSQLiteDatabase database) {
        final List<NetworkKey> keys = new ArrayList<>();
        final Cursor cursor = database.query("SELECT * FROM network_key");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int index = cursor.getInt(cursor.getColumnIndex("index"));
                final byte[] key = cursor.getBlob(cursor.getColumnIndex("key"));
                final NetworkKey networkKey = new NetworkKey(index, key);
                keys.add(networkKey);
            } while (cursor.moveToNext());
        }
        return keys;
    }

    private static List<ApplicationKey> getAppKeys(@NonNull final SupportSQLiteDatabase database) {
        final List<ApplicationKey> keys = new ArrayList<>();
        final Cursor cursor = database.query("SELECT * FROM application_key");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int index = cursor.getInt(cursor.getColumnIndex("index"));
                final byte[] key = cursor.getBlob(cursor.getColumnIndex("key"));
                final ApplicationKey applicationKey = new ApplicationKey(index, key);
                keys.add(applicationKey);
            } while (cursor.moveToNext());
        }
        return keys;
    }

    private static void addProvisionerNodes(@NonNull final SupportSQLiteDatabase database,
                                            @NonNull List<Provisioner> provisioners) {
        if (!provisioners.isEmpty()) {
            final List<NetworkKey> netKeys = getNetKeys(database);
            final List<ApplicationKey> appKeys = getAppKeys(database);
            for (Provisioner provisioner : provisioners) {
                final ProvisionedMeshNode node = new ProvisionedMeshNode(provisioner, netKeys, appKeys);
                final ContentValues values = new ContentValues();
                values.put("timestamp", node.getTimeStamp());
                values.put("name", node.getNodeName());
                values.put("mesh_uuid", node.getMeshUuid());
                values.put("uuid", node.getUuid());
                values.put("ttl", node.getTtl());
                values.put("blacklisted", node.isExcluded());
                values.put("security", node.getSecurity());
                values.put("unicast_address", node.getUnicastAddress());
                values.put("configured", node.isConfigured());
                values.put("device_key", node.getDeviceKey());
                values.put("seq_number", node.getSequenceNumber());
                values.put("mElements", MeshTypeConverters.elementsToJson(node.getElements()));
                final List<Integer> networkKeys = new ArrayList<>();
                for (NetworkKey networkKey : netKeys) {
                    networkKeys.add(networkKey.getKeyIndex());
                }
                final List<Integer> applicationKeys = new ArrayList<>();
                for (ApplicationKey applicationKey : appKeys) {
                    applicationKeys.add(applicationKey.getKeyIndex());
                }
                if (!netKeys.isEmpty()) {
                    values.put("netKeys", MeshTypeConverters.integerToJson(networkKeys));
                }
                if (!appKeys.isEmpty()) {
                    values.put("appKeys", MeshTypeConverters.integerToJson(applicationKeys));
                }
                database.insert("nodes", SQLiteDatabase.CONFLICT_REPLACE, values);
            }
        }
    }

    private static void migrateMeshNetwork5_6(final SupportSQLiteDatabase database) {
        final HashMap<UUID, SparseIntArray> nodesMap = new HashMap<>();
        final Cursor cursor1 = database.query("SELECT mesh_uuid, unicast_address, seq_number FROM nodes");
        if (cursor1 != null && cursor1.moveToFirst()) {
            final UUID meshUuid = UUID.fromString(cursor1.getString(cursor1.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US));
            do {
                final int unicast = cursor1.getInt(cursor1.getColumnIndex("unicast_address"));
                final int seqNumber = cursor1.getInt(cursor1.getColumnIndex("seq_number"));
                SparseIntArray sparseIntArray = nodesMap.get(meshUuid);
                if (sparseIntArray == null) {
                    sparseIntArray = new SparseIntArray();
                }
                sparseIntArray.put(unicast, seqNumber);
                nodesMap.put(meshUuid, sparseIntArray);
            } while (cursor1.moveToNext());
            cursor1.close();
        }

        database.execSQL("ALTER TABLE mesh_network RENAME TO mesh_network_temp");
        database.execSQL("CREATE TABLE `mesh_network` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`mesh_name` TEXT, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`iv_index` INTEGER NOT NULL, " +
                "`iv_update_state` INTEGER NOT NULL, " +
                "`sequence_numbers` TEXT NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, " +
                "PRIMARY KEY(`mesh_uuid`))");
        final Cursor cursor = database.query("SELECT * FROM mesh_network_temp");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String meshUuid = cursor.getString(cursor.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US);
                final String meshName = cursor.getString(cursor.getColumnIndex("mesh_name"));
                final long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                final int ivIndex = cursor.getInt(cursor.getColumnIndex("iv_index"));
                final int ivUpdateState = cursor.getInt(cursor.getColumnIndex("iv_update_state"));
                final boolean lastSelected = cursor.getInt(cursor.getColumnIndex("last_selected")) == 1;
                final SparseIntArray sequenceNumbersArray = nodesMap.get(UUID.fromString(meshUuid));
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", meshUuid);
                values.put("mesh_name", meshName);
                values.put("timestamp", timestamp);
                values.put("iv_index", ivIndex);
                values.put("iv_update_state", ivUpdateState);
                if (sequenceNumbersArray != null) {
                    values.put("sequence_numbers", MeshTypeConverters.sparseIntArrayToJson(sequenceNumbersArray));
                }
                values.put("last_selected", lastSelected);
                database.insert("mesh_network", SQLiteDatabase.CONFLICT_REPLACE, values);
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE mesh_network_temp");
    }

    private static void migrateKeyIndexes6_7(@NonNull final SupportSQLiteDatabase database) {
        final Cursor cursor = database.query("SELECT uuid, netKeys, appKeys FROM nodes");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    final ContentValues values = new ContentValues();
                    final String uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                    final String netKeysJson = cursor.getString(cursor.getColumnIndex("netKeys"));
                    final List<Integer> netKeys = MeshTypeConverters.fromJsonToIntegerList(netKeysJson);
                    final List<NodeKey> netKeyIndexes = new ArrayList<>();
                    for (Integer keyIndex : netKeys) {
                        if (keyIndex != null) {
                            netKeyIndexes.add(new NodeKey(keyIndex, false));
                        }
                    }
                    values.put("netKeys", MeshTypeConverters.nodeKeysToJson(netKeyIndexes));

                    final List<NodeKey> appKeyIndexes = new ArrayList<>();
                    final String appKeysJson = cursor.getString(cursor.getColumnIndex("appKeys"));
                    final List<Integer> appKeys = MeshTypeConverters.fromJsonToIntegerList(appKeysJson);
                    for (Integer keyIndex : appKeys) {
                        appKeyIndexes.add(new NodeKey(keyIndex, false));
                    }
                    values.put("appKeys", MeshTypeConverters.nodeKeysToJson(appKeyIndexes));
                    database.update("nodes", SQLiteDatabase.CONFLICT_REPLACE, values, "uuid = ?", new String[]{uuid});
                } catch (Exception ex) {
                    Log.v(TAG, "Something went wrong while migrating data");
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private static void migrateMeshNetwork7_8(@NonNull final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `mesh_network_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`mesh_name` TEXT, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`iv_index` TEXT NOT NULL, " +
                "`sequence_numbers` TEXT NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, " +
                "PRIMARY KEY(`mesh_uuid`))");

        final Cursor cursor = database.query("SELECT * FROM mesh_network");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final String meshName = cursor.getString(cursor.getColumnIndex("mesh_name"));
                final long timestamp = cursor.getInt(cursor.getColumnIndex("timestamp"));
                final int ivIndex = cursor.getInt(cursor.getColumnIndex("iv_index"));
                final int ivUpdateState = cursor.getInt(cursor.getColumnIndex("iv_update_state"));
                final String sequenceNumbers = cursor.getString(cursor.getColumnIndex("sequence_numbers"));
                final int lastSelected = cursor.getInt(cursor.getColumnIndex("last_selected"));
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", uuid);
                values.put("mesh_name", meshName);
                values.put("timestamp", timestamp);
                values.put("iv_index", MeshTypeConverters.ivIndexToJson(new IvIndex(ivIndex, ivUpdateState == MeshNetwork.IV_UPDATE_ACTIVE, Calendar.getInstance())));
                values.put("sequence_numbers", sequenceNumbers);
                values.put("last_selected", lastSelected);
                database.insert("mesh_network_temp", SQLiteDatabase.CONFLICT_REPLACE, values);
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE mesh_network");
        database.execSQL("ALTER TABLE mesh_network_temp RENAME TO mesh_network");
    }

    private static void migrateProvisioner8_9(@NonNull final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `provisioner_temp` " +
                "(`provisioner_uuid` TEXT NOT NULL, " +
                "`mesh_uuid` TEXT NOT NULL, " +
                "`name` TEXT, " +
                "`allocated_unicast_ranges` TEXT NOT NULL, " +
                "`allocated_group_ranges` TEXT NOT NULL, " +
                "`allocated_scene_ranges` TEXT NOT NULL, " +
                "`provisioner_address` INTEGER," +
                "`global_ttl` INTEGER NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, PRIMARY KEY(`provisioner_uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL("INSERT INTO provisioner_temp (provisioner_uuid,  mesh_uuid, name,  " +
                "allocated_unicast_ranges, allocated_group_ranges, allocated_scene_ranges, " +
                "provisioner_address, global_ttl, last_selected) " +
                "SELECT provisioner_uuid, mesh_uuid, name," +
                "allocated_unicast_ranges, allocated_group_ranges, allocated_scene_ranges," +
                "provisioner_address, global_ttl, last_selected FROM provisioner");
        database.execSQL("DROP TABLE provisioner");
        database.execSQL("CREATE INDEX index_provisioner_mesh_uuid ON `provisioner_temp` (mesh_uuid)");
        database.execSQL("ALTER TABLE provisioner_temp RENAME TO provisioner");
    }

    private static void migrateMeshNetwork9_10(@NonNull final SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE mesh_network ADD COLUMN partial INTEGER NOT NULL DEFAULT 0");
    }

    private static void migrateNodes10_11(@NonNull final SupportSQLiteDatabase database) {
        addColumnNetworkExclusionList(database);
        migrateFromBlacklistedToExcluded(database);
    }

    private static void addColumnNetworkExclusionList(@NonNull final SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE mesh_network ADD COLUMN networkExclusions TEXT NOT NULL DEFAULT '{}'");
    }

    private static void migrateFromBlacklistedToExcluded(@NonNull final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `nodes_temp` " +
                "(timestamp INTEGER NOT NULL, " +
                "netKeys TEXT, " +
                "name TEXT, ttl INTEGER, " +
                "excluded INTEGER NOT NULL, " +
                "secureNetworkBeacon INTEGER, " +
                "mesh_uuid TEXT, uuid TEXT NOT NULL, " +
                "security INTEGER NOT NULL, " +
                "unicast_address INTEGER NOT NULL DEFAULT 1, " +
                "configured INTEGER NOT NULL, " +
                "device_key BLOB, " +
                "seq_number INTEGER NOT NULL, " +
                "cid INTEGER, " +
                "pid INTEGER, " +
                "vid INTEGER, " +
                "crpl INTEGER, " +
                "elements TEXT, " +
                "appKeys TEXT, " +
                "networkTransmitCount INTEGER, " +
                "networkIntervalSteps INTEGER, " +
                "relayTransmitCount INTEGER, " +
                "relayIntervalSteps INTEGER, " +
                "friend INTEGER, " +
                "lowPower INTEGER, " +
                "proxy INTEGER, " +
                "relay INTEGER, " +
                "PRIMARY KEY(uuid), " +
                "FOREIGN KEY(mesh_uuid) REFERENCES mesh_network(mesh_uuid) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO nodes_temp (timestamp, netKeys, name, excluded, secureNetworkBeacon, mesh_uuid, " +
                        "security, unicast_address, configured, device_key, seq_number, cid, pid, vid, crpl, elements, " +
                        "appKeys, networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps, " +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid) " +
                        "SELECT timestamp, netKeys, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, unicast_address, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "appKeys, networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps," +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid FROM nodes");
        database.execSQL("DROP TABLE nodes");
        database.execSQL("ALTER TABLE nodes_temp RENAME TO nodes");
        database.execSQL("CREATE INDEX index_nodes_mesh_uuid ON `nodes` (mesh_uuid)");
    }

    private static void migrateMeshNetwork11_12(@NonNull final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `mesh_network_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                " `mesh_name` TEXT, " +
                " `timestamp` INTEGER NOT NULL DEFAULT 0, " +
                " `partial` INTEGER NOT NULL DEFAULT 0," +
                " `iv_index` TEXT NOT NULL, " +
                " `network_exclusions` TEXT NOT NULL DEFAULT '{}', " +
                " `last_selected` INTEGER NOT NULL, " +
                "PRIMARY KEY(`mesh_uuid`))");
        final Cursor cursor = database.query("SELECT * FROM mesh_network");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final String meshName = cursor.getString(cursor.getColumnIndex("mesh_name"));
                final long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                final String ivIndex = cursor.getString(cursor.getColumnIndex("iv_index"));
                final String networkExclusions = cursor.getString(cursor.getColumnIndex("networkExclusions"));
                final int lastSelected = cursor.getInt(cursor.getColumnIndex("last_selected"));
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", uuid);
                values.put("mesh_name", meshName);
                values.put("timestamp", timestamp);
                values.put("iv_index", ivIndex);
                values.put("network_exclusions", networkExclusions);
                values.put("last_selected", lastSelected);
                database.insert("mesh_network_temp", SQLiteDatabase.CONFLICT_REPLACE, values);
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE mesh_network");
        database.execSQL("ALTER TABLE mesh_network_temp RENAME TO mesh_network");
    }
}
