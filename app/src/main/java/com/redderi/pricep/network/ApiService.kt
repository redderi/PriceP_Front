package com.redderi.pricep.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class TextRequest(
    val q: String
)

data class ProductInfo(
    val name: String,
    val price: String,
    val url: String
)

data class TextResponse(
    val answer_text: String,
    val product_info: List<ProductInfo>,
    val main_url: String
)

data class DefineImageResponse(
    val answer_text: String
)

interface ApiService {

    @POST("search-text/")
    fun sendText(@Body text: TextRequest): Call<TextResponse>

    @Multipart
    @POST("search-image/")
    fun sendImage(@Part image: MultipartBody.Part): Call<TextResponse>

    @Multipart
    @POST("define-image/")
    fun defineImage(@Part image: MultipartBody.Part): Call<DefineImageResponse>
}
