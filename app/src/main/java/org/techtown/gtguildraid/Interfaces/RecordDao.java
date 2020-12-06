package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Record;

import java.util.List;

@Dao
public abstract class RecordDao {
    @Insert
    public abstract void insertRecord(Record record);

    @Insert
    public abstract void insertHeroList(List<Hero> heroes);

    @Query("SELECT * FROM Record WHERE raidId = :raidId")
    public abstract List<Record> getAllRecords(int raidId);

    @Query("SELECT * FROM Hero WHERE heroId = :heroId")
    public abstract Hero getHero(int heroId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId")
    public abstract List<Record> getCertainMemberRecords(int memberId, int raidId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND day = :day")
    public abstract List<Record> getCertainDayRecords(int memberId, int raidId, int day);


    @Query("SELECT * FROM Boss WHERE bossId = :bossId")
    public abstract Boss getBoss(int bossId);

    public List<Record> getCertainDayRecordsWithHeroes(int memberId, int raidId, int day){
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
        }

        return records;
    }
}
