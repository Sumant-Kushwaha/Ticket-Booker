package com.amigo.ticketbooker.model

import java.util.UUID

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

enum class BerthPreference {
    NO_PREFERENCE,
    LOWER,
    MIDDLE,
    UPPER,
    SIDE_LOWER,
    SIDE_UPPER
}

data class Passenger(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int = 18,
    val gender: Gender = Gender.MALE,
    val country: String = "India",
    val berthPreference: BerthPreference = BerthPreference.NO_PREFERENCE,
    val mealPreference: String = "Vegetarian"
)
