package com.ekasi.studios.stylelink.ui.storeprofile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ekasi.studios.stylelink.base.composable.ProfileTitle
import com.ekasi.studios.stylelink.base.composable.StatBar
import com.ekasi.studios.stylelink.data.model.Service
import com.ekasi.studios.stylelink.ui.theme.StylelinkTheme
import com.ekasi.studios.stylelink.ui.theme.mediumSize
import com.ekasi.studios.stylelink.ui.theme.tinySize
import com.ekasi.studios.stylelink.utils.services.stores.models.Store
import com.ekasi.studios.stylelink.utils.states.ServicesState

@Composable
fun StoreProfile(
    storeProfileViewModel: StoreProfileViewModel,
    storeId: String?
) {
    val id = storeId ?: return
    val state = storeProfileViewModel.state.collectAsState()
    val servicesState = storeProfileViewModel.servicesState.collectAsState()

    LaunchedEffect(Unit) {
        /**
         * Fetches A Single Store Profile
         *
         * @param storeId: String
         */
        storeProfileViewModel.fetchStoreProfile(id)
    }

    BackHandler(
        enabled = true,
        onBack = { storeProfileViewModel.clearState() }
    )

    when (state.value) {
        is StoreProfileState.Loading -> {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(mediumSize)
                )
            }
        }

        is StoreProfileState.Success -> {
            val profile = (state.value as StoreProfileState.Success).store
            Column {
                StoreProfileComponent(store = profile, servicesState = servicesState)
            }

            LaunchedEffect(profile._id) {
                if (profile._id !== null) {
                    storeProfileViewModel.fetchStoreServices(profile._id)
                }
            }
        }

        is StoreProfileState.Error -> {
            val message = (state.value as StoreProfileState.Error).message
            Text(text = message)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProfileComponent(store: Store, servicesState: State<ServicesState>) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        store.name.lowercase(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowLeft,
                            contentDescription = "back_Button"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(tinySize, paddingValues.calculateTopPadding()),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = store.profileImage,
                    contentDescription = "store_profile_image",
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .height(mediumSize)
                        .width(mediumSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    StatBar(value = "1,2K", title = "Reviews")
                    if (servicesState.value !== ServicesState.Loading) {
                        val services = (servicesState.value as ServicesState.MultipleServicesSuccess).services
                        StatBar(value = services.size.toString(), title = "Services")
                    }
                    StatBar(value = "2K", title = "Favorites")

//                    StatBar(value = "4", title = "Location(s)")
                }
            }
            Text(
                text = store.run { name.capitalize() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            val description =
                "Lorem ipsum dolor sit amet. Ut dulcimers omnis vel reiciendis ducimus et veniam consequatur."
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(0.dp, 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .weight(1f),
                ) {

                    Text(text = "Book Services")
                    Spacer(modifier = Modifier.width(4.dp))
//                    Icon(
//                        imageVector = Icons.Rounded.Send,
//                        contentDescription = "chat_with_stylist",
//                        modifier = Modifier.size(tinySize)
//                    )
                }
                Row(
                    modifier = Modifier
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "share_icon"
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = "favorite_icon"
                        )
                    }

                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "share_icon"
                        )
                    }
                }
            }
            ProfileTitle(title = "Available Services")
            ServicesCarousel(servicesState = servicesState, storeId = store._id)

        }
    }
}

@Composable
fun ServicesCarousel(servicesState: State<ServicesState>, storeId: String?) {
    when (servicesState.value) {
        is ServicesState.Loading -> {
            Text(text = "Loading...")
        }

        is ServicesState.MultipleServicesSuccess -> {
            val services = (servicesState.value as ServicesState.MultipleServicesSuccess).services
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(0.dp, tinySize)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tinySize)
            ) {
                services.forEach { service: Service ->

                    ServiceCard(service = service)
                }
            }
        }

        is ServicesState.Error -> {
            val error = (servicesState.value as ServicesState.Error)
            Text(text = "Error: $error")
        }

        else -> {
            Text(text = "No Services have been found")
        }
    }
}

@Composable
fun ServiceCard(service: Service) {
    val configuration = LocalConfiguration.current
    val cardWidth = configuration.screenWidthDp.dp / 2
    val cardHeight = configuration.screenHeightDp.dp / 3
    val backgroundImage = "https://legends-barber.com/wp-content/uploads/2024/04/FLAT-TOP.png"
    Card(
        modifier = Modifier
            .height(cardHeight)
            .width(cardWidth)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = Color(0x20000000)
        )

    ) {
        Box() {
            AsyncImage(
                model = backgroundImage,
                contentDescription = "style_image_preview",
                modifier = Modifier
                    .height(275.dp)
                    .width(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x20000000),
                                Color(0x20000000),
                                Color.Black
                            )
                        )
                    )
                    .width(cardWidth),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Card(
                    modifier = Modifier
                        .padding(10.dp, tinySize),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        "Low Fade",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(8.dp, 4.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = service.name.uppercase(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "R${service.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceCardPreview() {
    val mockService = Service(
        "7f8gd893tgh804fE",
        "Lorem ipsum dolor sit amet consectetur adipisicing elit. Amet adipisci ipsa quis perspiciatis a non, quidem totam similique debitis quibusdam.",
        900000,
        "LEGENDARY FLAT-TOP LOW FADE",
        60,
        "662e12869bd8aa15d6ed684a",
    )
    StylelinkTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(tinySize)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tinySize)
        ) {
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
            ServiceCard(service = mockService)
        }
    }
}