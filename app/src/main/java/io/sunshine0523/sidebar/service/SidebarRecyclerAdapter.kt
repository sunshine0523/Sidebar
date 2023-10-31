package io.sunshine0523.sidebar.service

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.databinding.ItemAppBinding

/**
 * @author KindBrave
 * @since 2023/9/26
 */
class SidebarRecyclerAdapter(
    private val callback: Callback
) : RecyclerView.Adapter<SidebarRecyclerAdapter.ViewHolder>() {

    private lateinit var context: Context
    //侧边栏应用分为两部分：用户添加的应用和最近打开的应用
    private val appList = ArrayList<AppInfo>()
    private val sidebarAppList = ArrayList<AppInfo>()
    private val recentAppList = ArrayList<AppInfo>()

    inner class ViewHolder(binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        val binding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.icon.setImageDrawable(appList[position].icon)
        holder.binding.icon.setOnClickListener {
            callback.onClick(appList[position])
        }
    }

    fun updateSidebarAppList(appList: List<AppInfo>) {
        this.sidebarAppList.apply {
            clear()
            addAll(appList)
        }
        this.appList.apply {
            clear()
            addAll(sidebarAppList)
            addAll(recentAppList)
        }
        notifyDataSetChanged()
    }

    fun updateRecentAppList(appList: List<AppInfo>) {
        this.recentAppList.apply {
            clear()
            addAll(appList)
        }
        this.appList.apply {
            clear()
            addAll(sidebarAppList)
            addAll(recentAppList)
        }
        notifyDataSetChanged()
    }

    interface Callback {
        fun onClick(appInfo: AppInfo)
    }
}