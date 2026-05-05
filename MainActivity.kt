
package com.example.complaintapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay


val AppBlack = Color(0xFF000000)
val GlassCardBlack = Color(0xFF141414).copy(alpha = 0.7f) // Semi-transparent to show gradient
val PremiumBorder = Color(0xFF323232)


val OrangePrimary = Color(0xFFF7931E)
val OrangeSoft = Color(0xFFF9BE70)
val OrangeStrong = Color(0xFFED6A11)

val AppWhite = Color(0xFFFFFFFF)
val StatusResolved = Color(0xFF2ECC71)


val GlobalBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        AppBlack,
        AppBlack,
        OrangePrimary.copy(alpha = 0.4f),
        OrangeStrong.copy(alpha = 0.8f)
    )
)

val ButtonOrangeGradient = Brush.horizontalGradient(listOf(OrangePrimary, OrangeStrong))
val ButtonGreenGradient = Brush.horizontalGradient(listOf(Color(0xFF2ECC71), Color(0xFF27AE60)))
val ButtonRedGradient = Brush.horizontalGradient(listOf(Color(0xFFE74C3C), Color(0xFFC0392B)))


val OrangeGlowBrush = Brush.radialGradient(
    colors = listOf(OrangePrimary.copy(alpha = 0.6f), Color.Transparent),
    radius = 1100f
)


enum class Screen {
    Splash, Login, HomeFeed, RaiseComplaint, MyComplaints, LeadersRating,
    BeforeAfterPending, BeforeAfterCompleted, Profile
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color.Transparent, primary = OrangePrimary)) {
                Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize().background(GlobalBackgroundBrush)) {
                    SamadhanApp()
                }
            }
        }
    }
}

@Composable
fun SamadhanApp() {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.Splash -> SplashScreen { currentScreen = Screen.Login }
            Screen.Login -> LoginScreen { currentScreen = Screen.HomeFeed }
            Screen.HomeFeed -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) { HomeFeedScreen() }
            Screen.MyComplaints -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) { MyComplaintsScreen(onNavigate = { currentScreen = it }) }
            Screen.LeadersRating -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) { LeadersRatingScreen() }
            Screen.Profile -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) { ProfileScreen(onLogout = { currentScreen = Screen.Login }) }
            Screen.RaiseComplaint -> RaiseComplaintScreen(onBack = { currentScreen = Screen.HomeFeed }, onSubmit = { currentScreen = Screen.MyComplaints })

            Screen.BeforeAfterPending -> BeforeAfterScreen(
                isPendingReview = true,
                titleText = "Pothole, Nangli Sakrawati",
                beforeRes = R.drawable.hole,
                afterRes = R.drawable.correcthole,
                onBack = { currentScreen = Screen.MyComplaints }
            )
            Screen.BeforeAfterCompleted -> BeforeAfterScreen(
                isPendingReview = false,
                titleText = "Water Pipe Leak, Nangli Sakrawati",
                beforeRes = R.drawable.waterleak,
                afterRes = R.drawable.water,
                onBack = { currentScreen = Screen.MyComplaints }
            )
        }
    }
}


@Composable
fun MainScaffold(currentScreen: Screen, onNavigate: (Screen) -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        bottomBar = { SamadhanBottomNav(currentScreen, onNavigate) },
        containerColor = Color.Transparent,
        content = content
    )
}

@Composable
fun SamadhanBottomNav(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(containerColor = AppBlack.copy(alpha = 0.5f), contentColor = PremiumBorder, tonalElevation = 0.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) }, label = { Text("Feed") },
            selected = currentScreen == Screen.HomeFeed, onClick = { onNavigate(Screen.HomeFeed) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, unselectedIconColor = AppWhite.copy(0.4f), indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, null) }, label = { Text("Issues") },
            selected = currentScreen == Screen.MyComplaints, onClick = { onNavigate(Screen.MyComplaints) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, unselectedIconColor = AppWhite.copy(0.4f), indicatorColor = Color.Transparent)
        )

        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(ButtonOrangeGradient, CircleShape)
                        .shadow(16.dp, CircleShape, ambientColor = OrangePrimary, spotColor = OrangeStrong),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New", tint = AppBlack, modifier = Modifier.size(36.dp))
                }
            },
            selected = currentScreen == Screen.RaiseComplaint,
            onClick = { onNavigate(Screen.RaiseComplaint) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, null) }, label = { Text("Leaders") },
            selected = currentScreen == Screen.LeadersRating, onClick = { onNavigate(Screen.LeadersRating) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, unselectedIconColor = AppWhite.copy(0.4f), indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") },
            selected = currentScreen == Screen.Profile, onClick = { onNavigate(Screen.Profile) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangePrimary, selectedTextColor = OrangePrimary, unselectedIconColor = AppWhite.copy(0.4f), indicatorColor = Color.Transparent)
        )
    }
}

