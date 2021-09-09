package org.techtown.gtguildraid.interfaces.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.models.entities.Boss;
import org.techtown.gtguildraid.models.entities.Hero;
import org.techtown.gtguildraid.models.IdDouble;
import org.techtown.gtguildraid.models.IdLong;
import org.techtown.gtguildraid.models.IdLongCnt;
import org.techtown.gtguildraid.models.entities.Record;

import java.util.List;

@Dao
public abstract class RecordDao {
    @Insert
    public abstract void insertRecord(Record record);

    @Delete
    public abstract void deleteRecord(Record record);

    @Query("SELECT * FROM Record WHERE raidId = :raidId ORDER BY memberId")
    public abstract List<Record> getAllRecords(int raidId);

    @Query("SELECT SUM(damage) FROM Record WHERE raidId = :raidId")
    public abstract long getTotalDamageInRaid(int raidId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId")
    public abstract List<Record> get1MemberRecords(int memberId, int raidId);

    @Query("SELECT leaderId FROM Record WHERE memberId = :memberId AND raidId = :raidId AND bossId = :bossId " +
            "GROUP BY leaderId ORDER BY COUNT(*) DESC")
    public abstract List<Integer> getLeaderIdsDesc(int memberId, int raidId, int bossId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND round >= :round AND day >= :day")
    public abstract List<Record> get1MemberRoundRecords(int memberId, int raidId, int round, int day);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND day = :day")
    public abstract List<Record> get1DayRecords(int memberId, int raidId, int day);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND bossId = :bossId")
    public abstract List<Record> get1BossRecords(int memberId, int raidId, int bossId);

    @Query("SELECT * FROM Record WHERE memberId = :memberId AND raidId = :raidId AND bossId = :bossId " +
            "AND round >= :round AND day >= :day")
    public abstract List<Record> get1BossRoundRecords(int memberId, int raidId, int bossId, int round, int day);

    @Query("SELECT SUM(damage) FROM Record WHERE raidId = :raidId AND bossId = :bossId " +
            "AND round = :round")
    public abstract long get1Boss1RoundSum(int raidId, int bossId, int round);

    @Query("SELECT * FROM Record WHERE raidId = :raidId AND bossId = :bossId " +
            "AND round = :round AND isLastHit = 1")
    public abstract Record get1Boss1RoundLastHit(int raidId, int bossId, int round);

    @Query("SELECT * FROM Record WHERE raidId = :raidId AND bossId = :bossId")
    public abstract List<Record> getAllMemberBossRecords(int raidId, int bossId);

    @Query("SELECT * FROM Boss WHERE bossId = :bossId")
    public abstract Boss getBoss(int bossId);

    @Query("SELECT * FROM Hero WHERE heroId = :heroId")
    public abstract Hero getHero(int heroId);

/*
    @Query("SELECT " +
            "(SELECT count(*) + 1 FROM " +
            "(SELECT memberId, SUM(CASE WHEN isLastHit == 1 THEN (damage * :lastHitValue) ELSE (damage) END) " +
            "as total from Record WHERE raidId = :raidId AND day >= :start GROUP BY memberId) t2 " +
            "WHERE t2.total > t1.total)" +
            " FROM (SELECT memberId, SUM(CASE WHEN isLastHit == 1 THEN (damage * :lastHitValue) ELSE (damage) END) as total from Record " +
            "WHERE raidId = :raidId AND day >= :start GROUP BY memberId) t1 " +
            " WHERE memberId = :memberId")
    public abstract int getRankFromAllRecords(int memberId, int raidId, int start, double lastHitValue);
*/

    @Query("WITH tt (memberId, total) as " +
            "(SELECT memberId, SUM(CASE WHEN isLastHit == 1 THEN (damage * :lastHitValue) ELSE (damage) END) as total " +
            "FROM Record WHERE raidId = :raidId AND day >= :start GROUP BY memberId) " +
            "SELECT rank FROM (SELECT s.memberId, s.total, (1 + COUNT(lesser.total)) AS rank FROM tt as s " +
            "LEFT JOIN tt as lesser " +
            "ON s.total < lesser.total " +
            "GROUP BY s.memberId " +
            "ORDER BY s.total DESC)" +
            "WHERE memberId = :memberId")
    public abstract int getRankFromAllRecords(int memberId, int raidId, int start, double lastHitValue);

    @Query("WITH tt (memberId, total) as" +
            "(SELECT memberId, SUM(CASE WHEN isLastHit == 1 THEN (damage * hardness * :lastHitValue) ELSE (damage * hardness) END) as total " +
            "FROM Record INNER JOIN Boss on Record.bossId = Boss.bossId " +
            "WHERE Record.raidId = :raidId AND Record.day >= :start GROUP BY memberId) " +
            "SELECT rank FROM (SELECT s.memberId, s.total, (1 + COUNT(lesser.total)) AS rank FROM tt as s " +
            "LEFT JOIN tt as lesser " +
            "ON s.total < lesser.total " +
            "GROUP BY s.memberId " +
            "ORDER BY s.total DESC) " +
            "WHERE memberId = :memberId")
    public abstract int getRankFromAllAdjustRecords(int memberId, int raidId, int start, double lastHitValue);

    @Query("WITH tt (memberId, total) as" +
            "(SELECT memberId, SUM(damage) as total " +
            "FROM Record WHERE raidId = :raidId AND bossId = :bossId GROUP BY memberId) " +
            "SELECT rank FROM (SELECT s.memberId, s.total, (1 + COUNT(lesser.total)) AS rank FROM tt as s " +
            "LEFT JOIN tt as lesser " +
            "ON s.total < lesser.total " +
            "GROUP BY s.memberId " +
            "ORDER BY s.total DESC) " +
            "WHERE memberId = :memberId")
    public abstract int getTotalRankFromBossRecords(int memberId, int raidId, int bossId);

    @Query("WITH tt (memberId, total) as" +
            "(SELECT memberId, AVG(damage) as total " +
            "FROM Record WHERE raidId = :raidId AND bossId = :bossId GROUP BY memberId) " +
            "SELECT rank FROM (SELECT s.memberId, s.total, (1 + COUNT(lesser.total)) AS rank FROM tt as s " +
            "LEFT JOIN tt as lesser " +
            "ON s.total < lesser.total " +
            "GROUP BY s.memberId " +
            "ORDER BY s.total DESC) " +
            "WHERE memberId = :memberId")
    public abstract int getAvgRankFromBossRecords(int memberId, int raidId, int bossId);

    @Query("SELECT AVG(damage) FROM Record " +
            "WHERE raidId = :raidId AND leaderId = :leaderId " +
            "AND bossId = :bossId AND round >= :round AND isLastHit = 0")
    public abstract long getAverageOfLeaderFromRecords(int raidId, int bossId, int leaderId, int round);

    @Query("SELECT AVG(damage) FROM Record " +
            "WHERE raidId = :raidId AND leaderId = :leaderId AND memberId = :memberId " +
            "AND bossId = :bossId AND round >= :round AND isLastHit = 0")
    public abstract long getAverageOfMyLeader(int raidId, int memberId, int bossId, int leaderId, int round);

    @Query("SELECT MAX(round) FROM Record " +
            "WHERE raidId = :raidId")
    public abstract int getMaxRound(int raidId);

    @Query("SELECT SUM(damage) FROM Record " +
            "WHERE raidId = :raidId AND round = :round")
    public abstract long getMaxRoundRecordSum(int raidId, int round);

    @Query("SELECT memberId, " +
            "SUM(CASE WHEN isLastHit == 1 THEN (damage * hardness * :lastHitValue) " +
            "ELSE (damage * hardness) END) as value " +
            "from Record INNER JOIN Boss on Record.bossId = Boss.bossId " +
            "WHERE Record.raidId = :raidId AND Record.day >= :start AND Record.day <= :end " +
            "GROUP BY memberId ORDER BY value DESC LIMIT 30")
    public abstract List<IdLong> getRanksOfAdjustRecords(int raidId, int start, int end, double lastHitValue);

    @Query("SELECT memberId, " +
            "SUM(CASE WHEN isLastHit == 1 THEN (damage * :lastHitValue) " +
            "ELSE damage END) as value " +
            "from Record INNER JOIN Boss on Record.bossId = Boss.bossId " +
            "WHERE Record.raidId = :raidId AND Record.day >= :start AND Record.day <= :end " +
            "GROUP BY memberId ORDER BY value DESC LIMIT 30")
    public abstract List<IdLong> getRanksOfNormalRecords(int raidId, int start, int end, double lastHitValue);

    @Query("SELECT memberId, SUM(damage) AS value " +
            "FROM Record WHERE raidId = :raidId AND bossId = :bossId AND day >= :start AND day <= :end " +
            "GROUP BY memberId ORDER BY value DESC LIMIT 5")
    public abstract List<IdLong> getRanksOfBossTotal(int raidId, int bossId, int start, int end);

    @Query("SELECT SUM(damage) " +
            "FROM Record WHERE raidId = :raidId AND bossId = :bossId AND day >= :start AND day <= :end")
    public abstract long getTotalOfBoss(int raidId, int bossId, int start, int end);

    @Query("SELECT memberId, COUNT(*) as value FROM Record " +
            "WHERE raidId = :raidId AND isLastHit = 1 AND day >= :start AND day <= :end " +
            "GROUP BY memberId ORDER BY value DESC LIMIT 5")
    public abstract List<IdLong> getRanksOfLastHit(int raidId, int start, int end);

    @Query("SELECT memberId, (SUM(damage * hardness * 1.0) / SUM(damage)) - 1.0 as value" +
            " from Record INNER JOIN Boss on Record.bossId = Boss.bossId " +
            "WHERE Record.raidId = :raidId AND Record.day >= :start AND Record.day <= :end " +
            "GROUP BY memberId ORDER BY value DESC LIMIT 5")
    public abstract List<IdDouble> getRanksOfAdjustGrowth(int raidId, int start, int end);

    @Query("SELECT memberId, AVG(damage) as value, COUNT(damage) as count FROM Record " +
            "WHERE raidId = :raidId AND bossId = :bossId AND isLastHit = 0 AND day >= :start AND day <= :end " +
            "GROUP BY memberId HAVING count(memberId) >= :cnt ORDER BY value DESC LIMIT 5")
    public abstract List<IdLongCnt> getRanksOfBossAverage(int raidId, int bossId, int cnt, int start, int end);

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

    public List<Record> get1MemberRoundRecordsWithExtra(int memberId, int raidId, int bossId, int round, int day) {
        List<Record> records = get1BossRoundRecords(memberId, raidId, bossId, round, day);
        for (Record record : records) {
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }

    public List<Record> get1MemberRoundRecordsWithExtra(int memberId, int raidId, int round, int day) {
        List<Record> records = get1MemberRoundRecords(memberId, raidId, round, day);
        for (Record record : records) {
            Boss boss = getBoss(record.getBossId());
            record.setBoss(boss);

            Hero hero = getHero(record.getLeaderId());
            record.setLeader(hero);
        }

        return records;
    }
}
