package com.example.myapplication


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import com.example.myapplication.database.ActiveItem
import com.example.myapplication.screens.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.text.isNotBlank

class MainActivity : ComponentActivity() {

    sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: Int) {
        data object Active : Screen("Upcoming", R.string.upcoming_button, R.drawable.upcoming_ev)
        data object Upcoming : Screen("Add", R.string.add_button, R.drawable.add_field)
        data object Past : Screen("Past", R.string.past_button, R.drawable.past_ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainViewModel = MainViewModel()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(mainViewModel)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        MainActivity.Screen.Active,
        MainActivity.Screen.Upcoming,
        MainActivity.Screen.Past
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = @Composable {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val currentScreen = items.find { it.route == currentDestination?.route }
                    Text(
                        text = currentScreen?.let { stringResource(id = it.resourceId) } ?: "",
                        )
                }
            )
        },
        bottomBar = {
            NavigationBar (
                containerColor = Color.Transparent
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(painterResource(id = screen.icon),
                                contentDescription = stringResource(id = screen.resourceId),
                                modifier = Modifier.sizeIn(
                                    maxWidth = 64.dp, maxHeight = 64.dp,
                                    minWidth = 64.dp, minHeight = 64.dp
                                )
                            )
                               },
                        label = {
                            if (screen.route != "Add")
                                Text(stringResource(screen.resourceId))
                                else
                            Text("",
                                modifier = Modifier.padding(bottom = 12.dp)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }, colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorResource(R.color.main_blue),
                            unselectedIconColor =
                                if(screen.route == "Add")
                                    colorResource(R.color.add_field)
                                else
                                colorResource(R.color.gray_600),
                            indicatorColor = Color.Transparent

                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainActivity.Screen.Active.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainActivity.Screen.Active.route) { ActiveScreen(mainViewModel) }
            composable(MainActivity.Screen.Upcoming.route) { UpcomingScreen(mainViewModel) }
            composable(MainActivity.Screen.Past.route) { PastScreen(mainViewModel) }
        }
    }
}


@Composable
fun ActiveScreen(mainViewModel: MainViewModel) {
  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top
  ) {
      val activeItemsList: List<ActiveItem> by mainViewModel.allActiveItems.collectAsState()
      val filteredList = activeItemsList.filter { it.endDate!! >= Date().time }
      if (filteredList.isEmpty()) {
          Column(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(start = 8.dp, top = 16.dp, end = 8.dp, bottom = 24.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
          ) {
              Text("No active items to display.")
          }
      } else {
          LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
              items(filteredList) { item ->
                  ActiveListItem(item = item)
              }

          }
      }
  }
}

@Composable
fun ActiveListItem(item: ActiveItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Start,
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 8.dp)
          )  {
              Image(
                  painter = painterResource(id = R.drawable.blue_dot),
                  contentDescription = null,
                  modifier = Modifier
                      .sizeIn(maxWidth = 16.dp, maxHeight = 16.dp)
                      .padding(end = 6.dp)
              )
              val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
              Text("${dateFormat.format(item.startDate)} - ${dateFormat.format(item.endDate)}")
          }
            Row (
                modifier = Modifier
                    .padding(start = 20.dp)
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.mountains)
                            .error(R.drawable.mountains)
                            .build(),
                        contentDescription = "Active Item Image",
                        modifier = Modifier
                            .sizeIn(maxWidth = 160.dp, maxHeight = 120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painterResource(R.drawable.mountains),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .sizeIn(maxWidth = 160.dp, maxHeight = 120.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                ) {
                    Text(text = item.title)
                    Row {
                        Image(
                            painterResource(R.drawable.location),
                            contentDescription = null,
                            modifier = Modifier
                                .sizeIn(maxWidth = 24.dp, maxHeight = 24.dp)
                        )
                        Text(item.address)
                    }
                    Row {
                        Image(
                            painterResource(R.drawable.description),
                            contentDescription = null,
                            modifier = Modifier
                                .sizeIn(maxWidth = 24.dp, maxHeight = 24.dp)
                        )
                        Text(item.description)
                    }
                }
            }
        }
    }
}

const val IMGBB_API_KEY = "5f3a97713741b5b3bf23e0a72d43c40b"

