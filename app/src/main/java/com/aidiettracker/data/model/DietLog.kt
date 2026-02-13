package com.aidiettracker.data.model

data class DietLog(
    val date: String = "",
    val meals: List<String> = emptyList(),
    val totalCalories: Int = 0,
    val totalMacros: MacroTargets = MacroTargets()
)
