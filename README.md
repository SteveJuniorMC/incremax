# Incremax

Build strength incrementally. Small steps, big results.

Incremax is an Android fitness app that helps you build lasting exercise habits through incremental progression. Start with just 10 push-ups a day, and by the end of the year, you'll be doing 100+ - that's elite level fitness achieved through sustainable, gradual improvement.

## Features

### Incremental Workout Plans
- **Preset Challenges**: 100 Push-up Challenge, Couch to 5K, Plank Master, and more
- **Custom Plans**: Create your own progression with any exercise
- **Smart Progression**: Auto-calculates daily targets based on your schedule
- **Progress Visualization**: See how far you've come and how close you are to your goal

### Full Gamification System
- **Streaks**: Build daily workout streaks like Duolingo
- **XP & Levels**: Earn experience points and level up from Beginner to Legend
- **Achievements**: Unlock badges for milestones (7-day streak, 1000 push-ups, etc.)
- **Progress Tracking**: Activity heatmap, lifetime stats, personal records

### Exercise Types
- **Bodyweight**: Push-ups, sit-ups, squats, lunges, burpees, planks
- **Cardio**: Running, walking (distance-based)
- **Flexibility**: Stretching (time-based)
- **Custom**: Create your own exercises with custom units

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Database**: Room (designed for future Firebase migration)
- **Navigation**: Navigation Compose
- **Preferences**: DataStore

## Building

The app is built using GitHub Actions. On every push to `main`, a debug APK is automatically built and uploaded as an artifact.

### Local Development

```bash
./gradlew assembleDebug
```

### GitHub Actions

Push to the `main` branch to trigger a build. Download the APK from the Actions tab.

## Project Structure

```
app/src/main/java/com/incremax/
├── data/                    # Data layer
│   ├── local/              # Room database
│   │   ├── dao/            # Data Access Objects
│   │   ├── entity/         # Database entities
│   │   └── database/       # Database configuration
│   └── repository/         # Repository implementations
├── domain/                  # Domain layer
│   ├── model/              # Domain models
│   └── repository/         # Repository interfaces
├── di/                      # Hilt dependency injection
├── ui/                      # Presentation layer
│   ├── theme/              # Material 3 theme
│   ├── navigation/         # Navigation setup
│   └── screens/            # Screen composables
│       ├── home/           # Dashboard
│       ├── plans/          # Workout plans
│       ├── workout/        # Active workout
│       ├── progress/       # Stats & history
│       ├── achievements/   # Badges & rewards
│       └── profile/        # Settings
└── util/                    # Utilities
```

## The Incremental Philosophy

The power of incremental fitness:

| Week | Push-ups/day | Weekly Total |
|------|--------------|--------------|
| 1    | 10           | 70           |
| 10   | 28           | 196          |
| 26   | 60           | 420          |
| 52   | 112          | 784          |

Starting with just 10 push-ups and adding 2 per week gets you to elite-level fitness by the end of the year. That's 27,000+ push-ups over 12 months, achieved through sustainable daily habits.

## License

MIT License
