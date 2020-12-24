package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Record;

import java.util.List;

@Dao
public abstract class RecordDao {
    @Insert
    public abstract void insertRecord(Record record);

    @Delete
    public abstract void deleteRecord(Record record);

    @Query("SELECT * FROM Record WHERE raidId = :raidId ORDER BY memberId")
    public abstract List<Record> getAllRecords(int raidId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId")
    public abstract List<Record> getCertainMemberRecords(int memberId, int raidId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND day = :day")
    public abstract List<Record> getCertainDayRecords(int memberId, int raidId, int day);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND bossId = :bossId")
    public abstract List<Record> getCertainBossRecords(int memberId, int raidId, int bossId);

    @Query("SELECT * FROM Record WHERE raidId = :raidId AND bossId = :bossId")
    public abstract List<Record> getAllMemberBossRecords(int raidId, int bossId);

    @Query("SELECT * FROM Boss WHERE bossId = :bossId")
    public abstract Boss getBoss(int bossId);

    @Query("UPDATE Record SET damage = :damage, bossId = :bossId, level = :level" +
            " WHERE recordID = :rId")
    public abstract void updateRecord(int rId, int damage, int bossId, int level);

    public List<Record> getCertainDayRecordsWithBoss(int memberId, int raidId, int day) {
        List<Record> records = getCertainDayRecords(memberId, raidId, day);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);
        }

        return records;
    }


    public List<Record> getAllRecordsWithBoss(int raidId){
        List<Record> records = getAllRecords(raidId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);
        }

        return records;
    }


    public List<Record> getCertainMemberRecordsWithBoss(int memberId, int raidId){
        List<Record> records = getCertainMemberRecords(memberId, raidId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);
        }

        return records;
    }

    public List<Record> getAllRecordsWithOneBoss(int raidId, int bossId) {
        List<Record> records = getAllMemberBossRecords(raidId, bossId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);
        }

        return records;
    }

    public List<Record> getMemberRecordsWithOneBoss(int memberId, int raidId, int bossId) {
        List<Record> records = getCertainBossRecords(memberId, raidId, bossId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);
        }

        return records;
    }

    /*public List<Record> getCertainDayRecordsWithHeroes(int memberId, int raidId, int day){
        List<Record> records = getCertainDayRecords(memberId, raidId, day);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero1 = getHero(record.getHero1Id());
            Hero hero2 = getHero(record.getHero2Id());
            Hero hero3 = getHero(record.getHero3Id());
            Hero hero4 = getHero(record.getHero4Id());

            record.setHero1(hero1);
            record.setHero2(hero2);
            record.setHero3(hero3);
            record.setHero4(hero4);
            record.setLevel(record.getLevel());
        }

        return records;
    }*/
}
