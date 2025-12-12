Project: FitSnack – Smart Fitness and Snack Tracker

Overview
--------
FitSnack is a simple, mobile-first Android app focused on quick snack and workout logging to help users track calories eaten, calories burned, and net calories.

Brand
-----
- Primary gradient: green (#00C853) to orange (#FF6D00)
- Primary green: #00C853
- Accent orange: #FF6D00
- Neutral background: #F5F5F5
- White content surface: #FFFFFF

Design Goals
------------
- Fast snack/workout capture from the dashboard
- Clean, legible calorie metrics
- Guest mode to reduce onboarding friction
- Scalable UI using Material 3 components

Screens & Wireframes
--------------------
1) Login Screen
   - Title, Email, Password
   - Buttons: Sign in, Sign up, Continue as Guest
   - Use a small gradient header bar with app logo/text

2) Dashboard
   - Top: greeting + date
   - Card row with three large metric chips: Calories Burned, Calories Eaten, Net Calories
   - FAB (floating action button) to add Snack/Workout (speed-dial style or modal dialog)
   - Recent history list below, tappable items to edit

3) Add Snack
   - Fields: Snack Name (text), Calories (number), Portion Size (text), Date (date picker)
   - Buttons: Save, Cancel

Component Library
-----------------
- AppBar: small top app bar with gradient background
- MetricCard: shows title, numeric value, and optional trend icon
- Primary FAB: circular with gradient background and white icon
- Input fields: OutlinedTextField with hint and leading icons when helpful

Color & Typography
------------------
- Use Material3 typography scales (titleLarge, bodyMedium, labelSmall)
- High contrast between text and surfaces
- Use green for positive/healthy indicators and orange for energetic call-to-action buttons

Data Model (local-first)
------------------------
- SnackEntry {
    id: long,
    name: string,
    calories: int,
    portion: string,
    date: ISO8601 string
  }
- WorkoutEntry {
    id: long,
    name: string,
    caloriesBurned: int,
    durationMinutes: int,
    date: ISO8601 string
  }

Persistence
-----------
- Start with Room for local persistence
- Entities for SnackEntry and WorkoutEntry
- DAOs for insert, update, delete, list (by day)

Navigation
----------
- StartDestination: LoginFragment
- DashboardFragment
- AddSnackFragment / AddWorkoutFragment
- Use single-activity + fragments or Jetpack Compose screens (recommend Compose for quick iteration)

Accessibility
-------------
- Ensure touch targets >= 48dp
- Provide content descriptions for icons
- Respect font scaling and high contrast modes

Android Implementation Notes
----------------------------
- Recommend using Material3 and Jetpack libraries (Navigation, Room, LiveData/Flow)
- Theme tokens already added: colors.xml and themes.xml updated to include brand tokens
- Added drawable `res/drawable/fit_gradient.xml` for linear gradient header/FAB usage

Next Steps (implementation plan)
--------------------------------
1. Scaffold navigation and fragments/screens (Login, Dashboard, AddSnack, AddWorkout)
2. Add Room entities + DAOs and a minimal repository
3. Implement dashboard UI with metric calculations
4. Wire add snack/workout forms and update persistence
5. Add guest-mode gating and simple preferences to store guest state

Assets and Deliverables
-----------------------
- Colors and theme values added to `app/src/main/res/values/colors.xml` and `themes.xml`.
- Gradient added at `app/src/main/res/drawable/fit_gradient.xml`.
- This DESIGN.md provides UI specs, data model, and implementation guidance.

Appendix: Quick Android snippets
-------------------------------
- Gradient background for AppBar (XML):
  <com.google.android.material.appbar.MaterialToolbar
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:background="@drawable/fit_gradient" />

- FAB with circular gradient (XML):
  <com.google.android.material.floatingactionbutton.FloatingActionButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:src="@drawable/ic_add"
      app:backgroundTintMode="src_over"
      app:backgroundTint="@color/fit_green" />

- Simple Room entity (Kotlin example):
  @Entity
  data class SnackEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Int,
    val portion: String?,
    val date: String
  )

Contact
-------
If you want, I can scaffold fragments, Room entities, and a small dashboard UI next. Specify if you prefer XML views or Jetpack Compose.

