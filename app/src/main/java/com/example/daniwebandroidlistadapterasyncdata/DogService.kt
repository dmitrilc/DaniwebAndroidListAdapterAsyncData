package com.example.daniwebandroidlistadapterasyncdata

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface DogService {

    @GET("breeds/list/all")
    suspend fun getAllBreeds(): BreedsCall?

    @GET("breed/{breed}/images/random")
    suspend fun getImageUrlByBreed(@Path("breed") breed: String): ImageUrlCall?

    companion object {
        val INSTANCE: DogService = Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(DogService::class.java)
    }
}

data class BreedsCall(
    val message: Map<String, List<String>>?
)

data class ImageUrlCall(
    val message: String?
)