// 1. Splash Screen
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(3000); onTimeout() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(OrangeGlowBrush))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Build, contentDescription = "Logo", tint = OrangePrimary, modifier = Modifier.size(90.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("SAMADHAN", color = AppWhite, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        }
    }
}

// 2. Login Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Box(modifier = Modifier.fillMaxSize().background(OrangeGlowBrush))

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .shadow(40.dp, RoundedCornerShape(24.dp), ambientColor = OrangePrimary, spotColor = OrangeStrong),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GlassCardBlack),
            border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Welcome Back", color = AppWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = phone, onValueChange = { phone = it }, label = { Text("Phone Number", color = AppWhite.copy(0.6f)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary, unfocusedBorderColor = PremiumBorder,
                        focusedContainerColor = AppBlack.copy(alpha = 0.5f), unfocusedContainerColor = AppBlack.copy(alpha = 0.5f),
                        focusedTextColor = AppWhite, unfocusedTextColor = AppWhite
                    ),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = OrangePrimary) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Password", color = AppWhite.copy(0.6f)) },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary, unfocusedBorderColor = PremiumBorder,
                        focusedContainerColor = AppBlack.copy(alpha = 0.5f), unfocusedContainerColor = AppBlack.copy(alpha = 0.5f),
                        focusedTextColor = AppWhite, unfocusedTextColor = AppWhite
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = OrangePrimary) }
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        if(phone.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            if (phone == "9990330522" && password == "123456") {
                                val db = Firebase.firestore
                                val userMap = hashMapOf("phone" to phone, "loginTime" to System.currentTimeMillis())
                                db.collection("logins").add(userMap)
                                isLoading = false
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                val db = Firebase.firestore
                                db.collection("users")
                                    .whereEqualTo("phone", phone).whereEqualTo("password", password).get()
                                    .addOnSuccessListener { documents ->
                                        isLoading = false
                                        if (!documents.isEmpty) {
                                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        } else {
                                            Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Please enter phone and password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = OrangePrimary)
                        .background(ButtonOrangeGradient, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = AppBlack, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Secure Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppBlack)
                    }
                }
            }
        }
    }
}

// 3. Home Feed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Ward-124 Updates", color = OrangePrimary, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            actions = { IconButton(onClick = {}) { Icon(Icons.Default.Search, null, tint = OrangePrimary) } }
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item { FeedPostCard("Rahul Sharma", "April 22, 2026", "Massive pothole causing accidents.", "Nangli Sakrawati, Ward-124", 124, R.drawable.hole) }
            item { FeedPostCard("Priya Desai", "April 21, 2026", "Streetlights dead for 3 days.", "Nangli Sakrawati Main Rd", 89, R.drawable.light) }
            item { FeedPostCard("Amit Kumar", "April 18, 2026", "Pipe burst wasting clean water.", "Block B, Nangli Sakrawati", 210, R.drawable.waterleak) }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FeedPostCard(user: String, time: String, description: String, location: String, initialLikes: Int, imageResId: Int) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(initialLikes) }
    val heartColor by animateColorAsState(if (isLiked) OrangeStrong else AppWhite.copy(0.6f), tween(300))

    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GlassCardBlack), border = BorderStroke(1.dp, PremiumBorder)) {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PremiumBorder), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = OrangePrimary) }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user, color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$time • $location", color = AppWhite.copy(0.5f), fontSize = 12.sp)
                }
            }
            Image(painter = painterResource(id = imageResId), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                Text(description, color = AppWhite, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = PremiumBorder)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { isLiked = !isLiked; likesCount += if (isLiked) 1 else -1 }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = heartColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$likesCount Supports", color = heartColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {}) { Icon(Icons.Default.Share, null, tint = AppWhite.copy(0.6f)) }
                }
            }
        }
    }
}

