package ru.maltsev.dogs.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DogDao {

    @Insert
    suspend fun insertAll(vararg dogBreed: DogBreed): List<Long>

    @Query("select * from dogbreed")
    suspend fun getAllGods(): List<DogBreed>

    @Query("select * from dogbreed where uuid =:dogId")
    suspend fun getDog(dogId: Int): DogBreed

    @Query("delete from dogbreed")
    suspend fun deleteAllDogs()

    @Query("select * from dogbreed where dog_name like :searchQuery")
    suspend fun getFilteredDogs(searchQuery: String): List<DogBreed>
}