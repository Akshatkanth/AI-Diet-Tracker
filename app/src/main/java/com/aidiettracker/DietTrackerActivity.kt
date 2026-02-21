package com.aidiettracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class MealEntry(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val mealType: String,
    val time: String
)

class DietTrackerActivity : AppCompatActivity() {

    private val meals = mutableListOf<MealEntry>()
    private var waterCount = 0
    private val dailyCalorieGoal = 2000
    private val dailyProteinGoal = 150
    private val dailyCarbsGoal = 250
    private val dailyFatGoal = 65

    private lateinit var tvDate: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvCaloriesConsumed: TextView
    private lateinit var tvCaloriesGoal: TextView
    private lateinit var tvCaloriesRemaining: TextView
    private lateinit var tvCaloriesPercent: TextView
    private lateinit var progressCalories: ProgressBar
    private lateinit var progressBarCalories: ProgressBar
    private lateinit var tvProteinConsumed: TextView
    private lateinit var tvCarbsConsumed: TextView
    private lateinit var tvFatConsumed: TextView
    private lateinit var progressProtein: ProgressBar
    private lateinit var progressCarbs: ProgressBar
    private lateinit var progressFat: ProgressBar

    private lateinit var etMealName: TextInputEditText
    private lateinit var etCalories: TextInputEditText
    private lateinit var etProtein: TextInputEditText
    private lateinit var etCarbs: TextInputEditText
    private lateinit var etFat: TextInputEditText
    private lateinit var chipGroupMealType: ChipGroup
    private lateinit var btnAddMeal: MaterialButton

    private lateinit var tvMealCount: TextView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutMealsContainer: LinearLayout

    private lateinit var tvWaterCount: TextView
    private lateinit var layoutWaterGlasses: LinearLayout
    private lateinit var progressWater: ProgressBar
    private lateinit var btnAddWater: MaterialButton
    private lateinit var btnRemoveWater: MaterialButton
    private lateinit var btnResetDay: MaterialButton

