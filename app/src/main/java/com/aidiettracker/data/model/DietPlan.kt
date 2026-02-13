package com.aidiettracker.data.model

data class DietPlan(
    val planName: String = "",
    val caloriesTarget: Int = 0,
    val macroTargets: MacroTargets = MacroTargets(),
    val meals: List<String> = emptyList(),
    val alternatives: List<String> = emptyList()
)

data class MacroTargets(
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0
)
