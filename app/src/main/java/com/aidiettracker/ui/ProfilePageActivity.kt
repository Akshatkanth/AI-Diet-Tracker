package com.aidiettracker.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.aidiettracker.DietTrackerActivity
import com.aidiettracker.R
import com.aidiettracker.data.BmiCalculator
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.BodyType
import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.Goal
import com.aidiettracker.data.model.UserProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlin.math.round

class ProfilePageActivity : AppCompatActivity() {

    private var currentProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        bindNavigation()
        bindActions()
        bindProfile()
    }

    override fun onResume() {
        super.onResume()
        bindProfile()
    }

    private fun bindActions() {
        findViewById<View>(R.id.button_edit_profile).setOnClickListener {
            openProfileEditor()
        }
        findViewById<View>(R.id.button_complete_profile).setOnClickListener {
            startActivitySmooth(ProfileActivity::class.java)
        }

        findViewById<View>(R.id.button_edit_personal).setOnClickListener {
            openProfileEditor()
        }
        findViewById<View>(R.id.button_edit_fitness).setOnClickListener {
            openProfileEditor()
        }
        findViewById<View>(R.id.button_edit_metrics).setOnClickListener {
            openProfileEditor()
        }

        findViewById<View>(R.id.button_edit_profile).attachTapFeedback()
        findViewById<View>(R.id.button_complete_profile).attachTapFeedback()
        findViewById<View>(R.id.button_edit_personal).attachTapFeedback()
        findViewById<View>(R.id.button_edit_fitness).attachTapFeedback()
        findViewById<View>(R.id.button_edit_metrics).attachTapFeedback()
    }

    private fun bindNavigation() {
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            startTabActivitySmooth(DashboardActivity::class.java)
        }
        findViewById<LinearLayout>(R.id.nav_view_plan).setOnClickListener {
            startTabActivitySmooth(DietPlanActivity::class.java)
        }
        findViewById<LinearLayout>(R.id.nav_track_diet).setOnClickListener {
            startTabActivitySmooth(DietTrackerActivity::class.java)
        }
        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            findViewById<NestedScrollView>(R.id.profile_page_scroll).smoothScrollTo(0, 0)
        }
        findViewById<FrameLayout>(R.id.nav_quick_actions).setOnClickListener {
            startActivitySmooth(DietTrackerActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_home).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_view_plan).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_track_diet).attachTapFeedback()
        findViewById<LinearLayout>(R.id.nav_profile).attachTapFeedback()
        findViewById<FrameLayout>(R.id.nav_quick_actions).attachTapFeedback()
    }

    private fun bindProfile() {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val profile = LocalProfileStore.load(this, currentUid)
        currentProfile = profile
        val completeProfileCta = findViewById<View>(R.id.button_complete_profile)
        val editProfileButton = findViewById<View>(R.id.button_edit_profile)
        val editPersonalButton = findViewById<View>(R.id.button_edit_personal)
        val editFitnessButton = findViewById<View>(R.id.button_edit_fitness)
        val editMetricsButton = findViewById<View>(R.id.button_edit_metrics)
        val subtitle = findViewById<TextView>(R.id.text_profile_subtitle)

        if (profile == null) {
            subtitle.text = "No profile found yet. Complete the onboarding questions to personalize your plan."
            completeProfileCta.visibility = View.VISIBLE
            editProfileButton.visibility = View.GONE
            editPersonalButton.visibility = View.GONE
            editFitnessButton.visibility = View.GONE
            editMetricsButton.visibility = View.GONE

            setText(R.id.text_profile_name, "-")
            setText(R.id.text_profile_age, "-")
            setText(R.id.text_profile_goal, "-")
            setText(R.id.text_profile_activity, "-")
            setText(R.id.text_profile_height, "-")
            setText(R.id.text_profile_weight, "-")
            setText(R.id.text_profile_target_weight, "-")
            setText(R.id.text_profile_diet, "-")
            setText(R.id.text_profile_body_type, "-")
            setText(R.id.text_profile_bmi, "-")
            return
        }

        subtitle.text = "Your onboarding answers are saved here."
        completeProfileCta.visibility = View.GONE
        editProfileButton.visibility = View.VISIBLE
        editPersonalButton.visibility = View.VISIBLE
        editFitnessButton.visibility = View.VISIBLE
        editMetricsButton.visibility = View.VISIBLE

        setText(R.id.text_profile_name, profile.name)
        setText(R.id.text_profile_age, profile.age.toString())
        setText(R.id.text_profile_goal, formatGoal(profile.goal))
        setText(R.id.text_profile_activity, formatActivity(profile.activityLevel))
        setText(R.id.text_profile_height, "${oneDecimal(profile.heightCm)} cm")
        setText(R.id.text_profile_weight, "${oneDecimal(profile.weightKg)} kg")
        setText(R.id.text_profile_target_weight, "${oneDecimal(profile.targetWeightKg)} kg")
        setText(R.id.text_profile_diet, formatDiet(profile.dietPreference))
        setText(R.id.text_profile_body_type, formatBodyType(profile.bodyType))
        setText(
            R.id.text_profile_bmi,
            "${oneDecimal(profile.bmi)} (${profile.bmiCategory.name.lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }})"
        )
    }

    private fun openProfileEditor() {
        val profile = currentProfile ?: LocalProfileStore.load(this, FirebaseAuth.getInstance().currentUser?.uid)
        if (profile == null) {
            startActivitySmooth(ProfileActivity::class.java)
            return
        }

        val editorView = LayoutInflater.from(this).inflate(R.layout.dialog_profile_editor, null)
        val nameInput = editorView.findViewById<TextInputEditText>(R.id.input_profile_name)
        val ageInput = editorView.findViewById<TextInputEditText>(R.id.input_profile_age)
        val heightInput = editorView.findViewById<TextInputEditText>(R.id.input_profile_height)
        val weightInput = editorView.findViewById<TextInputEditText>(R.id.input_profile_weight)
        val targetWeightInput = editorView.findViewById<TextInputEditText>(R.id.input_profile_target_weight)
        val goalInput = editorView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_profile_goal)
        val activityInput = editorView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_profile_activity)
        val dietInput = editorView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_profile_diet)
        val bodyTypeInput = editorView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdown_profile_body_type)

        nameInput.setText(profile.name)
        ageInput.setText(profile.age.takeIf { it > 0 }?.toString().orEmpty())
        heightInput.setText(profile.heightCm.takeIf { it > 0.0 }?.let { oneDecimal(it) }.orEmpty())
        weightInput.setText(profile.weightKg.takeIf { it > 0.0 }?.let { oneDecimal(it) }.orEmpty())
        targetWeightInput.setText(profile.targetWeightKg.takeIf { it > 0.0 }?.let { oneDecimal(it) }.orEmpty())

        val goalLabels = listOf("Lose weight", "Maintain weight", "Gain weight")
        val activityLabels = listOf("Sedentary", "Light", "Moderately active", "Very active")
        val dietLabels = listOf("Vegetarian", "Non-vegetarian")
        val bodyTypeLabels = listOf("Ectomorph", "Mesomorph", "Endomorph")

        setupDropdown(goalInput, goalLabels, formatGoal(profile.goal))
        setupDropdown(activityInput, activityLabels, formatActivity(profile.activityLevel))
        setupDropdown(dietInput, dietLabels, formatDiet(profile.dietPreference))
        setupDropdown(bodyTypeInput, bodyTypeLabels, formatBodyType(profile.bodyType))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Edit profile")
            .setView(editorView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text?.toString()?.trim().orEmpty()
                val age = ageInput.text?.toString()?.trim()?.toIntOrNull()
                val heightCm = heightInput.text?.toString()?.trim()?.toDoubleOrNull()
                val weightKg = weightInput.text?.toString()?.trim()?.toDoubleOrNull()
                val targetWeightKg = targetWeightInput.text?.toString()?.trim()?.toDoubleOrNull()

                if (name.isBlank() || age == null || age <= 0 || heightCm == null || heightCm <= 0.0 || weightKg == null || weightKg <= 0.0 || targetWeightKg == null || targetWeightKg <= 0.0) {
                    Toast.makeText(this, "Please complete all profile fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedProfile = profile.copy(
                    uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                    name = name,
                    age = age,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    targetWeightKg = targetWeightKg,
                    goal = parseGoal(goalInput.text?.toString()),
                    activityLevel = parseActivity(activityInput.text?.toString()),
                    dietPreference = parseDiet(dietInput.text?.toString()),
                    bodyType = parseBodyType(bodyTypeInput.text?.toString())
                ).withUpdatedMetrics()

                LocalProfileStore.save(this, updatedProfile)
                currentProfile = updatedProfile
                bindProfile()
                findViewById<NestedScrollView>(R.id.profile_page_scroll).smoothScrollTo(0, 0)
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun setupDropdown(field: MaterialAutoCompleteTextView, options: List<String>, selected: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        field.setAdapter(adapter)
        field.setText(selected, false)
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

    private fun UserProfile.withUpdatedMetrics(): UserProfile {
        val rawBmi = BmiCalculator.calculate(weightKg = weightKg, heightCm = heightCm)
        val bmi = round(rawBmi * 10.0) / 10.0
        return copy(
            bmi = bmi,
            bmiCategory = BmiCalculator.categoryFor(bmi)
        )
    }

    private fun setText(id: Int, value: String) {
        findViewById<TextView>(id).text = value
    }

    private fun oneDecimal(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

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
}

