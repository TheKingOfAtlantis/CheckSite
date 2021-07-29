package com.sksulai.checksite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sksulai.checksite.ui.EntryPoint
import com.sksulai.checksite.ui.theme.CheckSiteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckSiteTheme { EntryPoint() }
        }
    }
}
