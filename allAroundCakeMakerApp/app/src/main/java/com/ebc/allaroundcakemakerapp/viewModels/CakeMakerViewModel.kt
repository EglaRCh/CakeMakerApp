// CakeMakerViewModel.kt
package com.ebc.allaroundcakemakerapp.viewModels

import android.app.Application
import android.content.Context
import android.database.Cursor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.ebc.allaroundcakemakerapp.data.DatabaseHelper

import com.ebc.allaroundcakemakerapp.models.CupcakeOrderState
import com.ebc.allaroundcakemakerapp.models.SummaryOrder
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext


// Estados para manejo de UI
data class CupcakeOrderState(
    val telefono: String = "",
    val flavor: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val total: Double = 0.0,
    val pickupDate: String = "",
    val extraInstructions: String = "",
    val pickupInstructions: String = ""
)

data class SummaryOrderState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class RegistrationState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)


class CakeMakerViewModel(application: Application) : AndroidViewModel(application) {

    // Estado del pedido de cupcakes
    var state by mutableStateOf(CupcakeOrderState())
        private set

    var registrationState = MutableStateFlow(RegistrationState())
    var summaryOrderState = MutableStateFlow(SummaryOrderState())

    // Función para manejar cambios en los valores del pedido
    fun onValue(value: String, key: String) {
        when (key) {
            "flavor" -> state = state.copy(flavor = value)
            "quantity" -> {
                val qty = value.toIntOrNull() ?: 0
                state = state.copy(quantity = qty)
                calculateTotal()
            }
            "price" -> {
                val prc = value.toDoubleOrNull() ?: 0.0
                state = state.copy(price = prc)
                calculateTotal()
            }
            "pickupDate" -> state = state.copy(pickupDate = value)
            "extraInstructions" -> state = state.copy(extraInstructions = value)
            "pickupInstructions" -> state = state.copy(pickupInstructions = value)
            "telefono" -> state = state.copy(telefono = value)
        }
    }

    private fun calculateTotal() {
        state = state.copy(total = state.quantity * state.price)
    }

    // Función para reiniciar el estado del pedido
    fun reset() {
        state = CupcakeOrderState()
    }

    suspend fun registerUser(context: Context, nombreCompleto: String, telefono: String) {
        withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val db = dbHelper.writableDatabase
            val insertQuery = "INSERT INTO Usuarios (telefono, nombreCompleto) VALUES ('$telefono', '$nombreCompleto')"
            db.execSQL(insertQuery)
            registrationState.value = RegistrationState(isSuccess = true)
        }
    }

    suspend fun saveSummaryOrder(context: Context, order: SummaryOrder) {
        withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val db = dbHelper.writableDatabase
            val insertQuery = """
                INSERT INTO SummaryOrders (
                    telefono, cantidad, sabor, fechaPickup, 
                    instruccionesExtra, instruccionesPickup, total
                ) VALUES (
                    '${order.telefono}', ${order.cantidad}, '${order.sabor}', '${order.fechaPickup}', 
                    '${order.instruccionesExtra}', '${order.instruccionesPickup}', ${order.total}
                )
            """.trimIndent()
            db.execSQL(insertQuery)
            summaryOrderState.value = SummaryOrderState(isSuccess = true)
        }
    }

    suspend fun getOrdersByTelefono(context: Context, telefono: String): List<SummaryOrder> {
        val orders = mutableListOf<SummaryOrder>()
        withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            val db = dbHelper.readableDatabase
            val cursor: Cursor = db.rawQuery("SELECT * FROM SummaryOrders WHERE telefono = ?", arrayOf(telefono))
            if (cursor.moveToFirst()) {
                do {
                    val order = SummaryOrder(
                        id = cursor.getInt(0),
                        telefono = cursor.getString(1),
                        cantidad = cursor.getInt(2),
                        sabor = cursor.getString(3),
                        fechaPickup = cursor.getString(4),
                        instruccionesExtra = cursor.getString(5),
                        instruccionesPickup = cursor.getString(6),
                        total = cursor.getDouble(7)
                    )
                    orders.add(order)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return orders
    }
}

