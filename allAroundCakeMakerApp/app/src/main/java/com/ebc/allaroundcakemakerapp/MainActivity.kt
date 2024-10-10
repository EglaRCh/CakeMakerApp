package com.ebc.allaroundcakemakerapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.compose.ui.Modifier
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import com.ebc.allaroundcakemakerapp.navigation.NavManager
import com.ebc.allaroundcakemakerapp.ui.theme.AllAroundCakeMakerAppTheme
import com.ebc.allaroundcakemakerapp.viewModels.CakeMakerViewModel



    class MainActivity : ComponentActivity() {

        private val cakeMakerViewModel: CakeMakerViewModel by viewModels()
        private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Inicializar el lanzador de permisos
            requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permiso concedido, crear el canal de notificación
                    createNotificationChannel()
                } else {
                    // Permiso denegado, mostrar mensaje y cerrar la aplicación
                    Toast.makeText(
                        this,
                        "Permiso de notificaciones necesario para continuar. La aplicación se cerrará.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish() // Cerrar la aplicación si el permiso es rechazado
                }
            }

            // Verificar y solicitar permiso de notificaciones si es necesario
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Solicitar el permiso
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Permiso ya concedido, inicializar la app
                    createNotificationChannel()
                }
            } else {
                // Versiones de Android inferiores a 13, inicializar la app directamente
                createNotificationChannel()
            }

            // Configurar la UI usando Compose
            setContent {
                AllAroundCakeMakerAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavManager(cakeMakerViewModel = cakeMakerViewModel)
                    }
                }
            }
        }

        private fun createNotificationChannel() {
            val channel = NotificationChannel(
                "default_channel",
                "Default Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for default notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

