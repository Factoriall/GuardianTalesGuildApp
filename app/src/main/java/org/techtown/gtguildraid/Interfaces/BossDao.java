package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

import org.techtown.gtguildraid.Models.Boss;

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
}
