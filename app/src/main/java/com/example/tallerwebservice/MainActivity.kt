package com.example.tallerwebservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tallerwebservice.ui.ChatScreen
import com.example.tallerwebservice.ui.theme.TallerWebServiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TallerWebServiceTheme {
                ChatScreen()
            }
        }
    }
}
