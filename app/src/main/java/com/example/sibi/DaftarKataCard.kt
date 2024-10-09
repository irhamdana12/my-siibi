package com.example.sibi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlin.text.contains


@Composable
fun DaftarKataCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.kata),
                contentDescription = "Daftar Kata Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Penerjemah Bahasa Indonesia ke Bahasa SIBI",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate("daftarKata") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADEF))
                ) {
                    Text("Kunjungi")
                }
            }
        }
    }
}
@Composable
fun DaftarKataScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var specialOutputs by remember { mutableStateOf(mutableListOf<KataItem>()) }
    var showNotFoundMessage by remember { mutableStateOf(false) } // State for not found message
    // Data for images and descriptions
    val kataList = listOf(
        KataItem(R.drawable.a, "a"),
        KataItem(R.drawable.i, "i"),
        KataItem(R.drawable.u, "u"),
        KataItem(R.drawable.e, "e"),
        KataItem(R.drawable.o, "o"),
        KataItem(R.drawable.baik, "baik"),
        KataItem(R.drawable.bangku, "bangku"),
        KataItem(R.drawable.bel, "bel"),
        KataItem(R.drawable.dia, "dia"),
        KataItem(R.drawable.meja, "meja"),
        KataItem(R.drawable.pramuka, "pramuka"),
        KataItem(R.drawable.sakit, "sakit"),
        KataItem(R.drawable.saya, "saya"),
        KataItem(R.drawable.teman, "teman"),
        KataItem(R.drawable.tugas, "tugas"),
    )

    // Split the search query into keywords
    val keywords = searchQuery.split(" ").filter { it.isNotEmpty() }
    val filteredList = kataList.filter { kataItem ->
        keywords.all { keyword ->
            kataItem.title.contains(keyword, ignoreCase = true)
        }
    }

    fun performSearch() {
        specialOutputs.clear()
        showNotFoundMessage = false // Reset not found message

        searchQuery.split(" ").forEach { word ->
            val foundItem = kataList.find { it.title.equals(word, ignoreCase = true) }
            if (foundItem != null) {
                specialOutputs.add(foundItem)
            } else if (word.isNotEmpty()) {
                showNotFoundMessage = true // Set not found message if word not found
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBar(searchQuery) { query ->
            searchQuery = query
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to trigger the search
        Button(onClick = { performSearch() }) {
            Text("Cari Kata")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display "Kata tidak tersedia" if applicable
        if (showNotFoundMessage) {
            Text("Kata tidak tersedia", color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Display search results
        if (specialOutputs.isNotEmpty()) {
            specialOutputs.forEach { item ->
                KataCard(item)
                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}
// SearchBar Composable untuk menampilkan input pencarian
@Composable
fun SearchBar(searchQuery: String, onQueryChanged: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = onQueryChanged,
        label = { Text("Cari kata isyarat...") },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF0F0F0),
            unfocusedContainerColor = Color(0xFFF0F0F0),
            disabledContainerColor = Color(0xFFF0F0F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* Implement action */ })
    )
}

// KataCard untuk menampilkan gambar dan keterangan
@Composable
fun KataCard(kataItem: KataItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = kataItem.imageRes),
                contentDescription = kataItem.title,
                modifier = Modifier
                    .width(160.dp)  // Lebar gambar 150 dp
                    .height(150.dp) // Tinggi gambar 100 dp
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = kataItem.title,
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// Data class untuk menyimpan informasi gambar dan keterangan
data class KataItem(val imageRes: Int, val title: String)

@Preview(showBackground = true)
@Composable
fun DaftarKataScreenPreview() {
    DaftarKataScreen()
}
