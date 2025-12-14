# FitSnacks

Android app to track snacks and calories using Room.

## Table of Contents

- Overview
- Functionalities
- Quick Start
- Project structure
- Architecture & Key Files
- Dependencies
- Database schema & DAO
- Usage (UI walkthrough)
- Development notes
- Migrations
- Testing
- Contributing
- License

## Overview

FitSnacks is a simple snack-tracking Android application that demonstrates usage of Android Jetpack libraries such as Room for local persistence, Lifecycle/ViewModel for UI data handling, and standard Android UI components.

Language: Java (Android SDK)

Minimum SDK: 28
Compile / Target SDK: 36

## Functionalities

- Add snack entries with name, calories, portion size and date.
- View list of all snacks ordered by date (newest first).
- View snacks filtered by date.
- Delete snack entries.
- Observe total calories (aggregated) via LiveData.

## Quick Start

Requirements:
- JDK 11+
- Android SDK (platform 36 recommended)
- Android Studio (recommended) or command-line Gradle

Commands (from project root):

Build debug APK:

```bash
./gradlew assembleDebug
```

Install and run on a connected device/emulator:

```bash
./gradlew installDebug
```

Run unit tests:

```bash
./gradlew test
```

Run instrumentation tests (on device/emulator):

```bash
./gradlew connectedAndroidTest
```

## Project structure (important files)

Top-level:
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/` (version catalog)

App module (main):
- `app/src/main/java/com/example/fitsnacks/` - main package
  - `data/` - Room entities, DAO, database
  - `ui/` - Fragments, adapters, view code
  - `MainActivity` / navigation resources (if present)
- `app/src/main/res/` - layouts, drawables, strings

## Architecture & Key Files

- `app/src/main/java/com/example/fitsnacks/data/AppDatabase.java` - Room database singleton.
- `app/src/main/java/com/example/fitsnacks/data/SnackEntry.java` - Room entity representing a snack record.
- `app/src/main/java/com/example/fitsnacks/data/SnackDao.java` - DAO exposing queries: insert, delete, getAll, getSnacksByDate, getTotalCalories.
- `app/src/main/java/com/example/fitsnacks/ui/AddSnackFragment.java` - UI for adding a new snack entry.
- `app/src/main/java/com/example/fitsnacks/ui/SnackListAdapter.java` - RecyclerView adapter for snack list.

## Dependencies

These are the dependencies declared in the project Gradle files (module `app` / `build.gradle.kts` and `gradle/libs.versions.toml`). Versions reflect the repository at the time of README creation.

Core Android / UI
- androidx.appcompat:appcompat:1.7.1
- com.google.android.material:material:1.13.0
- androidx.activity:activity:1.12.0
- androidx.constraintlayout:constraintlayout:2.2.1
- androidx.recyclerview:recyclerview:1.4.0

Lifecycle / ViewModel
- androidx.lifecycle:lifecycle-viewmodel:2.10.0
- androidx.lifecycle:lifecycle-livedata:2.10.0

Room (database)
- androidx.room:room-runtime:2.8.4
- androidx.room:room-common-jvm:2.8.4
- androidx.room:room-compiler:2.8.4 (annotation processor)
- androidx.room:room-rxjava2:2.8.4

Kotlin (present for annotation processor metadata reasons)
- org.jetbrains.kotlin:kotlin-stdlib:1.9.20
- org.jetbrains.kotlin:kotlin-reflect:1.9.20
- org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0

Testing
- junit:junit:4.13.2 (unit tests)
- androidx.test.ext:junit:1.3.0 (instrumentation)
- androidx.test.espresso:espresso-core:3.7.0 (UI tests)

Notes:
- The project uses a version catalog at `gradle/libs.versions.toml` for several aliased dependencies.

## Database schema & DAO

Entity: `SnackEntry` (table `snacks`)
- id: long (PrimaryKey, autoGenerate = true)
- name: String
- calories: int
- portion: String
- date: String (ISO-8601, stored as text)

DAO: `SnackDao` methods
- `void insert(SnackEntry snack)` — insert a snack (Room will perform on calling thread unless wrapped in background executor).
- `LiveData<List<SnackEntry>> getAllSnacks()` — returns LiveData with all snacks ordered by date desc, id desc.
- `LiveData<Integer> getTotalCalories()` — returns aggregated calories sum as LiveData.
- `void delete(long id)` — delete snack by id.
- `LiveData<List<SnackEntry>> getSnacksByDate(String date)` — filter by date (string match).

Usage notes:
- DAO operations that write to the DB (insert/delete) must be called off the main thread. Use Executors, Kotlin coroutines (with a suitable Room suspend/Coroutine setup), or ViewModelScope with IO dispatcher.

Example (Java) background insert using an Executor:

```java
ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
dbExecutor.execute(() -> {
    AppDatabase db = AppDatabase.getDatabase(context);
    db.snackDao().insert(new SnackEntry("Apple", 95, "1 medium", "2025-12-14"));
});
```

(If you prefer coroutines, adapt the code and Room declarations accordingly.)

## Usage (UI walkthrough)

Typical flow for an end user:
1. Open the app.
2. Tap the "Add" button (or open Add Snack screen).
3. Enter snack `name`, `calories`, `portion` and optionally a `date` (app expects ISO-8601 string format, but UI may provide a date picker).
4. Tap Save — the item is persisted to Room and the list updates via LiveData observation.
5. View total calories (UI reads `getTotalCalories()` LiveData).
6. Swipe or tap delete on a list item to remove it.

Developer tips:
- The list is backed by LiveData, so make sure fragments/activities observe the LiveData with the appropriate lifecycle owner to get automatic UI updates.

## Development notes

- Java is the primary language in the codebase, but Kotlin dependencies are present to support annotation processors. Do not remove Kotlin stdlib or metadata deps unless you update annotation processing.

- Some generated sources are present under `app/build/generated/` — these are produced by Room and annotation processors during build.

- There are a few IDE/compile warnings related to unused methods in `AppDatabase` and a deprecation warning for `fallbackToDestructiveMigration()` (see Migrations section).

## Migrations

Current setup in `AppDatabase` uses `fallbackToDestructiveMigration()` which will drop & recreate the database on schema version changes. This is fine for early development but will delete user data on upgrades.

Recommended approach for production:
1. Keep `version` in the `@Database` annotation and increment it when you change entities.
2. Provide `Migration` objects that transform old schemas to new ones.

Example Migration skeleton (Java):

```java
// Example migration from version 1 -> 2
static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Example: add a new column 'notes' to the snacks table
        database.execSQL("ALTER TABLE snacks ADD COLUMN notes TEXT DEFAULT ''");
    }
};

