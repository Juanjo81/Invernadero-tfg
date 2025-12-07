package com.example.invernaderomqtt.ui.principal

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.invernaderomqtt.navigation.NavegacionApp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ActividadPrincipal : ComponentActivity() {

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // Permitir que Compose dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Forzar modo oscuro en toda la app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContent {
            val controladorNavegacion = rememberNavController()
            val vistaModeloMQTT = VistaModeloMQTT()

            LaunchedEffect(Unit) {
                vistaModeloMQTT.inicializarMQTT(applicationContext)


            }

            // Oscurecer barras del sistema
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Companion.Black,
                    darkIcons = false // texto blanco
                )
            }

            // Tema oscuro completo
            MaterialTheme(
                colors = darkColors(
                    primary = Color(0xFF2E7D32),
                    background = Color.Companion.Black,
                    surface = Color.Companion.Black,
                    onPrimary = Color.Companion.White,
                    onBackground = Color.Companion.White,
                    onSurface = Color.Companion.White
                )
            ) {
                // Fondo negro global
                Box(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .background(Color.Companion.Black)
                ) {
                    NavegacionApp(
                        controlNavegacion = controladorNavegacion,
                        vistaModelo = vistaModeloMQTT
                    )
                }
            }
        }
    }

    // Manejar respuesta del usuario al permiso
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permisos", "Permiso de notificaciones concedido")
            } else {
                Log.w("Permisos", "Permiso de notificaciones denegado")
            }
        }
    }
}