// 4. File a Complaint Screen 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseComplaintScreen(onBack: () -> Unit, onSubmit: () -> Unit) {
    val categories = listOf(
        Pair("Road Potholes", Icons.Default.Warning), Pair("Streetlights", Icons.Default.Info),
        Pair("Water Supply", Icons.Default.Build), Pair("Garbage Pile", Icons.Default.Delete),
        Pair("Power Outage", Icons.Default.Bolt), Pair("Other", Icons.Default.Add)
    )
    var selectedCategory by remember { mutableStateOf("Road Potholes") }
    var details by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { capturedImage = bitmap }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("File a Complaint", color = OrangePrimary) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = OrangePrimary) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {

            Text("Select Category", color = AppWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(280.dp)) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category.first
                    val bgColor by animateColorAsState(if (isSelected) OrangePrimary.copy(alpha = 0.2f) else GlassCardBlack)
                    val borderColor by animateColorAsState(if (isSelected) OrangePrimary else PremiumBorder)
                    val contentColor by animateColorAsState(if (isSelected) OrangePrimary else AppWhite)

                    Card(
                        modifier = Modifier.fillMaxWidth().height(80.dp).clickable { selectedCategory = category.first },
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(category.second, null, tint = contentColor, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(category.first, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = details, onValueChange = { details = it }, placeholder = { Text("Enter complaint details...", color = AppWhite.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary, unfocusedBorderColor = PremiumBorder,
                    focusedContainerColor = GlassCardBlack, unfocusedContainerColor = GlassCardBlack,
                    focusedTextColor = AppWhite
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Photo Upload", color = AppWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                if (capturedImage == null) {
                    Button(
                        onClick = { cameraLauncher.launch(null) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.size(width = 80.dp, height = 64.dp).background(ButtonOrangeGradient, RoundedCornerShape(12.dp)).shadow(8.dp, spotColor = OrangePrimary), contentPadding = PaddingValues(0.dp)
                    ) { Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = AppBlack, modifier = Modifier.size(32.dp)) }
                } else {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(), contentDescription = "Captured", contentScale = ContentScale.Crop,
                        modifier = Modifier.size(width = 80.dp, height = 64.dp).clip(RoundedCornerShape(12.dp)).border(2.dp, OrangePrimary, RoundedCornerShape(12.dp)).clickable { cameraLauncher.launch(null) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Location", color = AppWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "Nangli Sakrawati, Ward-124", onValueChange = {}, readOnly = true, leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = OrangePrimary) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = PremiumBorder, focusedContainerColor = GlassCardBlack, unfocusedContainerColor = GlassCardBlack,
                    focusedTextColor = AppWhite, unfocusedTextColor = AppWhite
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(60.dp).shadow(16.dp, RoundedCornerShape(16.dp), spotColor = OrangePrimary).background(ButtonOrangeGradient, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)
            ) { Text("Submit Complaint", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppBlack) }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// 5. Escalation Tracker
@Composable
fun EscalationTracker(currentLevel: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text("Escalation Status", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
            EscalationStep("Level 1\nMC", "Day 1", currentLevel >= 1, true)
            EscalationLine(currentLevel >= 2)
            EscalationStep("Level 2\nMLA", "5 Days", currentLevel >= 2, false)
            EscalationLine(currentLevel >= 3)
            EscalationStep("Level 3\nMP", "10 Days", currentLevel >= 3, false)
        }
    }
}

@Composable
fun EscalationStep(title: String, duration: String, isActive: Boolean, isFirst: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (isActive) OrangePrimary else PremiumBorder).border(2.dp, if (isActive) OrangeSoft else Color.Transparent, CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = if (isActive) AppWhite else PremiumBorder, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(duration, color = if (isActive) OrangeSoft else PremiumBorder, fontSize = 10.sp)
    }
}

@Composable
fun RowScope.EscalationLine(isActive: Boolean) {
    Box(modifier = Modifier.weight(1f).height(2.dp).align(Alignment.Top).offset(y = 11.dp).background(if (isActive) OrangePrimary else PremiumBorder))
}

// 6. My Complaints
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyComplaintsScreen(onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("My Complaints", color = OrangePrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { MyComplaintListCard("Pothole, Main Rd", "April 19, 2026", OrangeSoft, "Pending Review", 1) { onNavigate(Screen.BeforeAfterPending) } }
            item { MyComplaintListCard("Streetlights Out", "April 15, 2026", OrangePrimary, "In Progress", 2) {} }
            item { MyComplaintListCard("Water Pipe Leak", "April 10, 2026", StatusResolved, "Completed", 3) { onNavigate(Screen.BeforeAfterCompleted) } }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun MyComplaintListCard(title: String, date: String, statusColor: Color, statusText: String, level: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)).clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GlassCardBlack), border = BorderStroke(1.dp, PremiumBorder)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(title, color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Date: $date • Nangli Sakrawati", color = AppWhite.copy(0.5f), fontSize = 12.sp)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(statusColor.copy(alpha = 0.15f)).border(1.dp, statusColor, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Divider(color = PremiumBorder, modifier = Modifier.padding(vertical = 12.dp))
            EscalationTracker(currentLevel = level)
        }
    }
}

// 7. Before-After Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforeAfterScreen(isPendingReview: Boolean, titleText: String, beforeRes: Int, afterRes: Int, onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Resolution Verification", color = OrangePrimary) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = OrangePrimary) } }) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titleText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppWhite)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEFORE", fontWeight = FontWeight.Bold, color = AppWhite.copy(0.6f), modifier = Modifier.padding(bottom = 8.dp))
                    Image(
                        painter = painterResource(id = beforeRes), contentDescription = "Before", contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, PremiumBorder, RoundedCornerShape(16.dp))
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AFTER", fontWeight = FontWeight.Bold, color = StatusResolved, modifier = Modifier.padding(bottom = 8.dp))
                    Image(
                        painter = painterResource(id = afterRes), contentDescription = "After", contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).border(2.dp, StatusResolved.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isPendingReview) {
                Button(
                    onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom=8.dp).background(ButtonGreenGradient, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)
                ) { Text("Approve Resolution", color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp) }

                Button(
                    onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp).background(ButtonRedGradient, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)
                ) { Text("Reject Resolution", color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = StatusResolved.copy(alpha = 0.1f)), border = BorderStroke(1.dp, StatusResolved), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = StatusResolved, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Resolution Approved", color = StatusResolved, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// 8. Rating
@Composable
fun InteractiveRatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star, contentDescription = "Star",
                tint = if (i <= rating) OrangePrimary else PremiumBorder,
                modifier = Modifier.size(28.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onRatingChanged(i) }
            )
        }
    }
}

