package com.example.appdelivery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appdelivery.data.session.SessionManager
import com.example.appdelivery.ui.auth.LoginScreen
import com.example.appdelivery.ui.auth.RegisterScreen
import com.example.appdelivery.ui.cart.CartScreen
import com.example.appdelivery.ui.cart.CartViewModel
import com.example.appdelivery.ui.catalog.CatalogViewModel
import com.example.appdelivery.ui.catalog.MenuScreen
import com.example.appdelivery.ui.catalog.RestaurantListScreen
import com.example.appdelivery.ui.catalog.SearchScreen
import com.example.appdelivery.ui.catalog.SearchViewModel
import com.example.appdelivery.ui.courier.AvailableOrdersScreen
import com.example.appdelivery.ui.courier.CourierHomeScreen
import com.example.appdelivery.ui.courier.CourierRegisterScreen
import com.example.appdelivery.ui.courier.CourierViewModel
import com.example.appdelivery.ui.courier.DeliveryDetailScreen
import com.example.appdelivery.ui.map.MapScreen
import com.example.appdelivery.ui.map.MapViewModel
import com.example.appdelivery.ui.order.OrderDetailScreen
import com.example.appdelivery.ui.order.OrderSummaryScreen
import com.example.appdelivery.ui.order.OrderViewModel
import com.example.appdelivery.ui.order.OrdersScreen
import com.example.appdelivery.ui.payment.PaymentScreen
import com.example.appdelivery.ui.payment.PaymentViewModel
import com.example.appdelivery.ui.profile.AddressFormScreen
import com.example.appdelivery.ui.profile.EditProfileScreen
import com.example.appdelivery.ui.profile.ProfileScreen
import com.example.appdelivery.ui.profile.ProfileViewModel
import com.example.appdelivery.ui.review.RateOrderScreen
import com.example.appdelivery.ui.review.ReviewViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val RESTAURANTS = "restaurants"
    const val SEARCH = "search"
    const val MAP = "map"
    const val CART = "cart"
    const val PAYMENT = "payment"
    const val ORDER_SUMMARY = "order_summary"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    fun orderDetail(orderId: String) = "order_detail/$orderId"
    const val RATE_ORDER = "rate_order/{orderId}"
    fun rateOrder(orderId: String) = "rate_order/$orderId"
    const val MENU = "menu/{restaurantId}"
    fun menu(id: Long) = "menu/$id"
    const val ADDRESS_FORM = "address_form?addressId={addressId}"
    fun addressForm(id: Long? = null) =
        if (id == null) "address_form" else "address_form?addressId=$id"
    const val COURIER_HOME = "courier_home"
    const val COURIER_REGISTER = "courier_register"
    const val COURIER_ORDERS = "courier_orders"
    const val COURIER_DELIVERY = "courier_delivery/{orderId}"
    fun courierDelivery(orderId: String) = "courier_delivery/$orderId"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val session = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val profileVm: ProfileViewModel = viewModel()
    val catalogVm: CatalogViewModel = viewModel()
    val searchVm: SearchViewModel = viewModel()
    val mapVm: MapViewModel = viewModel()
    val cartVm: CartViewModel = viewModel()
    val paymentVm: PaymentViewModel = viewModel()
    val orderVm: OrderViewModel = viewModel()
    val courierVm: CourierViewModel = viewModel()
    val reviewVm: ReviewViewModel = viewModel()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val role = session.role.first()
        val userId = session.userId.first()
        val courierId = session.courierId.first()
        startDestination = when {
            role == "COURIER" || courierId != null -> Routes.COURIER_HOME
            role == "CUSTOMER" || userId != null -> Routes.PROFILE
            else -> Routes.LOGIN
        }
    }

    val start = startDestination ?: return
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.PROFILE) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToRegister = { nav.navigate(Routes.REGISTER) },
                onCourierLoginSuccess = {
                    nav.navigate(Routes.COURIER_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToCourierRegister = { nav.navigate(Routes.COURIER_REGISTER) },
                courierVm = courierVm
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
        composable(Routes.COURIER_REGISTER) {
            CourierRegisterScreen(
                vm = courierVm,
                onRegisterSuccess = {
                    nav.navigate(Routes.COURIER_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.COURIER_HOME) {
            CourierHomeScreen(
                vm = courierVm,
                onOpenAvailableOrders = {
                    nav.navigate(Routes.COURIER_ORDERS)
                },
                onOpenDeliveryDetail = { orderId ->
                    nav.navigate(Routes.courierDelivery(orderId))
                },
                onLogout = {
                    scope.launch {
                        session.clear()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) }
                    }
                }
            )
        }
        composable(Routes.COURIER_ORDERS) {
            AvailableOrdersScreen(
                vm = courierVm,
                onOrderAccepted = { orderId ->
                    nav.navigate(Routes.courierDelivery(orderId)) {
                        popUpTo(Routes.COURIER_HOME)
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = Routes.COURIER_DELIVERY,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            DeliveryDetailScreen(
                orderId = id,
                courierVm = courierVm,
                orderVm = orderVm,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                vm = profileVm,
                onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                onAddAddress = { nav.navigate(Routes.addressForm()) },
                onEditAddress = { id -> nav.navigate(Routes.addressForm(id)) },
                onOpenRestaurants = { nav.navigate(Routes.RESTAURANTS) },
                onOpenOrders = { nav.navigate(Routes.ORDERS) },
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
        composable(Routes.RESTAURANTS) {
            RestaurantListScreen(
                vm = catalogVm,
                onOpenMenu = { id -> nav.navigate(Routes.menu(id)) },
                onSearch = { nav.navigate(Routes.SEARCH) },
                onOpenMap = { nav.navigate(Routes.MAP) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = Routes.MENU,
            arguments = listOf(navArgument("restaurantId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("restaurantId") ?: return@composable
            MenuScreen(
                vm = catalogVm,
                cartVm = cartVm,
                restaurantId = id,
                onOpenCart = { nav.navigate(Routes.CART) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.CART) {
            CartScreen(
                vm = cartVm,
                onBack = { nav.popBackStack() },
                onAddAddress = { nav.navigate(Routes.addressForm()) },
                onProceedToPayment = { nav.navigate(Routes.PAYMENT) }
            )
        }
        composable(Routes.PAYMENT) {
            val totals by cartVm.totals.collectAsStateWithLifecycle()
            PaymentScreen(
                vm = paymentVm,
                totalAmount = totals.total,
                onBack = { nav.popBackStack() },
                onPaymentSuccess = { success ->
                    orderVm.pendingPaymentResult.value = success
                    nav.navigate(Routes.ORDER_SUMMARY)
                }
            )
        }
        composable(Routes.ORDER_SUMMARY) {
            val paymentResult by orderVm.pendingPaymentResult.collectAsStateWithLifecycle()
            val items by cartVm.items.collectAsStateWithLifecycle()
            val totals by cartVm.totals.collectAsStateWithLifecycle()
            val mode by cartVm.deliveryMode.collectAsStateWithLifecycle()
            val address by cartVm.selectedAddress.collectAsStateWithLifecycle()
            val restaurant by cartVm.currentRestaurant.collectAsStateWithLifecycle()

            val payment = paymentResult
            if (payment == null || items.isEmpty()) {
                LaunchedEffect(Unit) { nav.popBackStack() }
            } else {
                OrderSummaryScreen(
                    items = items,
                    totals = totals,
                    deliveryMode = mode,
                    selectedAddress = address,
                    restaurant = restaurant,
                    paymentResult = payment,
                    onConfirm = {
                        orderVm.placeOrder(
                            items = items,
                            totals = totals,
                            mode = mode,
                            restaurant = restaurant,
                            address = address,
                            payment = payment
                        ) { orderId ->
                            nav.navigate(Routes.orderDetail(orderId)) {
                                popUpTo(Routes.RESTAURANTS)
                            }
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
        }
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = id,
                orderVm = orderVm,
                onOpenOrders = { nav.navigate(Routes.ORDERS) },
                onGoToHome = { nav.navigate(Routes.RESTAURANTS) { popUpTo(Routes.RESTAURANTS) { inclusive = true } } },
                onGoToRate = { orderId -> nav.navigate(Routes.rateOrder(orderId)) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            route = Routes.RATE_ORDER,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("orderId") ?: return@composable
            RateOrderScreen(
                orderId = id,
                orderVm = orderVm,
                reviewVm = reviewVm,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.ORDERS) {
            OrdersScreen(
                vm = orderVm,
                onOpenOrder = { id -> nav.navigate(Routes.orderDetail(id)) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = searchVm,
                onOpenMenu = { id -> nav.navigate(Routes.menu(id)) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.MAP) {
            MapScreen(
                vm = mapVm,
                onOpenMenu = { id -> nav.navigate(Routes.menu(id)) },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
