package org.techtown.gtguildraid.Interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.techtown.gtguildraid.Models.Hero;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface HeroDao {
    @Insert(onConflict = REPLACE)
    void insert(Hero hero);

    @Delete
    void delete(Hero hero);

    @Delete
    void reset(List<Hero> heroes);

    @Query("SELECT * FROM Hero")
    List<Hero> getAllHeroes();

    @Query("SELECT * FROM Hero WHERE heroId = :heroId")
    Hero getHero(int heroId);

    @Query("SELECT * FROM Hero WHERE element = :element ORDER BY star DESC")
    List<Hero> getHeroesWithElement(int element);
}
