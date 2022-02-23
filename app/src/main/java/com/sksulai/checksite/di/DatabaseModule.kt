package com.sksulai.checksite.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

import android.content.Context
import androidx.room.Room

import com.sksulai.checksite.db.LocalDB
import com.sksulai.checksite.db.WorkerDao
import com.sksulai.checksite.db.WorkerRepo

@InstallIn(ActivityComponent::class)
@Module object DatabaseModule {
    @Provides fun provideDatabase(context: Context) =
        Room.databaseBuilder(
            context,
            LocalDB::class.java, LocalDB.name
        ).build()
    @Provides fun provideWorkerDao(db: LocalDB) = db.getWorkerDao()
}
