package org.techtown.gtguildraid.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Hero;
import org.techtown.gtguildraid.models.Record;

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
    public abstract List<Record> get1MemberRecords(int memberId, int raidId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND round >= :round")
    public abstract List<Record> get1MemberRoundRecords(int memberId, int raidId, int round);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND day = :day")
    public abstract List<Record> get1DayRecords(int memberId, int raidId, int day);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND bossId = :bossId")
    public abstract List<Record> get1BossRecords(int memberId, int raidId, int bossId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND bossId = :bossId " +
            "AND round >= :round")
    public abstract List<Record> get1BossRoundRecords(int memberId, int raidId, int bossId, int round);

    @Query("SELECT * FROM Record WHERE raidId = :raidId AND bossId = :bossId")
    public abstract List<Record> getAllMemberBossRecords(int raidId, int bossId);

    @Query("SELECT * FROM Boss WHERE bossId = :bossId")
    public abstract Boss getBoss(int bossId);

    @Query("SELECT * FROM Hero WHERE heroId = :heroId")
    public abstract Hero getHero(int heroId);

    @Query("UPDATE Record SET damage = :damage, bossId = :bossId, round = :round, leaderId = :leaderId, isLastHit = :isLastHit" +
            " WHERE recordID = :rId")
    public abstract void updateRecord(int rId, int damage, int bossId, int round, int leaderId, boolean isLastHit);

    public List<Record> get1DayRecordsWithExtra(int memberId, int raidId, int day) {
        List<Record> records = get1DayRecords(memberId, raidId, day);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }


    public List<Record> getAllRecordsWithExtra(int raidId){
        List<Record> records = getAllRecords(raidId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }


    public List<Record> get1MemberRecordsWithExtra(int memberId, int raidId){
        List<Record> records = get1MemberRecords(memberId, raidId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }

    public List<Record> get1BossRecordsWithExtra(int raidId, int bossId) {
        List<Record> records = getAllMemberBossRecords(raidId, bossId);
        for(Record record: records){
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }

    public List<Record> get1MemberRecordsWithExtra(int memberId, int raidId, int bossId) {
        List<Record> records = get1BossRecords(memberId, raidId, bossId);
        for (Record record : records) {
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }

    public List<Record> get1MemberRoundRecordsWithExtra(int memberId, int raidId, int bossId, int round) {
        List<Record> records = get1BossRoundRecords(memberId, raidId, bossId, round);
        for (Record record : records) {
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }

    public List<Record> get1MemberRoundRecordsWithExtra(int memberId, int raidId, int round) {
        List<Record> records = get1MemberRoundRecords(memberId, raidId, round);
        for (Record record : records) {
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }
}
