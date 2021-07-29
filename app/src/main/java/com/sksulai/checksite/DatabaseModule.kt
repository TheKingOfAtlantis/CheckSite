package com.sksulai.checksite

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

import android.content.Context
import androidx.room.Room

import com.sksulai.checksite.db.LocalDB
import com.sksulai.checksite.db.WorkerDao
import com.sksulai.checksite.db.WorkerRepo
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(
    SingletonComponent::class,
    ActivityComponent::class,
    ViewModelComponent::class,
) @Module object DatabaseModule {
    @Provides fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            LocalDB::class.java, LocalDB.name
        ).build()
    @Provides fun provideWorkerDao(db: LocalDB) = db.getWorkerDao()
}

@InstallIn(
    SingletonComponent::class,
    ActivityComponent::class,
    ViewModelComponent::class,
) @Module object RepositoryModule {
    @Provides fun provideWorkerRepo(
        @ApplicationContext context: Context,
        dao: WorkerDao
    ) = WorkerRepo(context, dao)
}
