package org.techtown.gtguildraid.utils;


import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.techtown.gtguildraid.interfaces.BossDao;
import org.techtown.gtguildraid.interfaces.FavoritesDao;
import org.techtown.gtguildraid.interfaces.HeroDao;
import org.techtown.gtguildraid.interfaces.MemberDao;
import org.techtown.gtguildraid.interfaces.RaidDao;
import org.techtown.gtguildraid.interfaces.RecordDao;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Favorites;
import org.techtown.gtguildraid.models.GuildMember;
import org.techtown.gtguildraid.models.Hero;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.models.Record;



//Add database entities
@Database(entities = {GuildMember.class, Boss.class, Raid.class, Hero.class, Record.class, Favorites.class}, version = 28, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {//hero 데이터 삭제
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Record_backup "
                    + "(recordId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, memberId INTEGER NOT NULL, raidId INTEGER NOT NULL, " +
                    "day INTEGER NOT NULL, bossId INTEGER NOT NULL, level INTEGER NOT NULL, damage INTEGER NOT NULL)");
            database.execSQL("INSERT INTO Record_backup "
            + "SELECT recordId, memberId, raidId, day, bossId, level, damage FROM Record");
            database.execSQL("DROP TABLE Record");
            database.execSQL("ALTER TABLE Record_backup RENAME TO Record");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {//leader만 재생성
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Record " +
                    "ADD COLUMN leaderId INTEGER NOT NULL DEFAULT 1");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {//boss의 imageId를 imgName으로 교체
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Boss_backup "
                    + "(bossId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, raidId INTEGER NOT NULL," +
                    " name TEXT, hardness REAL NOT NULL)");
            database.execSQL("INSERT INTO Boss_backup "
                    + "SELECT bossId, raidId, name, hardness FROM Boss");
            database.execSQL("DROP TABLE Boss");
            database.execSQL("ALTER TABLE Boss_backup RENAME TO Boss");
            database.execSQL("ALTER TABLE Boss " +
                    "ADD COLUMN imgName TEXT DEFAULT '1'");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {//record의 level을 round로 교체
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Record_backup "
                    + "(recordId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, memberId INTEGER NOT NULL, raidId INTEGER NOT NULL, " +
                    "day INTEGER NOT NULL, bossId INTEGER NOT NULL, leaderId INTEGER NOT NULL, damage INTEGER NOT NULL)");
            database.execSQL("INSERT INTO Record_backup "
                    + "SELECT recordId, memberId, raidId, day, bossId, leaderId, damage FROM Record");
            database.execSQL("DROP TABLE Record");
            database.execSQL("ALTER TABLE Record_backup RENAME TO Record");
            database.execSQL("ALTER TABLE Record " +
                    "ADD COLUMN round INTEGER NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE Record " +
                    "ADD COLUMN isLastHit INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {//favorites 새로 생성
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Favorites " +
                    "(heroId INTEGER PRIMARY KEY NOT NULL)");
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {//favorites의 데이터 변경
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Favorites_backup " +
                    "(favoritesId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "heroId INTEGER NOT NULL)");
            database.execSQL("INSERT INTO Favorites_backup(heroId) "
                    + "SELECT heroId FROM Favorites");
            database.execSQL("DROP TABLE Favorites");
            database.execSQL("ALTER TABLE Favorites_backup RENAME TO Favorites");
        }
    };

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('네바', 'neva', 4, 2, 1), "
                    + "('루', 'rue', 4, 3, 4), "
                    + "('가브리엘', 'gabriel', 4, 3, 4)");
        }
    };

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {//루 데이터 고치기
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("UPDATE hero SET element = 3, role = 1 WHERE heroId = 50");
        }
    };

    static final Migration MIGRATION_13_14 = new Migration(13, 14) {//린 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('린', 'rin', 1, 3, 1)");
        }
    };

    static final Migration MIGRATION_14_15 = new Migration(14, 15) {//미기 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('미래 기사', 'futureknight', 6, 3, 1)");
        }
    };

    //녹시아 데이터 업데이트 및 GuildMember에 CASCADE 추가 - 2021.04.02
    static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('녹시아', 'noxia', 5, 3, 4)");

            database.execSQL("CREATE TABLE Record_backup "
                    + "(recordId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "memberId INTEGER NOT NULL, " +
                    "raidId INTEGER NOT NULL, " +
                    "day INTEGER NOT NULL, " +
                    "bossId INTEGER NOT NULL, " +
                    "leaderId INTEGER NOT NULL, " +
                    "damage INTEGER NOT NULL, " +
                    "round INTEGER NOT NULL, " +
                    "isLastHit INTEGER NOT NULL, " +
                    "FOREIGN KEY(memberId) REFERENCES member(ID) ON UPDATE NO ACTION ON DELETE CASCADE," +
                    "FOREIGN KEY(raidId) REFERENCES raid(raidId) ON UPDATE NO ACTION ON DELETE CASCADE) ");
            database.execSQL("INSERT INTO Record_backup "
                    + "SELECT recordId, memberId, raidId, day, bossId, leaderId, damage, round, isLastHit FROM Record");
            database.execSQL("DROP TABLE Record");
            database.execSQL("ALTER TABLE Record_backup RENAME TO Record");
        }
    };

    //보스 정보에 element 정보 추가 - 2021.04.02
    static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Boss " +
                    "ADD COLUMN elementId INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE INDEX " +
                    "index_record_raid ON Record(raidId)");
            database.execSQL("CREATE INDEX " +
                    "index_record_memberId ON Record(memberId)");
        }
    };

    //메이릴 데이터 업데이트  - 2021.04.09
    static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('메이릴', 'meiril', 4, 3, 4)");
        }
    };

    //메이릴 데이터 고치기 - 2021.04.09
    static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("UPDATE hero SET element = 3 WHERE englishName = 'meiril'");
        }
    };

    //막뀨 데이터 삽입
    static final Migration MIGRATION_20_21 = new Migration(20, 21) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('Mk.99', 'mk99', 4, 3, 3)");
        }
    };

    //베로니카, 리리스 데이터 삽입 - 2021.05.15
    static final Migration MIGRATION_21_22 = new Migration(21, 22) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('베로니카', 'veronica', 2, 3, 4)," +
                    " ('리리스', 'lilith', 5, 3, 1)");
        }
    };

    //루시, 소히, 유즈 데이터 삽입 - 2021.06.27
    static final Migration MIGRATION_22_23 = new Migration(22, 23) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('루시', 'lucy', 1, 3, 3), "
                    + "('소히 수영복', 'beachsohee', 6, 3, 1), "
                    + "('유즈 수영복', 'beachyuze', 2, 3, 1)");
        }
    };

    //엘레노아 데이터 추가, 보스 isFurious 추가 - 2021.07.06
    static final Migration MIGRATION_23_24 = new Migration(23, 24) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('엘레노아', 'eleanor', 4, 3, 4)");
            database.execSQL("ALTER TABLE Boss " +
                    "ADD COLUMN isFurious INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_24_25 = new Migration(24, 25) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Raid " +
                    "ADD COLUMN thumbnail TEXT DEFAULT 'knight'");
        }
    };

    static final Migration MIGRATION_25_26 = new Migration(25, 26) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Boss_backup "
                    + "(bossId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "raidId INTEGER NOT NULL, " +
                    "name TEXT, " +
                    "hardness REAL NOT NULL," +
                    "imgName TEXT," +
                    "elementId INTEGER NOT NULL," +
                    "isFurious INTEGER NOT NULL, " +
                    "FOREIGN KEY(raidId) REFERENCES raid(raidId) ON UPDATE NO ACTION ON DELETE CASCADE) ");
            database.execSQL("INSERT INTO Boss_backup "
                    + "SELECT bossId, raidId, name, hardness, imgName, elementId, isFurious FROM Boss");
            database.execSQL("DROP TABLE Boss");
            database.execSQL("ALTER TABLE Boss_backup RENAME TO Boss");
            database.execSQL("CREATE INDEX index_boss_raid ON Boss(raidId)");
        }
    };

    static final Migration MIGRATION_26_27 = new Migration(26, 27) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('신틸라', 'scintilla', 1, 3, 1)");
        }
    };

    static final Migration MIGRATION_27_28 = new Migration(27, 28) {//hero 데이터 업데이트
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('에리나', 'erina', 6, 3, 2)");
            database.execSQL("INSERT INTO hero (koreanName, englishName, element, star, role) "
                    + "VALUES ('카마엘', 'camael', 3, 3, 4)");
        }
    };

    //카마엘 데이터 고치기 - 2021.08.29
    static final Migration MIGRATION_28_29 = new Migration(28, 29) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("UPDATE hero SET englishName = 'kamael' WHERE englishName = 'camael'");
        }
    };

    //카마엘 데이터 고치기 - 2021.08.29
    static final Migration MIGRATION_29_28 = new Migration(29, 28) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("UPDATE hero SET englishName = 'camael' WHERE englishName = 'kamael'");
        }
    };


    public synchronized static RoomDB getInstance(Context context){//Singleton Pattern!
        if(database == null){//initialize
            String DATABASE_NAME = "database";
            database = Room.databaseBuilder(context.getApplicationContext()
            , RoomDB.class, DATABASE_NAME)
                    .createFromAsset("database/database.db")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .addMigrations(MIGRATION_7_8)
                    .addMigrations(MIGRATION_8_9)
                    .addMigrations(MIGRATION_9_10)
                    .addMigrations(MIGRATION_10_11)
                    .addMigrations(MIGRATION_11_12)
                    .addMigrations(MIGRATION_12_13)
                    .addMigrations(MIGRATION_13_14)
                    .addMigrations(MIGRATION_14_15)
                    .addMigrations(MIGRATION_15_16)
                    .addMigrations(MIGRATION_16_17)
                    .addMigrations(MIGRATION_17_18)
                    .addMigrations(MIGRATION_18_19)
                    .addMigrations(MIGRATION_19_20)
                    .addMigrations(MIGRATION_20_21)
                    .addMigrations(MIGRATION_21_22)
                    .addMigrations(MIGRATION_22_23)
                    .addMigrations(MIGRATION_23_24)
                    .addMigrations(MIGRATION_24_25)
                    .addMigrations(MIGRATION_25_26)
                    .addMigrations(MIGRATION_26_27)
                    .addMigrations(MIGRATION_27_28)
                    .addMigrations(MIGRATION_28_29)
                    .addMigrations(MIGRATION_29_28)
                    .build();
        }
        else{
            Log.d("databaseName", database.toString());
        }
        return database;
    }

    //Create Dao
    public abstract MemberDao memberDao();
    public abstract RaidDao raidDao();
    public abstract RecordDao recordDao();
    public abstract HeroDao heroDao();
    public abstract FavoritesDao favoritesDao();
    public abstract BossDao bossDao();
}
