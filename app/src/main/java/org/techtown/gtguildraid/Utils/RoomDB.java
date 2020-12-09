package org.techtown.gtguildraid.Utils;


import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

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
@Database(entities = {GuildMember.class, Boss.class, Raid.class, Hero.class, Record.class}, version = 5, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;

    private static String DATABASE_NAME = "database";

    public synchronized static RoomDB getInstance(Context context){
        if(database == null){//initialize
            database = Room.databaseBuilder(context.getApplicationContext()
            , RoomDB.class, DATABASE_NAME)
                    .createFromAsset("database/database.db")
                    .allowMainThreadQueries()
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