suspend fun uploadImageToImgBB(context: android.content.Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val boundary = "---------------------------" + System.currentTimeMillis()
            val lineEnd = "\r\n"
            val twoHyphens = "--"

            val connection = URL("https://api.imgbb.com/1/upload").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            DataOutputStream(connection.outputStream).use { outputStream ->
                // API Key part
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"key\"$lineEnd")
                outputStream.writeBytes(lineEnd)
                outputStream.writeBytes(IMGBB_API_KEY + lineEnd)

                // Image part
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"${imageUri.lastPathSegment}\"$lineEnd")
                outputStream.writeBytes("Content-Type: image/jpeg$lineEnd")
                outputStream.writeBytes(lineEnd)
                outputStream.write(imageBytes)
                outputStream.writeBytes(lineEnd)

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                outputStream.flush()
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                val jsonResponse = JSONObject(response.toString())
                if (jsonResponse.getBoolean("success")) {
                    return@withContext jsonResponse.getJSONObject("data").getString("url")
                } else {
                    Log.e("ImgBBUpload", "ImgBB API Error: ${jsonResponse.getJSONObject("error").getString("message")}")
                }
            } else {
                Log.e("ImgBBUpload", "HTTP Error: ${connection.responseCode} ${connection.responseMessage}")

                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val reader = BufferedReader(InputStreamReader(errorStream))
                    val errorResponse = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        errorResponse.append(line)
                    }
                    reader.close()
                    Log.e("ImgBBUpload", "Error details: $errorResponse")
                }
            }
        } catch (e: Exception) {
            Log.e("ImgBBUpload", "Upload failed", e)
        }
        return@withContext null
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    var startDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var endDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Add New Event",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Start Date Picker
        OutlinedTextField(
            value = startDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select Start Date",
            onValueChange = { /* Read Only */ },
            label = { Text("Start Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartDatePicker = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    painterResource(id = R.drawable.start_date),
                    contentDescription = "Select Start Date",
                    modifier = Modifier
                        .clickable { showStartDatePicker = true }
                        .width(180.dp)
                )
            }
        )
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = startDateMillis ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDateMillis = datePickerState.selectedDateMillis
                        showStartDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // End Date Picker
        OutlinedTextField(
            value = endDateMillis?.let { dateFormat.format(Date(it)) } ?: "Select End Date",
            onValueChange = { /* Read Only */ },
            label = { Text("End Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEndDatePicker = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    painterResource(id = R.drawable.end_date),
                    contentDescription = "Select End Date",
                    modifier = Modifier
                        .clickable { showEndDatePicker = true }
                        .width(180.dp)
                )
            }
        )
        if (showEndDatePicker) {
            val initialEndDateMillis = endDateMillis ?: (startDateMillis ?: System.currentTimeMillis())
            val selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return startDateMillis?.let { utcTimeMillis >= it } ?: true
                }
                override fun isSelectableYear(year: Int): Boolean { return true }
            }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialEndDateMillis,
                selectableDates = selectableDates
            )
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endDateMillis = datePickerState.selectedDateMillis
                        showEndDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Image Picker Button
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
            {
            Text("Select Image")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Display Selected Image
        selectedImageUri?.let { uri ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .placeholder(R.drawable.mountains)
                    .error(R.drawable.mountains)
                    .build(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Save Event Button
        Button(
            onClick = {
                // Validate inputs
                if (title.isBlank() || address.isBlank() || startDateMillis == null || endDateMillis == null) {
                    Toast.makeText(context, "Please fill all fields and select dates.", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (endDateMillis!! < startDateMillis!!) {
                    Toast.makeText(context, "End date cannot be before start date.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                isUploading = true

                coroutineScope.launch {
                    var imageUrl: String? = null
                    if (selectedImageUri != null) {
                        imageUrl = uploadImageToImgBB(context, selectedImageUri!!)
                        if (imageUrl == null) {

                            Toast.makeText(context, "Image upload failed. Try again.", Toast.LENGTH_LONG).show()
                            isUploading = false
                            return@launch
                        }
                    }


                    val newEvent = ActiveItem(

                        title = title,
                        address = address,
                        description = description,
                        startDate = startDateMillis!!,
                        endDate = endDateMillis!!,
                        imageUrl = imageUrl
                    )
                    mainViewModel.addItem(newEvent)

                    isUploading = false


                    Toast.makeText(context, "Event Saved!", Toast.LENGTH_SHORT).show()
                    title = ""
                    address = ""
                    description = ""
                    selectedImageUri = null
                    startDateMillis = null
                    endDateMillis = null

                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Event")
            }
        }
    }
}





//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UpcomingScreen(mainViewModel: MainViewModel) {
//    val context = LocalContext.current
//    var title by rememberSaveable { mutableStateOf("") }
//    var address by rememberSaveable { mutableStateOf("") }
//    var description by rememberSaveable { mutableStateOf("") }
//    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
//    var showStartDatePicker by remember { mutableStateOf(false) }
//    var showEndDatePicker by remember { mutableStateOf(false) }
//    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
//    var startDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
//    var endDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
//
//    val photoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickVisualMedia(),
//        onResult = { uri -> selectedImageUri = uri }
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            "Add New Event",
//            style = MaterialTheme.typography.headlineSmall,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        OutlinedTextField(
//            value = title,
//            onValueChange = { title = it },
//            label = { Text("Event Title") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = address,
//            onValueChange = { address = it },
//            label = { Text("Address") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Description") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(120.dp),
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Start Date Picker
//        OutlinedTextField(
//            value = startDateMillis?.let {
//                // Convert UTC millis to local Date for display
//                dateFormat.format(Date(it))
//            } ?: "Select Start Date",
//            onValueChange = { /* Read Only */ },
//            label = { Text("Start Date") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { showStartDatePicker = true },
//            readOnly = true,
//            trailingIcon = {
//                Icon(
//                    painterResource(id = R.drawable.add_ev), // Replace with your calendar icon
//                    contentDescription = "Select Start Date",
//                    modifier = Modifier.clickable { showStartDatePicker = true }
//                )
//            }
//        )
//        if (showStartDatePicker) {
//            val datePickerState = rememberDatePickerState(
//                initialSelectedDateMillis = startDateMillis ?: System.currentTimeMillis()
//            )
//            DatePickerDialog(
//                onDismissRequest = { showStartDatePicker = false },
//                confirmButton = {
//                    TextButton(onClick = {
//                        // Update your state variable when the date is confirmed
//                        // The selectedDateMillis is already in UTC
//                        startDateMillis = datePickerState.selectedDateMillis
//                        showStartDatePicker = false
//                    }) { Text("OK") }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
//                }
//            ) {
//                DatePicker(state = datePickerState)
//            }
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // End Date Picker
//        OutlinedTextField(
//            value = endDateMillis?.let {
//                // Convert UTC millis to local Date for display
//                dateFormat.format(Date(it))
//            } ?: "Select End Date",
//            onValueChange = { /* Read Only */ },
//            label = { Text("End Date") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { showEndDatePicker = true },
//            readOnly = true,
//            trailingIcon = {
//                Icon(
//                    painterResource(id = R.drawable.add_ev), // Replace with your calendar icon
//                    contentDescription = "Select End Date",
//                    modifier = Modifier.clickable { showEndDatePicker = true }
//                )
//            }
//        )
//        if (showEndDatePicker) {
//            // Ensure end date picker doesn't start before the selected start date
//            val initialEndDateMillis = endDateMillis ?: (startDateMillis ?: System.currentTimeMillis())
//            val selectableDates = object : SelectableDates {
//                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
//                    return startDateMillis?.let { utcTimeMillis >= it } ?: true
//                }
//                override fun isSelectableYear(year: Int): Boolean {
//                    return true // Allow all years, or add specific logic
//                }
//            }
//            val datePickerState = rememberDatePickerState(
//                initialSelectedDateMillis = initialEndDateMillis,
//                selectableDates = selectableDates
//            )
//            DatePickerDialog(
//                onDismissRequest = { showEndDatePicker = false },
//                confirmButton = {
//                    TextButton(onClick = {
//                        // Update your state variable when the date is confirmed
//                        // The selectedDateMillis is already in UTC
//                        endDateMillis = datePickerState.selectedDateMillis
//                        showEndDatePicker = false
//                    }) { Text("OK") }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
//                }
//            ) {
//                DatePicker(state = datePickerState)
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (selectedImageUri != null) {
//            AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(selectedImageUri) // The download URL
//                            .crossfade(true)
//                            .placeholder(R.drawable.mountains) // Your placeholder drawable
//                            .error(R.drawable.mountains)       // Your error drawable
//                            .build(),
//                        contentDescription = "Active Item Image",
//                        modifier = Modifier
//                            .sizeIn(maxWidth = 160.dp, maxHeight = 120.dp)
//                            .clip(RoundedCornerShape(8.dp)), // Added clip for rounded corners
//                        contentScale = ContentScale.Crop
//                    )
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//
//        Button(onClick = {
//            photoPickerLauncher.launch(
//                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//            )
//        }) {
//            Text(if (selectedImageUri == null) "Select Image" else "Change Image")
//        }
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                if (title.isNotBlank() && address.isNotBlank() && description.isNotBlank() && startDateMillis != null && endDateMillis != null) {
//                    if (endDateMillis!! < startDateMillis!!) {
//                        Toast.makeText(context, "End date cannot be before start date.", Toast.LENGTH_LONG).show()
//                        return@Button
//                    }
//                    val newItem = ActiveItem(
//                        title = title,
//                        address = address,
//                        description = description,
//                        startDate = startDateMillis!!, // Store as Long (UTC milliseconds)
//                        endDate = endDateMillis!!,   // Store as Long (UTC milliseconds)
//                        imageUrl = selectedImageUri?.toString()
//                    )
//                    mainViewModel.addItem(newItem)
//                    Toast.makeText(context, "Event Saved!", Toast.LENGTH_SHORT).show()
//                    title = ""
//                    address = ""
//                    description = ""
//                    startDateMillis = null
//                    endDateMillis = null
//                    selectedImageUri = null
//                } else {
//                    Toast.makeText(context, "Please fill all fields and select dates.", Toast.LENGTH_LONG).show()
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Saved Event")
//        }
//    }
//}

@Composable
fun PastScreen(mainViewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val activeItemsList: List<ActiveItem> by mainViewModel.allActiveItems.collectAsState()
        val filteredList = activeItemsList.filter { it.endDate!! < Date().time }
        if (filteredList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, top = 16.dp, end = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No past items to display.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { item ->
                    ActiveListItem(item = item)
                }

            }
        }
    }
}
