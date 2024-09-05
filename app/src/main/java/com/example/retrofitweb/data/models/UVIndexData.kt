package com.example.retrofitweb.data.models
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class UVIndexData(
    @SerializedName("IUV") val uvIndex: Double,
    @SerializedName("DATE") val timestamp: String
)
