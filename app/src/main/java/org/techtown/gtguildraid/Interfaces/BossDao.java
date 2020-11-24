package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.RaidWithBosses;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface BossDao {
    @Insert(onConflict = REPLACE)
    void insert(Boss boss);

    @Delete
    void delete(Boss boss);

    @Delete
    void reset(List<Boss> bosses);

    @Transaction
    @Query("SELECT * FROM raid")
    public List<RaidWithBosses> getBossesWithRaid();
}
