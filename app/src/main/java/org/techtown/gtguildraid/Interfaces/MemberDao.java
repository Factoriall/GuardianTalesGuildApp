package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.GuildMember;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MemberDao {
    //Insert query
    @Insert(onConflict = REPLACE)
    void insert(GuildMember member);

    @Delete
    void delete(GuildMember member);

    @Delete
    void reset(List<GuildMember> members);

    @Query("UPDATE member SET name = :sName, remark = :sRemark WHERE ID = :sID")
    void update(int sID, String sName, String sRemark);

    @Query("UPDATE member SET isResigned = :flag WHERE ID = :sID")
    void setIsResigned(int sID, boolean flag);

    @Query("SELECT * FROM member WHERE isResigned = 0 AND isMe = 0")
    List<GuildMember> getCurrentMembers();

    @Query("SELECT * FROM member WHERE isResigned = 1 AND isMe = 0")
    List<GuildMember> getResignedMembers();
}
