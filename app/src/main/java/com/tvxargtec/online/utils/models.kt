package com.tvxargtec.online.utils

data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = ""
)

data class Channel(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val logo: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val isLive: Boolean = true
)

data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
)

data class User(
    val id: String = "",
    val email: String = "",
    val token: String = "",
    val isVip: Boolean = false,
    val vipExpiry: String = "",
    val points: Int = 0,
    val benefits: List<String> = emptyList(),
    val avatar: String = "",
    val name: String = ""
)

data class VipPlan(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val duration: String = "",
    val features: List<String> = emptyList()
)

// Puntos y Beneficios del Usuario
data class UserBenefits(
    val userId: String = "",
    val totalPoints: Int = 0,
    val pointsHistory: List<PointTransaction> = emptyList(),
    val activeBenefits: List<Benefit> = emptyList(),
    val vipStatus: String = "free",
    val vipExpiryDate: String = "",
    val watchedHours: Int = 0,
    val favoriteChannels: Int = 0
)

// Transacción de Puntos
data class PointTransaction(
    val id: String = "",
    val amount: Int = 0,
    val type: String = "",
    val description: String = "",
    val date: String = "",
    val expiryDate: String = ""
)

// Beneficio del Usuario
data class Benefit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val expiryDate: String = "",
    val isActive: Boolean = true
)

// Historial de Visualización
data class WatchHistory(
    val channelId: String = "",
    val channelTitle: String = "",
    val watchedAt: String = "",
    val duration: Long = 0
)

// Favorito
data class Favorite(
    val channelId: String = "",
    val channelTitle: String = "",
    val logo: String = "",
    val addedAt: String = ""
)

// EPG (Electronic Program Guide)
data class EpgNowResponse(
    val code: Int = 0,
    val data: List<EpgChannelData> = emptyList()
)

data class EpgChannelData(
    val channelId: String = "",
    val current: EpgProgramme? = null,
    val next: EpgProgramme? = null
)

data class EpgProgramme(
    val title: String = "",
    val desc: String = "",
    val category: String = "",
    val start: String = "",
    val stop: String = ""
)
