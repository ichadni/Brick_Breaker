# 🧱 Brick Breaker

A dynamic **arcade-style Brick Breaker game developed with Java Swing**. The game features progressive levels, multi-hit bricks, power-ups, multi-ball gameplay, paddle-angle physics, particle effects, scoring, lives, and multiple game states.

## 🎮 Gameplay

The player controls a paddle to bounce the ball and destroy all breakable bricks. Each level introduces a more challenging brick layout, stronger bricks, and additional gameplay elements.

### Key Features

* **5 Progressive Levels** with increasing difficulty
* **Multi-Health Bricks** requiring 1–3 hits
* **Steel Bricks** that cannot be destroyed
* **Dynamic Paddle Physics** based on ball impact position
* **Multiple Balls** through the Multi-Ball power-up
* **6 Power-Ups**

  * Widen Paddle
  * Shrink Paddle
  * Multi-Ball
  * Slow Ball
  * Fast Ball
  * Extra Life
* **Particle Effects** for collisions and destruction
* **Score & Life System**
* **Pause / Resume**
* **Game Over & Victory States**
* **Randomized Brick Layouts** at higher levels
* **Arcade-style Graphics and Animations**

## 🕹️ Controls

| Key       | Action                           |
| --------- | -------------------------------- |
| `←` / `A` | Move Paddle Left                 |
| `→` / `D` | Move Paddle Right                |
| `SPACE`   | Start / Launch Ball / Next Level |
| `P`       | Pause / Resume                   |
| `R`       | Restart Game                     |

## ⚡ Power-Ups

| Power-Up | Effect                         |
| -------- | ------------------------------ |
| `W`      | Temporarily widens the paddle  |
| `S`      | Temporarily shrinks the paddle |
| `M`      | Creates additional balls       |
| `↓`      | Slows down the balls           |
| `↑`      | Increases ball speed           |
| `+`      | Adds an extra life             |

## 🛠️ Technologies

* **Java**
* **Java Swing**
* **AWT**
* Object-Oriented Programming
* 2D Graphics
* Event-Driven Programming
* Collision Detection
* Game Loop & Animation
* Java Collections

## 🏗️ Project Structure

The project is implemented as a single Java application with separate classes for the main game components.

```text
BrickBreaker/
│
├── BrickBreaker.java
└── README.md
```

### Core Classes

```text
BrickBreaker
    │
    └── GamePanel
         ├── Paddle
         ├── Ball
         ├── Brick
         ├── PowerUp
         └── Particle
```

## 🚀 Getting Started

### Prerequisites

Make sure Java JDK is installed:

```bash
java -version
javac -version
```

### Clone the Repository

```bash
git clone https://github.com/ichadni/BrickBreaker.git
cd BrickBreaker
```

### Compile

```bash
javac BrickBreaker.java
```

### Run

```bash
java BrickBreaker
```

## 🧠 Game Mechanics

### Progressive Difficulty

The game contains five levels. As the player progresses, the number and difficulty of bricks increase. Higher levels can also introduce randomized gaps and steel bricks.

### Brick Health

Bricks can have different health levels:

* **1 HP** — requires one hit
* **2 HP** — requires two hits
* **3 HP** — requires three hits

### Paddle Physics

The ball's bounce angle changes depending on where it hits the paddle. Hitting different parts of the paddle produces different trajectories, giving the player greater control over the ball.

### Collision Detection

The game handles collisions between:

* Ball and walls
* Ball and paddle
* Ball and bricks
* Paddle and power-ups

## 🎯 Game States

The game manages different states throughout the gameplay:

```text
MENU
  ↓
LAUNCH
  ↓
RUNNING
  ↓
LEVEL_COMPLETE
  ↓
NEXT LEVEL
  ↓
WIN
```

Additional states include:

```text
PAUSED
GAME_OVER
```

## 📸 Screenshots

### Main Menu

![Main Menu](docs/menu.png)

### Gameplay

![Gameplay](docs/gameplay.png)

### Power-Ups

![Power-Ups](docs/powerups.png)

> Place your screenshots inside the `docs` folder using the filenames above.

## 📚 Concepts Demonstrated

This project demonstrates practical Java programming concepts including:

* Object-Oriented Programming
* Classes and Objects
* Inheritance
* Interfaces
* Enums
* Event Handling
* Collections
* Randomization
* 2D Graphics
* Collision Detection
* Game State Management
* Real-Time Game Loops

## 🔮 Future Improvements

* Sound effects and background music
* High-score persistence
* Additional levels
* More power-ups
* Custom game themes
* Difficulty selection
* Mouse and gamepad support
* Level editor
* High-score leaderboard

## 👩‍💻 Author

**Israt Jahan Chadni**

Software Engineering Student — Shahjalal University of Science and Technology (SUST)

[GitHub](https://github.com/ichadni) · [LinkedIn](https://www.linkedin.com/in/israt-chadni-016870287/)

---

⭐ **If you like this project, consider giving the repository a star.**
