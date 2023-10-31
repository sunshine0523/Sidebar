package io.sunshine0523.sidebar.ui.all_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.bean.SidebarAppInfo
import io.sunshine0523.sidebar.databinding.ItemAllAppBinding
import io.sunshine0523.sidebar.databinding.ItemSidebarAppBinding
import me.zhanghai.android.appiconloader.AppIconLoader

/**
 * @author KindBrave
 * @since 2023/10/21
 */
class AllAppRecyclerView(private val callback: Callback):
    RecyclerView.Adapter<AllAppRecyclerView.ViewHolder>() {

    private var appList: List<AppInfo>? = null
    private lateinit var appIconLoader: AppIconLoader

    inner class ViewHolder(dataBinding: ItemAllAppBinding): RecyclerView.ViewHolder(dataBinding.root) {
        val binding = dataBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        appIconLoader = AppIconLoader(100, false, parent.context)
        return ViewHolder(ItemAllAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return appList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = appList!![position]
        holder.binding.icon.setImageDrawable(item.icon)
        holder.binding.appName.text = item.label
        holder.binding.root.setOnClickListener {
            callback.onClick(item.packageName, item.activityName, item.userId)
        }
    }

    fun updateList(appList: List<AppInfo>) {
        this.appList = appList
        notifyDataSetChanged()
    }

    interface Callback {
        fun onClick(packageName: String, activityName: String, userId: Int)
    }
}