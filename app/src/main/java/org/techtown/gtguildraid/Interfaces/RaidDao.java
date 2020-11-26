package org.techtown.gtguildraid.Interfaces;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Raid;

import java.util.Date;
import java.util.List;

@Dao
public abstract class RaidDao {
    @Insert
    public abstract long insertRaid(Raid raid);

    @Insert
    public abstract void insertBossList(List<Boss> bosses);

    @Query("SELECT * FROM Raid")
    public abstract List<Raid> getAllRaids();

    @Query("SELECT * FROM Raid WHERE raidId =:id")
    public abstract Raid getRaid(int id);

    @Query("SELECT EXISTS(SELECT * FROM Raid WHERE endDay > :today)")
    public abstract Boolean isCurrentRaidExist(Date today);

    @Query("SELECT * FROM Raid WHERE endDay > :today")
    public abstract Raid getCurrentRaid(Date today);

    @Query("SELECT * FROM Raid WHERE endDay <= :today")
    public abstract List<Raid> getPastRaids(Date today);

    @Query("SELECT * FROM Boss WHERE raidId =:raidId")
    public abstract List<Boss> getBossesList(int raidId);

    @Query("UPDATE raid SET name = :sName, startDay = :startDate, endDay = :endDate WHERE raidId = :sID")
    public abstract void updateRaid(int sID, String sName, Date startDate, Date endDate);

    @Query("UPDATE boss SET name = :sName, hardness = :hardness WHERE bossId = :sID")
    public abstract void updateBoss(int sID, String sName, double hardness);

    public void insertRaidWithBosses(Raid raid, List<Boss> bosses) {
        int id = (int)insertRaid(raid);
        for (int i = 0; i < bosses.size(); i++) {
            Log.d("InsertRaidId", id + "");
            bosses.get(i).setRaidId(id);
        }
        insertBossList(bosses);
    }

    public Raid getCurrentRaidWithBosses(Date date) {
        Raid raid = getCurrentRaid(date);
        Log.d("CurrentRaidId", Integer.toString(raid.getRaidId()));
        Log.d("raidInfo", raid.getName());
        List<Boss> bosses = getBossesList(raid.getRaidId());

        raid.setBossList(bosses);
        return raid;
    }
}
