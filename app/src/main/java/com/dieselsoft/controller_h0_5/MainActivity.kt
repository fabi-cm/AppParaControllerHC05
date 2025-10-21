package com.dieselsoft.controller_h0_5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dieselsoft.controller_h0_5.features.view.BluetoothScreen
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel
import com.dieselsoft.controller_h0_5.ui.theme.Controllerh05Theme

class MainActivity : ComponentActivity() {
    private val viewModel: BluetoothViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Controllerh05Theme {
                setContent {
                    BluetoothScreen(viewModel)
                }
            }
        }
    }
}