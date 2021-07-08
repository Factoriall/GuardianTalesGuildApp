package org.techtown.gtguildraid.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Raid;

import java.util.Date;
import java.util.List;

@Dao
public abstract class RaidDao {
    @Insert
    public abstract long insertRaid(Raid raid);

    @Insert
    public abstract void insertBossList(List<Boss> bosses);

    @Delete
    public abstract void delete(Raid raid);

    @Query("SELECT * FROM Raid")
    public abstract List<Raid> getAllRaids();

    @Query("SELECT * FROM Raid WHERE endDay <= :today ORDER BY endDay")
    public abstract List<Raid> getAllRaidsExceptRecent(Date today);

    @Query("SELECT * FROM Raid WHERE raidId =:id")
    public abstract Raid getRaid(int id);

    @Query("SELECT EXISTS(SELECT * FROM Raid WHERE endDay > :today)")
    public abstract Boolean isCurrentRaidExist(Date today);

    @Query("SELECT EXISTS(SELECT * FROM Raid WHERE startDay <= :today AND endDay > :today)")
    public abstract Boolean isStartedRaidExist(Date today);

    @Query("SELECT * FROM Raid WHERE endDay > :today")
    public abstract Raid getCurrentRaid(Date today);

    @Query("SELECT * FROM Raid WHERE endDay <= :today ORDER BY endDay DESC LIMIT 1")
    public abstract Raid getPastRecentRaid(Date today);

    @Query("SELECT * FROM Boss WHERE raidId =:raidId")
    public abstract List<Boss> getBossesList(int raidId);

    @Query("UPDATE raid SET name = :sName, startDay = :startDate, endDay = :endDate, thumbnail = :thumbnail WHERE raidId = :sID")
    public abstract void updateRaid(int sID, String sName, Date startDate, Date endDate, String thumbnail);

    @Query("UPDATE boss SET name = :sName, hardness = :hardness, imgName = :imgName, elementId = :elementId, isFurious = :isFurious WHERE bossId = :sID")
    public abstract void updateBoss(int sID, String sName, String imgName, double hardness, int elementId, boolean isFurious);

    public void insertRaidWithBosses(Raid raid, List<Boss> bosses) {
        int id = (int)insertRaid(raid);
        for (int i = 0; i < bosses.size(); i++) {
            bosses.get(i).setRaidId(id);
        }
        insertBossList(bosses);
    }

    public Raid getRaidWithBosses(int raidId){
        Raid raid = getRaid(raidId);
        List<Boss> bosses = getBossesList(raidId);
        raid.setBossList(bosses);
        return raid;
    }

    public Raid getCurrentRaidWithBosses(Date date) {
        Raid raid = getCurrentRaid(date);
        List<Boss> bosses = getBossesList(raid.getRaidId());

        raid.setBossList(bosses);
        return raid;
    }
}
