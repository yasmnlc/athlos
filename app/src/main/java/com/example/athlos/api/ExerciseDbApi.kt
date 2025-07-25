package com.example.athlos.api

import com.example.athlos.ui.models.Exercise
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseDbApi {
    @GET("exercises")
    suspend fun getAllExercises(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String = "exercisedb.p.rapidapi.com",
        @Query("limit") limit: Int = 1500
    ): List<Exercise>

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(
        @Path("bodyPart") bodyPart: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String = "exercisedb.p.rapidapi.com"
    ): List<Exercise>

    @GET("exercises/exercise/{id}")
    suspend fun getExerciseById(
        @Path("id") id: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String = "exercisedb.p.rapidapi.com"
    ): Exercise
}