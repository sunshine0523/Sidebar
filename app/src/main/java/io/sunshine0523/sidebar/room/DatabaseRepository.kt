package io.sunshine0523.sidebar.room

import android.content.Context
import androidx.lifecycle.LiveData
import io.sunshine0523.sidebar.room.MyDatabase.Companion.getDatabase
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

/**
 * @author sunshine
 * @date 2021/1/31
 */
class DatabaseRepository(context: Context) {

    private val sidebarAppsDao: SidebarAppsDao

    fun insertSidebarApp(packageName: String, activityName: String, userId: Int) {
        try {
            sidebarAppsDao.insert(packageName, activityName, userId)
        }catch (e: Exception) { }
    }

    fun deleteSidebarApp(packageName: String, activityName: String, userId: Int) {
        sidebarAppsDao.delete(packageName, activityName, userId)
    }

    fun getAllSidebarName(): LiveData<List<String>?> {
        return sidebarAppsDao.getAllName()
    }

    fun getAllSidebar() : LiveData<List<SidebarAppsEntity>?> {
        return sidebarAppsDao.getAll()
    }

    fun getAllSidebarAppsByFlow(): Flow<List<SidebarAppsEntity>?> {
        return sidebarAppsDao.getAllByFlow()
    }

    fun getCount(): Int {
        return sidebarAppsDao.getCount()
    }

    fun update(entity: SidebarAppsEntity) {
        sidebarAppsDao.update(entity)
    }

    fun getAllSidebarWithoutLiveData() : List<SidebarAppsEntity>? {
        return sidebarAppsDao.getAllWithoutLiveData()
    }

    fun deleteAllSidebar() {
        sidebarAppsDao.deleteAll()
    }

    fun deleteMore(sidebarAppsEntityList: List<SidebarAppsEntity>) {
        sidebarAppsDao.deleteList(sidebarAppsEntityList)
    }

    init {
        val database = getDatabase(context)
        sidebarAppsDao = database.sidebarAppsDao
    }
}