package com.aidiettracker.repository

import com.aidiettracker.data.model.DietLog
import com.aidiettracker.data.model.DietPlan
import com.aidiettracker.data.model.UserProfile

interface FirestoreRepository {
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun loadUserProfile(uid: String): UserProfile?
    suspend fun saveDietPlan(uid: String, plan: DietPlan)
    suspend fun saveDietLog(uid: String, log: DietLog)
}
