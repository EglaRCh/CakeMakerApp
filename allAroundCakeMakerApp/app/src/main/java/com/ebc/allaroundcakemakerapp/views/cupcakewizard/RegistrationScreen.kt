// RegistrationScreen.kt
package com.ebc.allaroundcakemakerapp.views.cupcakewizard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

import com.ebc.allaroundcakemakerapp.viewModels.CakeMakerViewModel
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ebc.allaroundcakemakerapp.enums.CakeMakerAppScreenViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    navController: NavController,
    cakeMakerViewModel: CakeMakerViewModel = viewModel()
) {
    // Obtenemos el estado del registro
    val registrationState by cakeMakerViewModel.registrationState.collectAsState()

    // Variables para la entrada de datos
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Manejamos el contexto
    val context = LocalContext.current

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Campo de texto para el nombre completo
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para el número telefónico
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para registrar el usuario
        Button(
            onClick = {
                // Iniciar la corutina para registrar el usuario
                CoroutineScope(Dispatchers.Main).launch {
                    cakeMakerViewModel.registerUser(context, fullName.trim(), phoneNumber.trim())

                    // Verificar si el registro fue exitoso y navegar a la siguiente pantalla
                    if (registrationState.isSuccess) {
                        navController.navigate(CakeMakerAppScreenViews.Start.name) {
                            popUpTo(CakeMakerAppScreenViews.Registration.name) { inclusive = true }
                        }
                    } else {
                        // Mostrar error (podrías usar un snackbar o un dialogo)
                        println("Error en el registro: ${registrationState.error}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !registrationState.isLoading // Deshabilitar mientras carga
        ) {
            if (registrationState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrando...")
            } else {
                Text("Registrarse")
            }
        }

        // Mostrar mensajes de error si los hay
        registrationState.error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    // Si el registro es exitoso, navega a la pantalla "Home"
    if (registrationState.isSuccess) {
        LaunchedEffect(Unit) {
            // Navegación a la pantalla "Home" después del registro exitoso
            navController.navigate(CakeMakerAppScreenViews.Start.name) { // Reemplaza "Home" con el nombre de tu ruta
                popUpTo(0) { inclusive = true }
            }
        }
    }
}