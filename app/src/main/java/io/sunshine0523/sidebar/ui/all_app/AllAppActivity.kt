package io.sunshine0523.sidebar.ui.all_app

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import io.sunshine0523.sidebar.databinding.ActivityAllAppBinding
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import rikka.material.app.MaterialActivity
import rikka.recyclerview.addFastScroller

/**
 * @author KindBrave
 * @since 2023/10/25
 */
class AllAppActivity: MaterialActivity() {
    private val logger = Logger(TAG)
    private lateinit var binding: ActivityAllAppBinding
    private lateinit var viewModel: AllAppViewModel
    private val scope = MainScope()

    companion object {
        private const val PACKAGE = "com.sunshine.freeform"
        private const val ACTION = "com.sunshine.freeform.start_freeform"
        private const val TAG = "AllAppActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAppBinding.inflate(layoutInflater)
        viewModel = AllAppViewModel(application)
        setContentView(binding.root)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val adapter = AllAppRecyclerView(object : AllAppRecyclerView.Callback {
            override fun onClick(packageName: String, activityName: String, userId: Int) {
                val intent = Intent(ACTION).apply {
                    setPackage(PACKAGE)
                    putExtra("packageName", packageName)
                    putExtra("activityName", activityName)
                    putExtra("userId", userId)
                }
                sendBroadcast(intent)
                finish()
            }
        })
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@AllAppActivity, 4)
            setAdapter(adapter)
            addFastScroller()
        }
        scope.launch(Dispatchers.IO) {
            viewModel.appListFlow.collect {
                if (Debug.isDebug) logger.d("changed: $it")
                launch(Dispatchers.Main) {
                    adapter.updateList(it)
                }
            }
        }
    }
}