package com.ekasi.studios.stylelink

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.ekasi.studios.stylelink.adapters.PlacePredictionAdapter
import com.ekasi.studios.stylelink.base.components.StorePreview.StorePreviewViewModel
import com.ekasi.studios.stylelink.base.composable.BottomNavigation
import com.ekasi.studios.stylelink.data.converters.ListConverter
import com.ekasi.studios.stylelink.data.local.AppDatabase
import com.ekasi.studios.stylelink.data.repository.AuthRepository
import com.ekasi.studios.stylelink.data.repository.MarkersRepository
import com.ekasi.studios.stylelink.data.repository.ProductRepository
import com.ekasi.studios.stylelink.data.repository.ServicesRepository
import com.ekasi.studios.stylelink.data.repository.StoreRepository
import com.ekasi.studios.stylelink.data.repository.UserRepository
import com.ekasi.studios.stylelink.navigation.SetupNavGraph
import com.ekasi.studios.stylelink.ui.auth.login.LoginViewModel
import com.ekasi.studios.stylelink.ui.auth.register.RegisterViewModel
import com.ekasi.studios.stylelink.ui.auth.signup.SignupViewModel
import com.ekasi.studios.stylelink.ui.screens.discover.DiscoverViewModel
import com.ekasi.studios.stylelink.ui.screens.home.HomeViewModel
import com.ekasi.studios.stylelink.ui.splash.SplashViewModel
import com.ekasi.studios.stylelink.ui.storeprofile.StoreProfileViewModel
import com.ekasi.studios.stylelink.ui.theme.StylelinkTheme
import com.ekasi.studios.stylelink.utils.network.NetworkClient
import com.ekasi.studios.stylelink.utils.services.RequestLocationPermission
import com.ekasi.studios.stylelink.viewModels.LocationViewModel
import com.ekasi.studios.stylelink.viewModels.PlacesViewModel
import com.ekasi.studios.stylelink.viewModels.StoresViewModel
import com.ekasi.studios.stylelink.viewModels.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject


class MainActivity : ComponentActivity() {
    companion object {
        lateinit var appDatabase: AppDatabase
    }

    //
    @Inject
    lateinit var placePredictionAdapter: PlacePredictionAdapter
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    var selectedIndex: Int = 0 // Track selected index

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locationPermissionGranted by mutableStateOf(false)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this)

        appDatabase =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "stylelink.db")
                .addTypeConverter(ListConverter())
                .build()

        setContent {
            StylelinkTheme {
                // Navigation Controller
                val navController = rememberNavController()

                // Firebase
                val auth = Firebase.auth
                val authService = NetworkClient.NetworkClient.authService()

                // Repositories
                val userRepository = UserRepository(
                    userApiService = NetworkClient.NetworkClient.userApiService(),
                    authService = authService
                )
                val storesRepository =
                    StoreRepository(NetworkClient.NetworkClient.storesApiService())
                val servicesRepository =
                    ServicesRepository(NetworkClient.NetworkClient.servicesApiService())
                val productRepository =
                    ProductRepository(NetworkClient.NetworkClient.productsApiService())

                val markersRepository =
                    MarkersRepository(NetworkClient.NetworkClient.markersApiService())


                // ViewModels
                val userViewModel = UserViewModel(
                    userDao = appDatabase.userDao(),
                    application = application,
                    userRepository = userRepository
                )

                val signupViewModel = SignupViewModel(
                    authRepository = AuthRepository(auth),
                    navController = navController
                )

                val splashViewModel = SplashViewModel(
                    authRepository = AuthRepository(auth),
                    navController = navController
                )

                val registerViewModel = RegisterViewModel(
                    userRepository = userRepository,
                    navController = navController,
                    userViewModel = userViewModel,
                    authRepository = AuthRepository(auth)
                )

                val homeViewModel = HomeViewModel(
                    authRepository = AuthRepository(auth),
                    navController = navController,
                    userViewModel = userViewModel
                )

                val loginViewModel = LoginViewModel(
                    authRepository = AuthRepository(auth),
                    navController = navController
                )

                val storesViewModel = StoresViewModel(
                    storeRepository = storesRepository,
                )

                val discoverViewModel = DiscoverViewModel(
                    navController = navController,
                    application = application,
                    markersRepository = markersRepository
                )

                val storeProfileViewModel = StoreProfileViewModel(
                    storeRepository = storesRepository,
                    servicesRepository = servicesRepository,
                    navController = navController,
                    productRepository = productRepository
                )

                val locationViewModel = LocationViewModel(
                    application = application
                )

                val placesViewModel = PlacesViewModel(
                    application = application,
                    markersRepository = markersRepository
                )

                val storePreviewViewModel = StorePreviewViewModel(
                    storeRepository = storesRepository,
                )

                val navigationItems = listOf(
                    "home",
                    "home",
                    "discover",
                    "home",
                    "home"
                )

                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute in navigationItems) {
                            BottomNavigation(
                                navController = navController,
                                selectedIndex = selectedIndex
                            )
                        }
                    }
                ) { it ->
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .background(Color.Transparent)
                            .fillMaxWidth()
                    ) {
                        SetupNavGraph(
                            navController = navController,
                            splashScreenViewModel = splashViewModel,
                            signupViewModel = signupViewModel,
                            registerViewModel = registerViewModel,
                            homeViewModel = homeViewModel,
                            loginViewModel = loginViewModel,
                            discoverViewModel = discoverViewModel,
                            storesViewModel = storesViewModel,
                            storeProfileViewModel = storeProfileViewModel,
                            locationViewModel = locationViewModel,
                            placesViewModel = placesViewModel,
                            storePreviewViewModel = storePreviewViewModel
                        )
                    }
                }

                if (areLocationPermissionsGranted()) {
                    getCurrentLocation(
                        onGetCurrentLocationSuccess = {
                            locationViewModel.storeCurrentLocation(it)
                        },
                        onGetCurrentLocationFailed = {
                            locationViewModel.onGetCurrentLocationFailed(it.message.toString())
                        }
                    )

                } else {
                    RequestLocationPermission(
                        onPermissionGranted = {
                            getCurrentLocation(
                                onGetCurrentLocationSuccess = {
                                    locationViewModel.storeCurrentLocation(it)
                                },
                                onGetCurrentLocationFailed = {
                                    locationViewModel.onGetCurrentLocationFailed(it.message.toString())
                                }
                            )
                        },
                        onPermissionDenied = { /*TODO*/ },
                        onPermissionsRevoked = { /*TODO*/ },
                    )
                }
            }
        }
    }

    /**
     * Retrieves the current user location asynchronously.
     *
     * @param onGetCurrentLocationSuccess Callback function invoked when the current location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetCurrentLocationFailed Callback function invoked when an error occurs while retrieving the current location.
     *        It provides the Exception that occurred.
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     *        If set to false, it uses balanced power accuracy.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true,
    ) {
        // Determine the accuracy priority based on the 'priority' parameter
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the current location asynchronously
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    /**
     * Checks if location permissions are granted.
     *
     * @return true if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted; false otherwise.
     */
    private fun areLocationPermissionsGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }
}



