package com.aidiettracker.data

import com.aidiettracker.data.model.BmiCategory
import com.aidiettracker.data.model.DietPlan
import com.aidiettracker.data.model.Goal
import com.aidiettracker.data.model.MacroTargets

object RuleBasedDietPlanner {
    fun createPlan(bmiCategory: BmiCategory, goal: Goal): DietPlan {
        return when {
            bmiCategory == BmiCategory.UNDERWEIGHT && goal == Goal.GAIN -> {
                DietPlan(
                    planName = "High Calorie Gain",
                    caloriesTarget = 2600,
                    macroTargets = MacroTargets(proteinGrams = 140, carbsGrams = 330, fatGrams = 70),
                    meals = listOf("Oatmeal + nuts", "Chicken rice bowl", "Salmon + quinoa"),
                    alternatives = listOf("Greek yogurt + granola", "Tofu stir-fry")
                )
            }
            bmiCategory == BmiCategory.NORMAL && goal == Goal.MAINTAIN -> {
                DietPlan(
                    planName = "Balanced Maintenance",
                    caloriesTarget = 2100,
                    macroTargets = MacroTargets(proteinGrams = 120, carbsGrams = 250, fatGrams = 60),
                    meals = listOf("Eggs + toast", "Turkey wrap", "Grilled fish + veggies"),
                    alternatives = listOf("Smoothie bowl", "Bean salad")
                )
            }
            (bmiCategory == BmiCategory.OVERWEIGHT || bmiCategory == BmiCategory.OBESE) && goal == Goal.LOSE -> {
                DietPlan(
                    planName = "Lean Cut",
                    caloriesTarget = 1700,
                    macroTargets = MacroTargets(proteinGrams = 130, carbsGrams = 180, fatGrams = 45),
                    meals = listOf("Egg whites + spinach", "Chicken salad", "Lean beef + veggies"),
                    alternatives = listOf("Cottage cheese + berries", "Lentil soup")
                )
            }
            else -> {
                DietPlan(
                    planName = "Personalized Plan",
                    caloriesTarget = 2000,
                    macroTargets = MacroTargets(proteinGrams = 120, carbsGrams = 220, fatGrams = 55),
                    meals = listOf("Overnight oats", "Grilled chicken + greens", "Tofu + veggies"),
                    alternatives = listOf("Protein shake", "Mixed nuts")
                )
            }
        }
    }
}
