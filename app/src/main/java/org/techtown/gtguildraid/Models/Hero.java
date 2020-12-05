package org.techtown.gtguildraid.Models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity (tableName = "hero")
public class Hero implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "heroId")
    private int heroId;

    @NonNull
    @ColumnInfo(name = "koreanName")
    private String koreanName;

    @NonNull
    @ColumnInfo(name = "englishName")
    private String englishName;

    @ColumnInfo(name = "element")
    private int element;

    @ColumnInfo(name = "star")
    private int star;

    @ColumnInfo(name = "role")
    private int role;

    public int getElement() {
        return element;
    }

    public void setElement(int element) {
        this.element = element;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
