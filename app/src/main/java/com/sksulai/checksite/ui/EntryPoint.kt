package com.sksulai.checksite.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable fun EntryPoint(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen() }
    }
}
