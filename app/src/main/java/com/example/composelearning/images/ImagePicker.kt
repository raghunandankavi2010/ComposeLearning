package com.example.composelearning.images

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


@Composable
fun MyScreen(modifier: Modifier, selectedUri: Uri?) {
    Column {
            AsyncImage(
                model = selectedUri,
                contentDescription = "Selected Image",
                modifier = modifier.padding(16.dp)
            )
        }
}
