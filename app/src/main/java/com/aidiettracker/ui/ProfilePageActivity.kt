package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.aidiettracker.DietTrackerActivity
import com.aidiettracker.R
import com.aidiettracker.data.local.LocalProfileStore
import com.aidiettracker.data.model.ActivityLevel
import com.aidiettracker.data.model.BodyType
import com.aidiettracker.data.model.DietPreference
import com.aidiettracker.data.model.Goal
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ProfilePageActivity : AppCompatActivity() {

    private companion object {
        const val EXTRA_EDIT_SECTION = "extra_edit_section"
        const val SECTION_PERSONAL = "personal"
        const val SECTION_FITNESS = "fitness"
        const val SECTION_METRICS = "metrics"
        const val SECTION_TOP = "top"
    }

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
            openEditor(SECTION_TOP)
        }
        findViewById<View>(R.id.button_complete_profile).setOnClickListener {
            startActivitySmooth(ProfileActivity::class.java)
        }
        findViewById<View>(R.id.button_logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthEntryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        findViewById<View>(R.id.button_edit_personal).setOnClickListener {
            openEditor(SECTION_PERSONAL)
        }
        findViewById<View>(R.id.button_edit_fitness).setOnClickListener {
            openEditor(SECTION_FITNESS)
        }
        findViewById<View>(R.id.button_edit_metrics).setOnClickListener {
            openEditor(SECTION_METRICS)
        }

        findViewById<View>(R.id.button_edit_profile).attachTapFeedback()
        findViewById<View>(R.id.button_complete_profile).attachTapFeedback()
        findViewById<View>(R.id.button_logout).attachTapFeedback()
        findViewById<View>(R.id.button_edit_personal).attachTapFeedback()
        findViewById<View>(R.id.button_edit_fitness).attachTapFeedback()
        findViewById<View>(R.id.button_edit_metrics).attachTapFeedback()
    }

    private fun openEditor(section: String) {
        val intent = android.content.Intent(this, ProfileEditActivity::class.java).apply {
            putExtra(EXTRA_EDIT_SECTION, section)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

