package com.example.athlos.data.model

// Adicione @Parcelize se quiser passar este objeto entre componentes com o Bundle
// import android.os.Parcelable
// import kotlinx.parcelize.Parcelize

// @Parcelize // Descomente se for usar Parcelable
data class User(
    val nome: String = "",
    val dataNascimento: String = "",
    val idade: String = "",
    val sexo: String = "",
    val peso: String = "",
    val altura: String = "",
    val email: String = "",
    val praticaExercicios: Boolean = false,
    val diasSemana: String = "",
    val meta: String = "" // Adicionado o campo meta
) // : Parcelable // Descomente se for usar Parcelable