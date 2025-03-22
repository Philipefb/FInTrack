package com.devspace.fintrack

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {

    @Query("Select * from taskentity")
    fun getAll(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(taskEntities: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(taskEntity: TaskEntity)

    @Delete
    fun delete(taskEntity: TaskEntity)

    @Query("Select * from taskentity where category is :categoryName")
    fun getAllByCategory(categoryName: String): List<TaskEntity>

    @Query("SELECT SUM(price) FROM taskentity")
    fun getAllTaskPrice(): Float

    @Delete
    fun deleteAll(taskEntity: List<TaskEntity>)



}