package org.techtown.gtguildraid.Utils;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.techtown.gtguildraid.Interfaces.BossDao;
import org.techtown.gtguildraid.Interfaces.MainDao;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.GuildMember;

//Add database entities
@Database(entities = {GuildMember.class, Boss.class}, version = 2, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;

    private static String DATABASE_NAME = "database";

    public synchronized static RoomDB getInstance(Context context){
        if(database == null){//initialize
            database = Room.databaseBuilder(context.getApplicationContext()
            , RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    //Create Dao
    public abstract MainDao memberDao();
    public abstract BossDao bossDao();
}
