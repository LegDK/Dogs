package ru.maltsev.dogs.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DogBreed::class], version = 1)
abstract class DogDatabase: RoomDatabase() {
    abstract fun dogDao(): DogDao

    companion object {
        @Volatile private var instance: DogDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            DogDatabase::class.java,
            "dogdatabase"
        ).build()

    }
}

//@Database(entities = [DogBreed::class], version = 1, exportSchema = false)
//abstract class DogDatabase : RoomDatabase() {
//
//    abstract fun dogDao(): DogDao
//
//    companion object {
//        private val INSTANCE: DogDatabase? = null
//
//        @Synchronized
//        fun getInstance(context: Context): DogDatabase {
//            var instance = INSTANCE
//
//            if (instance == null) {
//                instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    DogDatabase::class.java,
//                    "dogdatabase"
//                ).fallbackToDestructiveMigration()
//                    .build()
//            }
//
//            return instance
//        }
//    }
//}