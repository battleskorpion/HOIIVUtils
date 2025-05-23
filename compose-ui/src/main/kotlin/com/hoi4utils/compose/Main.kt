package com.hoi4utils.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable @Preview
fun App() {
    var count by remember { mutableStateOf(0) }
    MaterialTheme {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("You clicked $count times")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { count++ }) { Text("Click me!") }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose UI") {
        App()
    }
}