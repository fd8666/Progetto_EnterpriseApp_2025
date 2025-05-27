package com.example.eventra
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Per il Testing offline con room
//@Database(entities = [Nome::class], version = 4 , exportSchema = false)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun nomeDAO(): NomeDAO
//
//    companion object {
//        @Volatile
//        private var instance: AppDatabase? = null
//        fun getInstance(context: Context): AppDatabase {
//            synchronized(this) {
//                var localInstance = instance
//                if (localInstance == null) {
//                    localInstance = Room.databaseBuilder(
//                        context.applicationContext,
//                        AppDatabase::class.java,
//                        "EA"
//                    ).fallbackToDestructiveMigration().build()
//                    instance = localInstance
//                }
//                return localInstance
//            }
//        }
//    }
//}