// In builder:
Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database")
    .addMigrations(MIGRATION_1_2)
    .build();
```

If migrations are not provided and you keep `fallbackToDestructiveMigration()`, add a clear comment in `AppDatabase` to explain the tradeoff.

## Testing

- Unit tests: place under `app/src/test/java/`.
- Instrumentation tests: place under `app/src/androidTest/java/` and run on emulator/device with `./gradlew connectedAndroidTest`.

Tips for testing Room:
- Use an in-memory database for unit tests (`Room.inMemoryDatabaseBuilder(...)`) so tests don't touch device storage and run fast.

Example (JUnit) in-memory DB setup:

```java
@Before
public void createDb() {
    Context context = ApplicationProvider.getApplicationContext();
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
            .allowMainThreadQueries() // only for tests
            .build();
    dao = db.snackDao();
}

@After
public void closeDb() {
    db.close();
}
```

## Contributing

- Open an issue to discuss larger changes.
- Fork the repo, create a feature branch, run tests, and open a PR.
- Follow existing code style and document public changes.

A suggested workflow:
- Create a small change per PR with a clear description and screenshots (if UI change).
- Add unit tests for new logic.

## License

This repository does not include a LICENSE file yet. If you want a permissive license, consider adding an MIT license. Example text can be generated and added as `LICENSE`.

---

If you'd like, I can also:
- Add a sample `LICENSE` (MIT) file.
- Create a small in-memory Room unit test example in the repo.
- Replace `fallbackToDestructiveMigration()` in `AppDatabase` with a starter migration and update the code.

Tell me which of these you'd like next and I'll implement it.
