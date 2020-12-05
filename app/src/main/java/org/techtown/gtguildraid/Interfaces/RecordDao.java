package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Record;

import java.util.ArrayList;
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
            List<Integer> heroIds = record.getHeroIds();

            List<Hero> heroes = new ArrayList<>();
            for(int heroId : heroIds){
                heroes.add(getHero(heroId));
            }

            Boss boss;
            if(record.getBossId() != -1) {
                boss = getBoss(record.getBossId());
            }
            else{
                boss = new Boss();
                boss.setName("없음");
            }
            record.setBoss(boss);
        }

        return records;
    }
}
