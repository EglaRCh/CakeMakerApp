package com.ebc.allaroundcakemakerapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Nombre de la base de datos y versión
private const val DATABASE_NAME = "CakeMakerDB.db"
private const val DATABASE_VERSION = 1

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Definición de las tablas
    private val TABLE_USUARIOS = "Usuarios"
    private val TABLE_SUMMARY_ORDERS = "SummaryOrders"

    // Sentencia SQL para crear la tabla "Usuarios"
    private val CREATE_TABLE_USUARIOS = """
        CREATE TABLE $TABLE_USUARIOS (
            telefono TEXT PRIMARY KEY,
            nombreCompleto TEXT NOT NULL
        )
    """.trimIndent()

    // Sentencia SQL para crear la tabla "SummaryOrders"
    private val CREATE_TABLE_SUMMARY_ORDERS = """
        CREATE TABLE $TABLE_SUMMARY_ORDERS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            telefono TEXT NOT NULL,
            cantidad INTEGER,
            sabor TEXT NOT NULL,
            fechaPickup TEXT NOT NULL,
            instruccionesExtra TEXT,
            instruccionesPickup TEXT,
            total REAL NOT NULL,
            FOREIGN KEY(telefono) REFERENCES $TABLE_USUARIOS(telefono) ON DELETE CASCADE
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear las tablas cuando se crea la base de datos por primera vez
        db?.execSQL(CREATE_TABLE_USUARIOS)
        db?.execSQL(CREATE_TABLE_SUMMARY_ORDERS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Aquí manejamos la actualización de la base de datos si cambia la versión
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUMMARY_ORDERS")
        onCreate(db)
    }
}