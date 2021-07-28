package com.sksulai.checksite.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [
    WorkerModel::class
]) abstract class LocalDB : RoomDatabase() {
    companion object {
        const val name = "main-db"
    }

    abstract fun getWorkerDao(): WorkerDao
}
