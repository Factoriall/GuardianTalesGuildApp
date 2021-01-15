package org.techtown.gtguildraid.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "favorites")
public class Favorites implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int favoritesId;

    private int heroId;

    @Ignore
    public Hero hero;

    public Favorites(int heroId){
        this.heroId = heroId;
    }

    public int getFavoritesId() {
        return favoritesId;
    }

    public void setFavoritesId(int favoritesId) {
        this.favoritesId = favoritesId;
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