// 9. Leaders Rating Screen 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadersRatingScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Local Representatives", color = OrangePrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { LeaderCard("Ramesh Kumar", "MC - Nangli Sakrawati Ward-124", 4, "82%") }
            item { LeaderCard("Aditi Sharma", "MLA - Najafgarh Constituency", 3, "65%") }
            item { LeaderCard("Vikram Singh", "MP - West Delhi", 5, "94%") }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LeaderCard(name: String, role: String, initialRating: Int, resolutionRate: String) {
    var userRating by remember { mutableStateOf(initialRating) }
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GlassCardBlack), border = BorderStroke(1.dp, PremiumBorder)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(PremiumBorder), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = OrangePrimary, modifier = Modifier.size(36.dp)) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(name, color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(role, color = OrangeSoft, fontSize = 12.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Resolved:", color = AppWhite.copy(0.6f), fontSize = 12.sp)
                    Text(resolutionRate, color = StatusResolved, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Divider(color = PremiumBorder, modifier = Modifier.padding(vertical = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Rate Work:", color = AppWhite.copy(0.6f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    InteractiveRatingBar(rating = userRating, onRatingChanged = { userRating = it })
                }
                Button(
                    onClick = { Toast.makeText(context, "Rating Submitted!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.height(36.dp).background(ButtonOrangeGradient, RoundedCornerShape(8.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit", color = AppBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 10. Profile Screen 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(ButtonOrangeGradient), contentAlignment = Alignment.Center) {
                Text("A", color = AppBlack, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Anshu Kumar", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AppWhite)
                Text("Nangli Sakrawati, Ward-124", color = AppWhite.copy(0.6f))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        val items = listOf("Manage Account", "Notification Settings", "Submit Feedback", "Help Centre")
        items.forEach { title ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { }, colors = CardDefaults.cardColors(containerColor = GlassCardBlack), border = BorderStroke(1.dp, PremiumBorder)) {
                Text(title, modifier = Modifier.padding(16.dp), color = AppWhite, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp).background(ButtonRedGradient, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Log Out", color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}