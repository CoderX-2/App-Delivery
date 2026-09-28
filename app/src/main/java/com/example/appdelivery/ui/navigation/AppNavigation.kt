package com.example.appdelivery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appdelivery.data.session.SessionManager
import com.example.appdelivery.ui.auth.LoginScreen
import com.example.appdelivery.ui.auth.RegisterScreen
import com.example.appdelivery.ui.profile.AddressFormScreen
import com.example.appdelivery.ui.profile.EditProfileScreen
import com.example.appdelivery.ui.profile.ProfileScreen
import com.example.appdelivery.ui.profile.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val ADDRESS_FORM = "address_form?addressId={addressId}"
    fun addressForm(id: Long? = null) =
        if (id == null) "address_form" else "address_form?addressId=$id"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val session = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val profileVm: ProfileViewModel = viewModel()
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
            ProfileScreen(
                vm = profileVm,
                onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                onAddAddress = { nav.navigate(Routes.addressForm()) },
                onEditAddress = { id -> nav.navigate(Routes.addressForm(id)) },
                onLogout = {
                    scope.launch {
                        session.clear()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) }
                    }
                }
            )
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(vm = profileVm, onDone = { nav.popBackStack() })
        }
        composable(
            route = Routes.ADDRESS_FORM,
            arguments = listOf(navArgument("addressId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val id = entry.arguments?.getLong("addressId")?.takeIf { it != -1L }
            AddressFormScreen(vm = profileVm, addressId = id, onDone = { nav.popBackStack() })
        }
    }
}
