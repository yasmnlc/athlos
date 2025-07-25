package com.example.athlos.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OpenFoodFactsService {

    // 1. Criamos um cliente HTTP customizado com timeouts maiores
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Tempo para estabelecer a conexão
        .readTimeout(30, TimeUnit.SECONDS)    // Tempo para ler os dados da resposta
        .writeTimeout(30, TimeUnit.SECONDS)   // Tempo para enviar os dados
        .build()

    // 2. Construímos o Retrofit usando esse cliente customizado
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .client(okHttpClient) // Usamos o cliente com timeout maior
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenFoodFactsApi = retrofit.create(OpenFoodFactsApi::class.java)
}