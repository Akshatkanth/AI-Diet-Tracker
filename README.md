# AI Based Diet Tracker (Android)

## Overview
AI Based Diet Tracker is an Android app that acts as a virtual diet consultant. It lets users sign up, enter personal details, calculate BMI, receive rule-based diet plans, track daily intake, and view alternative diet suggestions. The backend uses Firebase Authentication and Firebase Firestore.

## Core Features
- User registration & login (Firebase Authentication)
- Personal profile capture (age, height, weight, body type, activity level, goal)
- BMI calculation and category classification
- Rule-based diet plan generation
- Daily diet tracking (meals, calories, macros)
- Alternative diet suggestions based on preferences and constraints

## Suggested App Architecture
- **UI**: XML layouts + Activity/Fragment + ViewModel
- **State**: MVVM with LiveData or StateFlow
- **Data**: Repository pattern for Firestore + local cache (Room optional)
- **Services**: Firebase Auth, Firebase Firestore

## Screen Map
1. **Splash / Auth Gate**
2. **Login**
3. **Register**
4. **Profile Setup** (personal details + goals)
5. **Dashboard** (BMI summary + today’s plan)
6. **Diet Plan** (generated plan + alternatives)
7. **Diet Tracker** (meals logged per day)
8. **Settings / Profile**

## Data Model (Firestore)
- **users/{uid}**
  - name
  - age
  - heightCm
  - weightKg
  - bodyType (Ectomorph/Mesomorph/Endomorph)
  - activityLevel (Sedentary/Light/Moderate/Active)
  - goal (Lose/Maintain/Gain)
  - bmi
  - bmiCategory
  - createdAt
- **users/{uid}/dietPlans/{planId}**
  - planName
  - caloriesTarget
  - macroTargets (protein, carbs, fat)
  - meals (breakfast/lunch/dinner/snacks)
  - alternatives
  - createdAt
- **users/{uid}/dietLogs/{logId}**
  - date
  - meals (list)
  - totalCalories
  - totalMacros

## BMI Logic
- BMI = weightKg / (heightM^2)
- Categories:
  - Underweight: < 18.5
  - Normal: 18.5–24.9
  - Overweight: 25–29.9
  - Obese: >= 30

## Rule-Based Diet Plan (Example)
- If BMI = Underweight and goal = Gain:
  - Higher calories + protein
- If BMI = Normal and goal = Maintain:
  - Balanced calories + macros
- If BMI = Overweight/Obese and goal = Lose:
  - Calorie deficit + higher protein + lower sugar

## Firebase Setup Checklist
1. Create Firebase project
2. Enable Email/Password Authentication
3. Create Firestore database (production or test mode)
4. Download `google-services.json`
5. Add Firebase dependencies in `build.gradle`



## Next Implementation Steps
- Scaffold Android Studio project (Kotlin, XML)
- Create Auth flow and profile setup
- Add BMI logic + rule engine
- Build diet plan and tracking screens
- Persist data in Firestore

## Local Build (Android Studio)
1. Open the project in Android Studio.
2. Add your Firebase `google-services.json` file to the `app/` directory.
3. Sync Gradle and run on an emulator or device.

## Notes
This application provides guidance and should not replace professional medical advice.
