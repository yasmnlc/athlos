package com.example.athlos.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val nome: String = "",
    val dataNascimento: String = "",
    val idade: String = "",
    val sexo: String = "",
    val peso: String = "",
    val altura: String = "",
    val email: String = "",
    val praticaExercicios: Boolean = false,
    val diasSemana: String = "",
    val goal: String? = null,
    val aguaAtual: Int = 0,
    val aguaMeta: Int = 2000,
    val lastResetDate: String = LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE),
    val profileImageUrl: String? = null,
    @PropertyName("favoriteWorkouts")
    val savedWorkouts: List<String> = emptyList()
) {
    @Suppress("unused")
    constructor() : this(
        uid = "", nome = "", dataNascimento = "", idade = "", sexo = "",
        peso = "", altura = "", email = "", praticaExercicios = false, diasSemana = "",
        goal = "", aguaAtual = 0, aguaMeta = 2000,
        lastResetDate = LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE), profileImageUrl = null,
        savedWorkouts = emptyList()
    )

    @Exclude
    fun getPesoAsFloat(): Float? {
        return peso.toFloatOrNull()
    }

    @Exclude
    fun getAlturaAsFloat(): Float? {
        return altura.toFloatOrNull()
    }
}