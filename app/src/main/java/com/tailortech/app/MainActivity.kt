package com.tailortech.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tailortech.app.data.MeasurementsRepository
import com.tailortech.app.data.TailorTechDatabase
import com.tailortech.app.ui.TailorTechApp
import com.tailortech.app.ui.theme.TailorTechTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(
            repository = MeasurementsRepository(
                dao = TailorTechDatabase.get(this).userMeasurementsDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TailorTechTheme {
                TailorTechApp(viewModel = viewModel)
            }
        }
    }
}
