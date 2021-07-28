package com.sksulai.checksite.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface WorkerDao {
    @Insert suspend fun insert(work: WorkerModel)
    @Update suspend fun update(work: WorkerModel)
    @Delete suspend fun delete(work: WorkerModel)

    @Query("Select * From WorkerModel") fun getAll(): Flow<List<WorkerModel>>
    @Query("Select * From WorkerModel Where name == :name") fun getByName(name: String): Flow<WorkerModel?>
    @Query("Select * From WorkerModel Where name == :url") fun getByUrl(url: String): Flow<WorkerModel?>
}
