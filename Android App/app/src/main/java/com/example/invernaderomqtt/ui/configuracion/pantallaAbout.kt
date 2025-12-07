package com.example.invernaderomqtt.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invernaderomqtt.R



@Composable
fun PantallaAbout() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📱 Acerca de la Aplicación",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Versión 1.0",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp
            )

            Text(
                text = "Esta aplicación ha sido desarrollada como parte del proyecto Trabajo Fin de Grado de la carrera Ingeniería del Software.",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "En el backend hay una app desarrollada en Arduino que controla todos los sensores y actuadores. Esta app Android se ha creado como una forma sencilla y visual de poder interactuar con el sistema Arduino, facilitando el monitoreo y la actuación remota.",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "Autor: Juan José Gómez Morales",
                color = Color(0xFF81C784),
                fontSize = 14.sp
            )

            Text(
                text = "Contacto: juanjogomez81@gmail.com",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp
            )

            Image(
                painter = painterResource(id = R.drawable.about),
                contentDescription = "Foto de graduación",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

        }
    }
}

