package org.techtown.gtguildraid.Utils;


import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.techtown.gtguildraid.Interfaces.FavoritesDao;
import org.techtown.gtguildraid.Interfaces.HeroDao;
import org.techtown.gtguildraid.Interfaces.MemberDao;
import org.techtown.gtguildraid.Interfaces.RaidDao;
import org.techtown.gtguildraid.Interfaces.RecordDao;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Favorites;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.Models.Record;



//Add database entities
@Database(entities = {GuildMember.class, Boss.class, Raid.class, Hero.class, Record.class, Favorites.class}, version = 11, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;

    private static String DATABASE_NAME = "database";

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

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {//favorites의 
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

    public synchronized static RoomDB getInstance(Context context){
        if(database == null){//initialize
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
}