    private val prefs by lazy { getSharedPreferences("diet_tracker_prefs", Context.MODE_PRIVATE) }
    private val todayKey get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_tracker)
        bindViews()
        setupDate()
        loadData()
        setupListeners()
        buildWaterGlasses()
        refreshUI()
    }

    private fun bindViews() {
        tvDate = findViewById(R.id.tv_date)
        tvStreak = findViewById(R.id.tv_streak)
        tvCaloriesConsumed = findViewById(R.id.tv_calories_consumed)
        tvCaloriesGoal = findViewById(R.id.tv_calories_goal)
        tvCaloriesRemaining = findViewById(R.id.tv_calories_remaining)
        tvCaloriesPercent = findViewById(R.id.tv_calories_percent)
        progressCalories = findViewById(R.id.progress_calories)
        progressBarCalories = findViewById(R.id.progress_bar_calories)
        tvProteinConsumed = findViewById(R.id.tv_protein_consumed)
        tvCarbsConsumed = findViewById(R.id.tv_carbs_consumed)
        tvFatConsumed = findViewById(R.id.tv_fat_consumed)
        progressProtein = findViewById(R.id.progress_protein)
        progressCarbs = findViewById(R.id.progress_carbs)
        progressFat = findViewById(R.id.progress_fat)
        etMealName = findViewById(R.id.et_meal_name)
        etCalories = findViewById(R.id.et_calories)
        etProtein = findViewById(R.id.et_protein)
        etCarbs = findViewById(R.id.et_carbs)
        etFat = findViewById(R.id.et_fat)
        chipGroupMealType = findViewById(R.id.chip_group_meal_type)
        btnAddMeal = findViewById(R.id.btn_add_meal)
        tvMealCount = findViewById(R.id.tv_meal_count)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        layoutMealsContainer = findViewById(R.id.layout_meals_container)
        tvWaterCount = findViewById(R.id.tv_water_count)
        layoutWaterGlasses = findViewById(R.id.layout_water_glasses)
        progressWater = findViewById(R.id.progress_water)
        btnAddWater = findViewById(R.id.btn_add_water)
        btnRemoveWater = findViewById(R.id.btn_remove_water)
        btnResetDay = findViewById(R.id.btn_reset_day)
    }

    private fun setupDate() {
        val sdf = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
        tvDate.text = "Today, ${sdf.format(Date())}"
    }

    private fun setupListeners() {
        btnAddMeal.setOnClickListener { addMeal() }
        btnAddWater.setOnClickListener {
            if (waterCount < 8) {
                waterCount++
                saveData()
                refreshUI()}
        }
        btnRemoveWater.setOnClickListener {
            if (waterCount > 0) {
                waterCount--
                saveData()
                refreshUI()
            }
        }
        btnResetDay.setOnClickListener { showResetDialog() }
    }

    private fun getMealType(): String {
        return when (chipGroupMealType.checkedChipId) {
            R.id.chip_breakfast -> "Breakfast"
            R.id.chip_lunch -> "Lunch"
            R.id.chip_dinner -> "Dinner"
            R.id.chip_snack -> "Snack"
            else -> "Meal"
        }
    }

    private fun addMeal() {
        val name = etMealName.text.toString().trim()
        val caloriesStr = etCalories.text.toString().trim()
        val proteinStr = etProtein.text.toString().trim()
        val carbsStr = etCarbs.text.toString().trim()
        val fatStr = etFat.text.toString().trim()

        if (name.isEmpty()) { etMealName.error = "Enter meal name"; return }
        if (caloriesStr.isEmpty()) { etCalories.error = "Enter calories"; return }

        val meal = MealEntry(
            name = name,
            calories = caloriesStr.toIntOrNull() ?: 0,
            protein = proteinStr.toFloatOrNull() ?: 0f,
            carbs = carbsStr.toFloatOrNull() ?: 0f,
            fat = fatStr.toFloatOrNull() ?: 0f,
            mealType = getMealType(),
            time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )

        meals.add(meal)
        saveData()
        clearInputs()
        refreshUI()
        Toast.makeText(this, "✅ ${meal.name} logged!", Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs() {
        etMealName.text?.clear()
        etCalories.text?.clear()
        etProtein.text?.clear()
        etCarbs.text?.clear()
        etFat.text?.clear()
    }

    private fun refreshUI() {
        val totalCalories = meals.sumOf { it.calories }
        val totalProtein = meals.sumOf { it.protein.toDouble() }.toFloat()
        val totalCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
        val totalFat = meals.sumOf { it.fat.toDouble() }.toFloat()

        val caloriePercent = ((totalCalories.toFloat() / dailyCalorieGoal) * 100).toInt().coerceAtMost(100)
        val remaining = (dailyCalorieGoal - totalCalories).coerceAtLeast(0)

        tvCaloriesConsumed.text = totalCalories.toString()
        tvCaloriesGoal.text = " / $dailyCalorieGoal kcal"
        tvCaloriesRemaining.text = if (totalCalories <= dailyCalorieGoal)
            "$remaining kcal remaining" else "${totalCalories - dailyCalorieGoal} kcal over goal"
        tvCaloriesPercent.text = "$caloriePercent%"
        progressCalories.progress = caloriePercent
        progressBarCalories.progress = caloriePercent

        tvProteinConsumed.text = "${totalProtein.toInt()}g"
        tvCarbsConsumed.text = "${totalCarbs.toInt()}g"
        tvFatConsumed.text = "${totalFat.toInt()}g"
        progressProtein.progress = ((totalProtein / dailyProteinGoal) * 100).toInt().coerceAtMost(100)
        progressCarbs.progress = ((totalCarbs / dailyCarbsGoal) * 100).toInt().coerceAtMost(100)
        progressFat.progress = ((totalFat / dailyFatGoal) * 100).toInt().coerceAtMost(100)

        tvMealCount.text = "${meals.size} meal${if (meals.size != 1) "s" else ""}"
        if (meals.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            layoutMealsContainer.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            layoutMealsContainer.visibility = View.VISIBLE
            rebuildMealCards()
        }

        tvWaterCount.text = "$waterCount / 8 glasses"
        progressWater.progress = waterCount
        updateWaterGlasses()
        updateStreak()
    }

    private fun rebuildMealCards() {
        layoutMealsContainer.removeAllViews()
        meals.forEachIndexed { index, meal ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_meal_card, layoutMealsContainer, false)

            card.findViewById<TextView>(R.id.tv_meal_type_emoji).text = when (meal.mealType) {
                "Breakfast" -> "🌅"
                "Lunch" -> "☀️"
                "Dinner" -> "🌙"
                "Snack" -> "🍎"
                else -> "🍽️"
            }
            card.findViewById<TextView>(R.id.tv_meal_name).text = meal.name
            card.findViewById<TextView>(R.id.tv_meal_type).text = "${meal.mealType} • ${meal.time}"
            card.findViewById<TextView>(R.id.tv_meal_calories).text = "${meal.calories} kcal"
            card.findViewById<TextView>(R.id.tv_meal_macros).text =
                "P: ${meal.protein.toInt()}g  C: ${meal.carbs.toInt()}g  F: ${meal.fat.toInt()}g"
            card.findViewById<ImageButton>(R.id.btn_delete_meal).setOnClickListener {
                meals.removeAt(index)
                saveData()
                refreshUI()
            }

            layoutMealsContainer.addView(card)
        }
    }

    private fun buildWaterGlasses() {
        layoutWaterGlasses.removeAllViews()
        for (i in 1..8) {
            val glass = TextView(this)
            glass.text = "🥤"
            glass.textSize = 24f
            glass.setPadding(4, 0, 4, 0)
            glass.tag = i
            layoutWaterGlasses.addView(glass)
        }
    }

    private fun updateWaterGlasses() {
        for (i in 0 until layoutWaterGlasses.childCount) {
            val glass = layoutWaterGlasses.getChildAt(i) as TextView
            glass.alpha = if (i < waterCount) 1f else 0.25f
        }
    }

    private fun updateStreak() {
        val streak = prefs.getInt("streak_count", 0)
        tvStreak.text = "🔥 $streak days"
    }

    private fun showResetDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogOrange)
            .setTitle("Reset Today's Log")
            .setMessage("This will clear all meals and water for today. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                meals.clear()
                waterCount = 0
                saveData()
                refreshUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveData() {
        val jsonArray = JSONArray()
        meals.forEach { meal ->
            val obj = JSONObject().apply {
                put("name", meal.name)
                put("calories", meal.calories)
                put("protein", meal.protein)
                put("carbs", meal.carbs)
                put("fat", meal.fat)
                put("mealType", meal.mealType)
                put("time", meal.time)
            }
            jsonArray.put(obj)
        }
        prefs.edit()
            .putString("meals_$todayKey", jsonArray.toString())
            .putInt("water_$todayKey", waterCount)
            .apply()

        val lastLogDate = prefs.getString("last_log_date", "")
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(System.currentTimeMillis() - 86400000))
        val currentStreak = prefs.getInt("streak_count", 0)

        if (meals.isNotEmpty()) {
            val newStreak = when (lastLogDate) {
                todayKey -> currentStreak
                yesterday -> currentStreak + 1
                else -> 1
            }
            prefs.edit()
                .putInt("streak_count", newStreak)
                .putString("last_log_date", todayKey)
                .apply()
        }
    }

    private fun loadData() {
        val mealsJson = prefs.getString("meals_$todayKey", "[]") ?: "[]"
        waterCount = prefs.getInt("water_$todayKey", 0)

        val jsonArray = JSONArray(mealsJson)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            meals.add(MealEntry(
                name = obj.getString("name"),
                calories = obj.getInt("calories"),
                protein = obj.getDouble("protein").toFloat(),
                carbs = obj.getDouble("carbs").toFloat(),
                fat = obj.getDouble("fat").toFloat(),
                mealType = obj.getString("mealType"),
                time = obj.getString("time")
            ))
        }
    }
}
