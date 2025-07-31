package com.example.athlos.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OpenFoodFactsService {

    // cliente HTTP customizado com timeouts maiores
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // tempo para estabelecer a conexão
        .readTimeout(30, TimeUnit.SECONDS)    // tempo para ler os dados da resposta
        .writeTimeout(30, TimeUnit.SECONDS)   // tempo para enviar os dados
        .build()

    // Retrofit usando esse cliente customizado
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .client(okHttpClient) // cliente com timeout maior
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenFoodFactsApi = retrofit.create(OpenFoodFactsApi::class.java)
}