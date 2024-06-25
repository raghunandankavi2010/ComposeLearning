package com.example.composelearning.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun MyComposable() {
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            // Camera permission granted
        } else {
            // Camera permission denied
        }

        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){
            // Record audio permission granted
        } else {
            // Record audio permission denied
        }
    }

    // Launch the permissions request
    Button(onClick = {
        permissionsLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }) {
        Text("Request Permissions")
    }
}