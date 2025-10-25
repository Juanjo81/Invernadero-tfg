package com.example.invernaderomqtt.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.invernaderomqtt.Configuracion
import com.example.invernaderomqtt.VistaModeloMQTT
import com.example.invernaderomqtt.ui.PantallaPrincipal
import androidx.compose.runtime.collectAsState



@Composable
fun NavegacionApp(controlNavegacion: NavHostController, vistaModelo: VistaModeloMQTT) {
    NavHost(navController = controlNavegacion, startDestination = "principal") {
        composable("principal") {
            PantallaPrincipal(navController = controlNavegacion, vistaModelo = vistaModelo)
        }
        composable("configuracion") {
            Configuracion.ConfiguracionScreen(
                temperaturaObjetivo = vistaModelo.temperaturaObjetivo.collectAsState().value,
                humedadObjetivo = vistaModelo.humedadObjetivo.collectAsState().value,
                onTemperaturaChange = { vistaModelo.setTemperaturaObjetivo(it) },
                onHumedadChange = { vistaModelo.setHumedadObjetivo(it) }
            )

        }
    }
}
