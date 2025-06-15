package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: Int) {
        data object Active : Screen("Active", R.string.upcoming_button, R.drawable.ic_launcher_foreground)
        data object Upcoming : Screen("Upcoming", R.string.add_button, R.drawable.ic_launcher_foreground)
        data object Past : Screen("Past", R.string.past_button, R.drawable.ic_launcher_foreground)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
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
                    Text(text = currentScreen?.let { stringResource(id = it.resourceId) } ?: "")
                }
            )
        },
        bottomBar = { // Or use a TabRow if you prefer tabs at the top
            NavigationBar { // Using NavigationBar for bottom navigation style, adapt if you want tabs
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.icon), contentDescription = stringResource(id = screen.resourceId)) },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainActivity.Screen.Active.route, // Your initial screen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainActivity.Screen.Active.route) { ActiveScreen() } // Replace with your actual screen composables
            composable(MainActivity.Screen.Upcoming.route) { UpcomingScreen() }
            composable(MainActivity.Screen.Past.route) { PastScreen() }
        }
    }
}

data class ActiveItem(
    // Example data class
    val id: String,
    val title: String,
    val startDate: Date,
    val endDate: Date,
    val address: String,
    val description: String,
    val image: ImageBitmap? = null,
)

@Composable
fun ActiveScreen() {
  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top
  ) {
      Text(
          stringResource(R.string.upcoming_title)
      )
      val activeItemsList = remember {
          listOf(
              ActiveItem("1", "Item 1", Date(), Date(), "Address 1", "Description 1"),
              ActiveItem("2", "Item 2", Date(), Date(), "Address 1", "Description 1"),
              ActiveItem("3", "Item 3", Date(), Date(), "Address 1", "Description 1"),
              ActiveItem("4", "Item 4", Date(), Date(), "Address 1", "Description 1"),
              // Add more items as needed
          )
      }

      if (activeItemsList.isEmpty()) {
          // Optional: Show a message if the list is empty
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
              verticalArrangement = Arrangement.spacedBy(8.dp) // Adds space between items
          ) {
              items(activeItemsList) { item -> // Loop through your data
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
        //elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Start,
              modifier = Modifier.fillMaxWidth()
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
                if (item.image != null) {
                    Image(
                        bitmap = item.image,
                        contentDescription = null,
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

//@Preview(showBackground = true)
//@Composable
//fun ActiveScreenPreview() {
//    MyApplicationTheme { // Make sure to wrap with your app's theme
//        ActiveScreen()
//    }
//}

@Composable
fun UpcomingScreen() {
    Text("Upcoming Screen Content")
}

@Composable
fun PastScreen() {
    Text("Past Screen Content")
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}