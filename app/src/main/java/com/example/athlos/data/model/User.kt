package com.example.athlos.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val meta: String = "",
    val aguaAtual: Int = 0,
    val aguaMeta: Int = 2000,
    val lastResetDate: String = LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE),
    val profileImageUrl: String? = null
)