package com.example.appdelivery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appdelivery.data.session.SessionManager
import com.example.appdelivery.ui.auth.LoginScreen
import com.example.appdelivery.ui.auth.RegisterScreen
import com.example.appdelivery.ui.profile.ProfileScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val session = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (session.userId.first() != null) Routes.PROFILE else Routes.LOGIN
    }

    val start = startDestination ?: return
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.PROFILE) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate(Routes.PROFILE) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(onLogout = {
                scope.launch {
                    session.clear()
                    nav.navigate(Routes.LOGIN) { popUpTo(0) }
                }
            })
        }
    }
}
