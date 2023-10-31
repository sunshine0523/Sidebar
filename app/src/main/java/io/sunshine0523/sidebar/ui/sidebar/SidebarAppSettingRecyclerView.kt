package io.sunshine0523.sidebar.ui.sidebar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.sunshine0523.sidebar.bean.SidebarAppInfo
import io.sunshine0523.sidebar.databinding.ItemSidebarAppBinding
import me.zhanghai.android.appiconloader.AppIconLoader

/**
 * @author KindBrave
 * @since 2023/10/21
 */
class SidebarAppSettingRecyclerView(private val callback: Callback):
    RecyclerView.Adapter<SidebarAppSettingRecyclerView.ViewHolder>() {

    private var appList: List<SidebarAppInfo>? = null
    private lateinit var appIconLoader: AppIconLoader

    inner class ViewHolder(dataBinding: ItemSidebarAppBinding): RecyclerView.ViewHolder(dataBinding.root) {
        val binding = dataBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        appIconLoader = AppIconLoader(100, false, parent.context)
        return ViewHolder(ItemSidebarAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return appList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.appIcon.setImageBitmap(appIconLoader.loadIcon(appList!![position].applicationInfo))
        holder.binding.appName.text = appList!![position].label
        holder.binding.packageName.text = appList!![position].applicationInfo.packageName
        holder.binding.switchShow.isChecked = appList!![position].isSidebarApp
        holder.binding.switchShow.setOnClickListener {
            appList!![position].isSidebarApp = holder.binding.switchShow.isChecked
            callback.onChanged(
                appList!![position].isSidebarApp,
                appList!![position].packageName,
                appList!![position].activityName,
                appList!![position].userId
            )
        }
        holder.binding.root.setOnClickListener {
            holder.binding.switchShow.isChecked = holder.binding.switchShow.isChecked.not()
            appList!![position].isSidebarApp = holder.binding.switchShow.isChecked
            callback.onChanged(
                appList!![position].isSidebarApp,
                appList!![position].packageName,
                appList!![position].activityName,
                appList!![position].userId
            )
        }
    }

    fun updateList(appList: List<SidebarAppInfo>) {
        this.appList = appList
        notifyDataSetChanged()
    }

    interface Callback {
        fun onChanged(sidebarApp: Boolean, packageName: String, className: String, userId: Int)
    }
}