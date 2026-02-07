package com.aidiettracker.data

import com.aidiettracker.data.model.BmiCategory

object BmiCalculator {
    fun calculate(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        if (heightM <= 0.0) {
            return 0.0
        }
        return weightKg / (heightM * heightM)
    }

    fun categoryFor(bmi: Double): BmiCategory {
        return when {
            bmi < 18.5 -> BmiCategory.UNDERWEIGHT
            bmi < 25.0 -> BmiCategory.NORMAL
            bmi < 30.0 -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }
    }
}
