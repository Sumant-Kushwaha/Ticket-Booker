package com.amigo.ticketbooker.model

import kotlinx.serialization.Serializable
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

@Serializable
data class Passenger(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int = 18,
    val gender: Gender = Gender.MALE,
    val country: String = "India",
    val berthPreference: BerthPreference = BerthPreference.NO_PREFERENCE,
    val mealPreference: MealPreference = MealPreference.VEGETARIAN,
    val hasValidConcession: Boolean = false,
    val isChild: Boolean = false, // true = child, false = adult
    val childAge: String = "" // Only used when isChild is true
)

enum class MealPreference {
    VEGETARIAN,
    NON_VEGETARIAN,
    JAIN,
    VEGAN,
    NO_MEAL
}
