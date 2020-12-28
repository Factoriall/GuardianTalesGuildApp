package org.techtown.gtguildraid.Utils;


import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.techtown.gtguildraid.Interfaces.HeroDao;
import org.techtown.gtguildraid.Interfaces.MemberDao;
import org.techtown.gtguildraid.Interfaces.RaidDao;
import org.techtown.gtguildraid.Interfaces.RecordDao;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.Models.Record;



//Add database entities
@Database(entities = {GuildMember.class, Boss.class, Raid.class, Hero.class, Record.class}, version = 7, exportSchema = false)
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

    public synchronized static RoomDB getInstance(Context context){
        if(database == null){//initialize
            database = Room.databaseBuilder(context.getApplicationContext()
            , RoomDB.class, DATABASE_NAME)
                    .createFromAsset("database/database.db")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
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
}
