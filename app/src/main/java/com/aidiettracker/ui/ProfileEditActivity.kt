package com.aidiettracker.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.aidiettracker.R
import com.aidiettracker.data.BmiCalculator
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.BodyType
import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.Goal
import com.aidiettracker.data.model.UserProfile
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlin.math.round

class ProfileEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EDIT_SECTION = "extra_edit_section"
        const val SECTION_PERSONAL = "personal"
        const val SECTION_FITNESS = "fitness"
        const val SECTION_METRICS = "metrics"
        const val SECTION_TOP = "top"
    }

    private lateinit var scrollView: NestedScrollView
    private lateinit var textEditorTitle: TextView
    private lateinit var textEditorSubtitle: TextView
    private lateinit var textBmiPreview: TextView
    private lateinit var buttonSave: MaterialButton

    private lateinit var inputName: TextInputEditText
    private lateinit var inputAge: TextInputEditText
    private lateinit var inputHeight: TextInputEditText
    private lateinit var inputWeight: TextInputEditText
    private lateinit var inputTargetWeight: TextInputEditText
    private lateinit var dropdownGoal: MaterialAutoCompleteTextView
    private lateinit var dropdownActivity: MaterialAutoCompleteTextView
    private lateinit var dropdownDiet: MaterialAutoCompleteTextView
    private lateinit var dropdownBodyType: MaterialAutoCompleteTextView

    private var profile: UserProfile? = null

    private val numericWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            updateBmiPreview()
            updateSaveState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        bindViews()
        bindDropdowns()
        bindActions()
        loadProfile()
    }

    private fun bindViews() {
        scrollView = findViewById(R.id.profile_edit_scroll)
        textEditorTitle = findViewById(R.id.text_editor_title)
        textEditorSubtitle = findViewById(R.id.text_editor_subtitle)
        textBmiPreview = findViewById(R.id.text_bmi_preview)
        buttonSave = findViewById(R.id.button_save_profile)

        inputName = findViewById(R.id.input_profile_name)
        inputAge = findViewById(R.id.input_profile_age)
        inputHeight = findViewById(R.id.input_profile_height)
        inputWeight = findViewById(R.id.input_profile_weight)
        inputTargetWeight = findViewById(R.id.input_profile_target_weight)
        dropdownGoal = findViewById(R.id.dropdown_profile_goal)
        dropdownActivity = findViewById(R.id.dropdown_profile_activity)
        dropdownDiet = findViewById(R.id.dropdown_profile_diet)
        dropdownBodyType = findViewById(R.id.dropdown_profile_body_type)

        inputHeight.addTextChangedListener(numericWatcher)
        inputWeight.addTextChangedListener(numericWatcher)
        inputTargetWeight.addTextChangedListener(numericWatcher)
        inputName.addTextChangedListener(numericWatcher)
        inputAge.addTextChangedListener(numericWatcher)
    }

    private fun bindDropdowns() {
        setupDropdown(dropdownGoal, listOf("Lose weight", "Maintain weight", "Gain weight"))
        setupDropdown(dropdownActivity, listOf("Sedentary", "Light", "Moderately active", "Very active"))
        setupDropdown(dropdownDiet, listOf("Vegetarian", "Non-vegetarian"))
        setupDropdown(dropdownBodyType, listOf("Ectomorph", "Mesomorph", "Endomorph"))

        listOf(dropdownGoal, dropdownActivity, dropdownDiet, dropdownBodyType).forEach { field ->
            field.setOnItemClickListener { _, _, _, _ ->
                updateSaveState()
            }
        }
    }

    private fun bindActions() {
        findViewById<View>(R.id.button_back).setOnClickListener {
            finishWithSmoothTransition()
        }

        buttonSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfile() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val existing = LocalProfileStore.load(this, currentUid)
        profile = existing

        if (existing == null) {
            Toast.makeText(this, "Complete onboarding first to edit your profile.", Toast.LENGTH_SHORT).show()
            startActivitySmooth(ProfileActivity::class.java)
            finish()
            return
        }

        textEditorTitle.text = "Edit profile"
        textEditorSubtitle.text = "Update your profile once and the dashboard, plan, and tracker will stay in sync."

        inputName.setText(existing.name)
        inputAge.setText(existing.age.takeIf { it > 0 }?.toString().orEmpty())
        inputHeight.setText(existing.heightCm.takeIf { it > 0.0 }?.let { oneDecimal(it) }.orEmpty())
        inputWeight.setText(existing.weightKg.takeIf { it > 0.0 }?.let { oneDecimal(it) }.orEmpty())
        inputTargetWeight.setText(existing.targetWeightKg.takeIf { it > 0.0 }?.let { oneDecimal(it) }.orEmpty())

        dropdownGoal.setText(formatGoal(existing.goal), false)
        dropdownActivity.setText(formatActivity(existing.activityLevel), false)
        dropdownDiet.setText(formatDiet(existing.dietPreference), false)
        dropdownBodyType.setText(formatBodyType(existing.bodyType), false)

        updateEditorCopy()
        updateBmiPreview()
        updateSaveState()
        scrollToRequestedSection()
    }

    private fun updateEditorCopy() {
        when (requestedSection()) {
            SECTION_PERSONAL -> {
                textEditorTitle.text = "Edit personal details"
                textEditorSubtitle.text = "Update your name and age first, then continue to your fitness and metric data."
            }
            SECTION_FITNESS -> {
                textEditorTitle.text = "Edit fitness preferences"
                textEditorSubtitle.text = "Keep your goal, activity level, diet preference, and body type aligned with your routine."
            }
            SECTION_METRICS -> {
                textEditorTitle.text = "Edit body metrics"
                textEditorSubtitle.text = "Adjust measurements so your BMI, dashboard, and diet planning stay accurate."
            }
            else -> {
                textEditorTitle.text = "Edit profile"
                textEditorSubtitle.text = "Update your profile once and the dashboard, plan, and tracker will stay in sync."
            }
        }
    }

    private fun scrollToRequestedSection() {
        val targetView = when (requestedSection()) {
            SECTION_PERSONAL -> findViewById<View>(R.id.section_personal)
            SECTION_FITNESS -> findViewById<View>(R.id.section_fitness)
            SECTION_METRICS -> findViewById<View>(R.id.section_metrics)
            else -> null
        }

        if (targetView == null) {
            scrollView.post { scrollView.smoothScrollTo(0, 0) }
            return
        }

        scrollView.post {
            scrollView.smoothScrollTo(0, (targetView.top - resources.displayMetrics.density.times(8)).toInt())
        }
    }

    private fun requestedSection(): String {
        return intent.getStringExtra(EXTRA_EDIT_SECTION)?.lowercase(Locale.getDefault()) ?: SECTION_TOP
    }

    private fun setupDropdown(field: MaterialAutoCompleteTextView, options: List<String>) {
        field.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, options))
        field.inputType = InputType.TYPE_NULL
    }

    private fun updateBmiPreview() {
        val heightCm = inputHeight.text?.toString()?.trim()?.toDoubleOrNull()
        val weightKg = inputWeight.text?.toString()?.trim()?.toDoubleOrNull()
        val preview = when {
            heightCm == null || heightCm <= 0.0 || weightKg == null || weightKg <= 0.0 -> "BMI preview will appear here"
            else -> {
                val rawBmi = BmiCalculator.calculate(weightKg = weightKg, heightCm = heightCm)
                val bmi = round(rawBmi * 10.0) / 10.0
                "BMI ${oneDecimal(bmi)} (${formatBmiCategory(BmiCalculator.categoryFor(bmi))})"
            }
        }
        textBmiPreview.text = preview
    }

    private fun updateSaveState() {
        val enabled = inputName.text?.toString()?.trim()?.isNotEmpty() == true &&
            inputAge.text?.toString()?.trim()?.toIntOrNull()?.let { it > 0 } == true &&
            inputHeight.text?.toString()?.trim()?.toDoubleOrNull()?.let { it > 0.0 } == true &&
            inputWeight.text?.toString()?.trim()?.toDoubleOrNull()?.let { it > 0.0 } == true &&
            inputTargetWeight.text?.toString()?.trim()?.toDoubleOrNull()?.let { it > 0.0 } == true &&
            dropdownGoal.text?.toString()?.isNotBlank() == true &&
            dropdownActivity.text?.toString()?.isNotBlank() == true &&
            dropdownDiet.text?.toString()?.isNotBlank() == true &&
            dropdownBodyType.text?.toString()?.isNotBlank() == true

        buttonSave.isEnabled = enabled
        buttonSave.alpha = if (enabled) 1f else 0.65f
    }

    private fun saveProfile() {
        val existing = profile ?: return
        val name = inputName.text?.toString()?.trim().orEmpty()
        val age = inputAge.text?.toString()?.trim()?.toIntOrNull()
        val heightCm = inputHeight.text?.toString()?.trim()?.toDoubleOrNull()
        val weightKg = inputWeight.text?.toString()?.trim()?.toDoubleOrNull()
        val targetWeightKg = inputTargetWeight.text?.toString()?.trim()?.toDoubleOrNull()

        if (name.isBlank() || age == null || age <= 0 || heightCm == null || heightCm <= 0.0 || weightKg == null || weightKg <= 0.0 || targetWeightKg == null || targetWeightKg <= 0.0) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val rawBmi = BmiCalculator.calculate(weightKg = weightKg, heightCm = heightCm)
        val bmi = round(rawBmi * 10.0) / 10.0
        val updatedProfile = existing.copy(
            uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
            name = name,
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            targetWeightKg = targetWeightKg,
            goal = parseGoal(dropdownGoal.text?.toString()),
            activityLevel = parseActivity(dropdownActivity.text?.toString()),
            dietPreference = parseDiet(dropdownDiet.text?.toString()),
            bodyType = parseBodyType(dropdownBodyType.text?.toString()),
            bmi = bmi,
            bmiCategory = BmiCalculator.categoryFor(bmi)
        )

        LocalProfileStore.save(this, updatedProfile)
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
        finishWithSmoothTransition()
    }

    private fun parseGoal(value: String?): Goal = when (value) {
        "Lose weight" -> Goal.LOSE
        "Gain weight" -> Goal.GAIN
        else -> Goal.MAINTAIN
    }

    private fun parseActivity(value: String?): ActivityLevel = when (value) {
        "Sedentary" -> ActivityLevel.SEDENTARY
        "Light" -> ActivityLevel.LIGHT
        "Very active" -> ActivityLevel.ACTIVE
        else -> ActivityLevel.MODERATE
    }

    private fun parseDiet(value: String?): DietPreference = when (value) {
        "Vegetarian" -> DietPreference.VEG_ONLY
        else -> DietPreference.NON_VEG
    }

    private fun parseBodyType(value: String?): BodyType = when (value) {
        "Ectomorph" -> BodyType.ECTOMORPH
        "Endomorph" -> BodyType.ENDOMORPH
        else -> BodyType.MESOMORPH
    }

    private fun formatGoal(goal: Goal): String = when (goal) {
        Goal.LOSE -> "Lose weight"
        Goal.MAINTAIN -> "Maintain weight"
        Goal.GAIN -> "Gain weight"
    }

    private fun formatActivity(activity: ActivityLevel): String = when (activity) {
        ActivityLevel.SEDENTARY -> "Sedentary"
        ActivityLevel.LIGHT -> "Light"
        ActivityLevel.MODERATE -> "Moderately active"
        ActivityLevel.ACTIVE -> "Very active"
    }

    private fun formatDiet(dietPreference: DietPreference): String = when (dietPreference) {
        DietPreference.VEG_ONLY -> "Vegetarian"
        DietPreference.NON_VEG -> "Non-vegetarian"
    }

    private fun formatBodyType(bodyType: BodyType): String = when (bodyType) {
        BodyType.ECTOMORPH -> "Ectomorph"
        BodyType.MESOMORPH -> "Mesomorph"
        BodyType.ENDOMORPH -> "Endomorph"
    }

    private fun formatBmiCategory(category: com.aidiettracker.data.model.BmiCategory): String = when (category) {
        com.aidiettracker.data.model.BmiCategory.UNDERWEIGHT -> "Underweight"
        com.aidiettracker.data.model.BmiCategory.NORMAL -> "Healthy"
        com.aidiettracker.data.model.BmiCategory.OVERWEIGHT -> "Overweight"
        com.aidiettracker.data.model.BmiCategory.OBESE -> "Obese"
    }

    private fun oneDecimal(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)
}