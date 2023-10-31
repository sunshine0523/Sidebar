package io.sunshine0523.sidebar.ui.sidebar

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.sunshine0523.sidebar.R
import io.sunshine0523.sidebar.databinding.ActivitySidebarAppSettingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import rikka.material.app.MaterialActivity
import rikka.recyclerview.addFastScroller

/**
 * @author KindBrave
 * @since 2023/10/21
 */
class SidebarAppSettingActivity : MaterialActivity() {
    private lateinit var binding: ActivitySidebarAppSettingBinding
    private lateinit var viewModel: SidebarAppSettingViewModel
    private val scope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySidebarAppSettingBinding.inflate(layoutInflater)
        binding.toolbar.title = getString(R.string.sidebar_app_setting_label)
        setContentView(binding.root)
        viewModel = SidebarAppSettingViewModel(application)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val adapter = SidebarAppSettingRecyclerView(object : SidebarAppSettingRecyclerView.Callback {
            override fun onChanged(
                sidebarApp: Boolean,
                packageName: String,
                className: String,
                userId: Int
            ) {
                if (sidebarApp) {
                    viewModel.addSidebarApp(packageName, className, userId)
                } else {
                    viewModel.deleteSidebarApp(packageName, className, userId)
                }
            }

        })
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SidebarAppSettingActivity)
            setAdapter(adapter)
            addFastScroller()
        }
        scope.launch(Dispatchers.IO) {
            viewModel.appListFlow.collect {
                launch(Dispatchers.Main) {
                    adapter.updateList(it)
                }
            }
        }
    }
}