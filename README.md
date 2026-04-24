# Furzefield Leisure Centre (FLC) Booking System

A self-contained Java 17 console application for managing group exercise lesson bookings at Furzefield Leisure Centre.

---

## Requirements

| Requirement | Detail |
|---|---|
| **Java version** | **Java 17 or later (required)** |
| Operating system | Windows, macOS, or Linux |
| External libraries | None — fully self-contained |

> If you do not have Java 17, download it free from https://adoptium.net/temurin/releases/?version=17
> The program will print a clear error message if an older Java version is detected.

---

## Features

- **Book a lesson** — view timetable by day (Saturday/Sunday) or by exercise type, then book into any available slot
- **Change / Cancel a booking** — change to a different lesson or cancel entirely; space is released immediately
- **Attend a lesson** — mark a booking as attended and submit a rating (1–5) with a written review
- **Monthly lesson report** — for any month, shows attended member count and average rating per lesson
- **Monthly champion report** — lists income for every exercise type in a chosen month and highlights the top earner
- **Booking status tracking** — each booking carries a status: `BOOKED`, `CHANGED`, `ATTENDED`, or `CANCELLED`

### Sample dataset
| Item | Count |
|---|---|
| Weekends / days | 8 weekends (16 days) |
| Lessons | 48 (6 per weekend) |
| Exercise types | 6 (Yoga, Zumba, Aquacise, Box Fit, Body Blitz, Pilates) |
| Members | 10 pre-registered |
| Attended lessons with reviews | 24 |

---

## Project structure

```
flc_project/
├── src/
│   └── flc/
│       ├── Main.java                  — entry point & CLI menu
│       ├── SampleDataFactory.java     — seeds members, lessons, bookings, reviews
│       ├── model/
│       │   ├── Booking.java
│       │   ├── BookingStatus.java     — BOOKED / CHANGED / ATTENDED / CANCELLED
│       │   ├── DayType.java
│       │   ├── ExerciseType.java      — 6 types with fixed prices
│       │   ├── Lesson.java
│       │   ├── Member.java
│       │   ├── Review.java
│       │   └── TimeSlot.java
│       ├── service/
│       │   └── BookingManager.java    — all business logic
│       └── util/
│           └── ReportPrinter.java     — monthly report output
├── test/
│   └── flc/
│       └── BookingManagerTest.java    — 9 JUnit 5 tests
├── flc-booking-system.jar             — executable JAR
├── run.bat                            — Windows launcher (handles £ encoding)
├── run.sh                             — macOS / Linux launcher
└── README.md
```

---

## Running the program

### Windows — double-click or run from Command Prompt
```
run.bat
```

### macOS / Linux — run from Terminal
```bash
chmod +x run.sh
./run.sh
```

### Any platform — run the JAR directly
```
java -Dfile.encoding=UTF-8 -jar flc-booking-system.jar
```
> On Windows, first run `chcp 65001` in the same Command Prompt window so the `£` symbol displays correctly.

---

## Compiling from source (Windows)

Requires Java 17 or later.

```bat
:: Create output directory
mkdir out

:: Compile all source files
for /r src %%f in (*.java) do set SRC=!SRC! "%%f"
javac -encoding UTF-8 -d out %SRC%
```

Or using PowerShell:
```powershell
$src = Get-ChildItem -Path src -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d out $src
```

To rebuild the executable JAR after compiling:
```bat
jar cfm flc-booking-system.jar src\MANIFEST.MF -C out .
```

---

## Running JUnit 5 tests (Windows)

Download the JUnit 5 standalone console JAR from:
https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.14.3/junit-platform-console-standalone-1.14.3.jar

Then compile and run the tests:

```powershell
# Compile tests
$testFiles = Get-ChildItem -Path test -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp "junit-platform-console-standalone-1.14.3.jar;out" -d test-out $testFiles

# Run tests
java -jar junit-platform-console-standalone-1.14.3.jar --class-path "out;test-out" --scan-class-path
```

Expected result: **9 tests, 0 failures**.

---

## Exercise type prices

| Exercise | Price per session |
|---|---|
| Yoga | £12.00 |
| Zumba | £11.00 |
| Aquacise | £13.50 |
| Box Fit | £14.00 |
| Body Blitz | £15.00 |
| Pilates | £12.50 |
