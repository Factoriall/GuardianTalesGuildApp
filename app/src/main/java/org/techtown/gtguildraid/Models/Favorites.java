package org.techtown.gtguildraid.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "favorites")
public class Favorites implements Serializable {
    @PrimaryKey
    private int heroId;

    @Ignore
    public Hero hero;

    public Favorites(int heroId){
        this.heroId = heroId;
    }

    public int getHeroId() {
        return heroId;
    }

    public Hero getHero() {
        return hero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }
}
