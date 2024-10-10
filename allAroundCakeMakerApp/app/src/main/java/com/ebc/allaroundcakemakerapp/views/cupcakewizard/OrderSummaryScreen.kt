package com.ebc.allaroundcakemakerapp.views.cupcakewizard

import android.app.NotificationManager
import android.content.Context

import android.content.Intent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import com.ebc.allaroundcakemakerapp.R
import com.ebc.allaroundcakemakerapp.enums.CakeMakerAppScreenViews
import com.ebc.allaroundcakemakerapp.models.SummaryOrder
import com.ebc.allaroundcakemakerapp.viewModels.CakeMakerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun OrderSummaryScreen(navController: NavController, cakeMakerViewModel: CakeMakerViewModel) {
    val context = LocalContext.current
    val resources = context.resources

    // Obtener el estado del resumen del pedido
    val summaryOrderState by cakeMakerViewModel.summaryOrderState.collectAsState()

    val numberOfCupcakes = resources.getQuantityString(
        R.plurals.cupcakes,
        cakeMakerViewModel.state.quantity,
        cakeMakerViewModel.state.quantity
    )

    val orderSummary = stringResource(
        R.string.order_details,
        numberOfCupcakes,
        cakeMakerViewModel.state.flavor,
        cakeMakerViewModel.state.pickupDate,
        cakeMakerViewModel.state.quantity,
        cakeMakerViewModel.state.extraInstructions,
        cakeMakerViewModel.state.pickupInstructions
    )
    val newOrder = stringResource(R.string.new_cupcake_order)

    val items = listOf(
        Pair(stringResource(R.string.quantity), numberOfCupcakes),
        Pair(stringResource(R.string.flavor), cakeMakerViewModel.state.flavor),
        Pair(stringResource(R.string.pickup_date), cakeMakerViewModel.state.pickupDate),
        Pair(stringResource(R.string.extra_instructions), cakeMakerViewModel.state.extraInstructions),
        Pair(stringResource(R.string.pickup_instructions), cakeMakerViewModel.state.pickupInstructions)
    )

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach {
                Text(it.first.uppercase())
                Text(text = it.second, fontWeight = FontWeight.Bold)
                Divider(thickness = 1.dp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.subtotal_price, cakeMakerViewModel.state.total),
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { shareOrder(context, newOrder, orderSummary) }
                ) {
                    Text(stringResource(R.string.send))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        sendNotification(context, "¡Tu pedido ha sido finalizado!", "Detalles: $orderSummary")
                        // Guardar el resumen del pedido en la base de datos
                        CoroutineScope(Dispatchers.Main).launch {
                            cakeMakerViewModel.saveSummaryOrder(context, SummaryOrder(
                                telefono = cakeMakerViewModel.state.telefono,
                                cantidad = cakeMakerViewModel.state.quantity,
                                sabor = cakeMakerViewModel.state.flavor,
                                fechaPickup = cakeMakerViewModel.state.pickupDate,
                                instruccionesExtra = cakeMakerViewModel.state.extraInstructions,
                                instruccionesPickup = cakeMakerViewModel.state.pickupInstructions,
                                total = cakeMakerViewModel.state.total
                            ))

                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(stringResource(R.string.end))
                }

            }
        }

        // Si el pedido se guarda exitosamente, navegar a la pantalla Finish
        if (summaryOrderState.isSuccess) {
            LaunchedEffect(Unit) {
                navController.navigate(CakeMakerAppScreenViews.Finish.name) {
                    popUpTo(CakeMakerAppScreenViews.OrderSummary.name) { inclusive = true }
                }
            }
        }

        // Mostrar errores si existen
        summaryOrderState.error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                // Mostrar un mensaje de error (podrías agregar un snackbar u otro mecanismo de alerta)
                println("Error: $errorMessage")
            }
        }
    }
}

private fun shareOrder(context: Context, subject: String, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}

private fun sendNotification(context: Context, title: String, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notificationId = 1 // ID único para la notificación

    val notificationBuilder = android.app.Notification.Builder(context, "default_channel")
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Ícono por defecto
        .setAutoCancel(true)

    notificationManager.notify(notificationId, notificationBuilder.build())
}