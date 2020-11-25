package org.techtown.gtguildraid.Interfaces;

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
    public abstract void insertRaid(Raid raid);

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

    @Query("SELECT * FROM Boss WHERE bossId =:raidId")
    public abstract List<Boss> getBossList(int raidId);

    @Query("UPDATE raid SET name = :sName, startDay = :startDate, endDay = :endDate WHERE raidId = :sID")
    public abstract void update(int sID, String sName, Date startDate, Date endDate);

    public void insertRaidWithBosses(Raid raid, List<Boss> bosses) {
        for (int i = 0; i < bosses.size(); i++) {
            bosses.get(i).setRaidId(raid.getRaidId());
        }
        insertBossList(bosses);
        insertRaid(raid);
    }

    public Raid getRaidWithBosses(int id) {
        Raid raid = getRaid(id);
        List<Boss> bosses = getBossList(id);
        raid.setBossList(bosses);
        return raid;
    }
}
