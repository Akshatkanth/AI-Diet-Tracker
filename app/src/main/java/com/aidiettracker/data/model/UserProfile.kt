package com.aidiettracker.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val bodyType: BodyType = BodyType.MESOMORPH,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: Goal = Goal.MAINTAIN,
    val bmi: Double = 0.0,
    val bmiCategory: BmiCategory = BmiCategory.NORMAL
)

enum class BodyType {
    ECTOMORPH,
    MESOMORPH,
    ENDOMORPH
}

enum class ActivityLevel {
    SEDENTARY,
    LIGHT,
    MODERATE,
    ACTIVE
}

enum class Goal {
    LOSE,
    MAINTAIN,
    GAIN
}

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
    OBESE
}
