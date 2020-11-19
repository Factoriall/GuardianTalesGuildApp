package org.techtown.gtguildraid;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MainDao {
    //Insert query
    @Insert(onConflict = REPLACE)
    void insert(GuildMember member);

    @Delete
    void delete(GuildMember member);

    @Delete
    void reset(List<GuildMember> members);

    @Query("UPDATE member SET name = :sName, remark = :sRemark WHERE ID = :sID")
    void update(int sID, String sName, String sRemark);

    @Query("SELECT * FROM member")
    List<GuildMember> getAll();
}
