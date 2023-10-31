package io.sunshine0523.sidebar.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * @author sunshine
 * @date 2021/1/31
 */
@Dao
interface SidebarAppsDao {

    @Query("INSERT INTO SidebarAppsEntity(packageName, activityName, userId) VALUES(:packageName, :activityName, :userId)")
    fun insert(packageName: String, activityName: String, userId: Int)

    @Query("DELETE FROM SidebarAppsEntity WHERE packageName = :packageName and activityName = :activityName and userId = :userId")
    fun delete(packageName: String, activityName: String, userId: Int)

    @Query("SELECT * FROM SidebarAppsEntity")
    fun getAll() : LiveData<List<SidebarAppsEntity>?>

    @Query("SELECT * FROM SidebarAppsEntity")
    fun getAllByFlow() : Flow<List<SidebarAppsEntity>?>

    @Query("SELECT packageName FROM SidebarAppsEntity")
    fun getAllName() : LiveData<List<String>?>

    @Query("SELECT * FROM SidebarAppsEntity")
    fun getAllWithoutLiveData() : List<SidebarAppsEntity>?

    @Query("SELECT COUNT(*) FROM SidebarAppsEntity")
    fun getCount(): Int

    @Query("DELETE FROM SidebarAppsEntity")
    fun deleteAll()

    @Delete
    fun deleteList(sidebarAppsEntityList: List<SidebarAppsEntity>)

    @Update
    fun update(entity: SidebarAppsEntity)
}