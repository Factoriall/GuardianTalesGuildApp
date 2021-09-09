package org.techtown.gtguildraid.interfaces.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.techtown.gtguildraid.models.entities.Boss;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface BossDao {
    @Insert(onConflict = REPLACE)
    void insert(Boss boss);

    @Delete
    void delete(Boss boss);

    @Update
    void update(Boss boss);

    @Query("SELECT * FROM Boss WHERE bossId = :bossId")
    Boss getBoss(int bossId);
}
