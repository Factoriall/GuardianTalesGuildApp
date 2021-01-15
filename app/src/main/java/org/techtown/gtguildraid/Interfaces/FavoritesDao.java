package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.Favorites;
import org.techtown.gtguildraid.Models.Hero;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public abstract class FavoritesDao {
    @Insert(onConflict = IGNORE)
    public abstract void insert(Favorites f);

    @Delete
    public abstract void delete(Favorites f);

    @Query("SELECT * FROM Favorites")
    public abstract List<Favorites> getAllFavorites();

    @Query("SELECT * FROM Hero WHERE heroId = :heroId")
    public abstract Hero getHero(int heroId);

    public List<Favorites> getAllFavoritesAndHero(){
        List<Favorites> list = getAllFavorites();
        for(Favorites fav : list){
            Hero hero = getHero(fav.getHeroId());
            fav.setHero(hero);
        }
        return list;
    }
}
