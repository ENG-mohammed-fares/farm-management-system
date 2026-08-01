# 🌾 Hased — Smart Farm Management System

<p align="center">
  <strong>An institutional desktop application for managing farm operations — built with JavaFX and PostgreSQL.</strong>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-orange">
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-21.0.5-blue">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-18-336791">
  <img alt="Maven" src="https://img.shields.io/badge/Build-Maven-C71A36">
  <img alt="Status" src="https://img.shields.io/badge/Status-Academic%20Project-green">
</p>

---

## 📖 Overview

**Hased** is a single-farm institutional management system designed for a Computer Engineering university project. Unlike public marketplace platforms, Hased is built for **one agricultural organization** — a farm owner (Admin) manages fields, crops, workers, harvests, transactions, and fertilizer/medicine inventory, while workers log their own daily activities and track their earnings.

The system runs entirely **offline as a JavaFX desktop application**, connected to a local PostgreSQL database — no internet dependency, in line with the project's academic constraints.

---

## 👥 Authors

- **Mohammad Fares**
- **Yamen Aburob**

Computer Engineering Department — University Project (2025–2026)

---

## ✨ Key Features

### 🔐 Authentication
- Secure Login / Sign Up with role-based routing (Admin ↔ Worker)
- Email **or** Phone login toggle, with live country-flag detection (198 countries supported)
- Forgot Password flow with a 5-digit verification code (email SMTP / simulated SMS), 30-second countdown, and resend option
- Full input validation: password strength rules, email format checks, duplicate email/phone/username prevention

### 🧑‍💼 Admin Dashboard
- **Dashboard** — real-time KPI cards (fields, workers, harvest totals, revenue), balance ring chart, recent activity feed
- **Fields & Crops** — add/edit/delete fields and crops, area entered in any unit (Square Meter, Dunum, Hectare, Acre) and stored in m²
- **Harvests** — approval workflow (Pending → Approved / Rejected) with live filtering
- **Workers** — assign registered users as workers, edit wage/role/status, deactivate or delete (with referential-integrity protection)
- **Transactions** — record Sales, Purchases, and Payments manually; automatic financial summaries
- **Fertilizers & Medicines** — full inventory management with composition, active ingredient, target disease, and low-stock alerts
- **Reports** — harvest quality ring, budget usage ring, top-5 crops by yield, revenue/expense breakdown
- **History** — unified timeline combining harvests, transactions, and farm activity logs

### 👷 Worker Dashboard
- **Dashboard** — personal profile, monthly stats, payment progress ring
- **Log Work** — dynamic form based on job type:
  - Harvester → submit harvest (good/damaged quantity)
  - Irrigator → log irrigation (m³)
  - Plower → log plowing (dunum)
- **My Work** — history of submissions with approval status
- **My Earnings** — total earned, received, and remaining balance
- **Calendar** — visual monthly calendar showing real farm activity (color-coded by type), clickable days with full detail, contributions highlighted
- **Settings** — editable profile (name, email, phone) with duplicate validation

### 🌗 UI/UX
- Full Dark Mode support across all pages, persisted across navigation
- Smooth JavaFX animations (fade, slide, typewriter effects)
- Responsive card-based layout using `GridPane` with percentage-based columns
- Window state (maximized/size) preserved across all scene transitions

---

## 🗄️ Database Schema

PostgreSQL database with **9 core tables**:

| Table | Description |
|---|---|
| `Users` | System accounts (one Admin + Workers) |
| `Farms` | Single farm record |
| `Farm_Workers` | Worker assignments with job type and wage |
| `Fields` | Farm fields with soil status and area (m²) |
| `Crops` | Crops planted per field |
| `Fertilizers_Medicines` | Product inventory with composition details |
| `Farm_Logs` | Activity logs (irrigation, plowing, planting, fertilizing) |
| `Harvests` | Harvest records with approval status |
| `Transactions` | Financial records (Sale / Purchase / Payment) |

Full DDL + sample data available in [`DDL_and_INSERT.sql`](./DDL_and_INSERT.sql).

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| UI Framework | JavaFX 21.0.5 (FXML + CSS) |
| Database | PostgreSQL 18 |
| Build Tool | Maven |
| Email (SMTP) | Angus Mail (Jakarta Mail) |
| Password Handling | Plain-text (academic simplification) |

---

## 📂 Project Structure

```
farm_managment_system/
├── src/main/java/com/smartfarm/
│   ├── controller/       # FXML controllers (Login, SignUp, Dashboard, WorkerDashboard, Intro)
│   ├── dao/               # Data Access Objects (one per table)
│   ├── model/              # POJOs representing each entity
│   ├── service/           # Business logic layer (Auth, Farm, Worker, Transaction)
│   ├── util/               # Utilities (DatabaseConnection, SceneSwitcher, SessionManager, etc.)
│   └── Main.java
├── src/main/resources/
│   ├── css/                # Stylesheets (login, signup, dashboard)
│   ├── fxml/                # FXML layout files
│   └── images/              # Icons, logo, and 198 country flags
├── pom.xml
└── DDL_and_INSERT.sql
```

---

## ⚙️ Setup & Installation

### Prerequisites
- **Java 21 JDK**
- **PostgreSQL 18** (or compatible)
- **Maven** (or use IntelliJ's bundled Maven)

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/hased.git
cd hased/farm_managment_system
```

### 2. Set up the database
```bash
psql -U postgres
```
```sql
CREATE USER hased WITH PASSWORD '123456' SUPERUSER;
CREATE DATABASE hasd_db OWNER hased;
\c hasd_db
```
Then run the schema:
```bash
psql -U hased -d hasd_db -f DDL_and_INSERT.sql
```

### 3. Configure the connection
Verify `src/main/java/com/smartfarm/util/DatabaseConnection.java` matches your setup:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/hasd_db";
private static final String USER = "hased";
private static final String PASSWORD = "123456";
```

### 4. Run the application
From IntelliJ: run `Main.java`, or via Maven:
```bash
mvn clean javafx:run
```

---

## 🔑 Default Login Credentials

| Role | Identifier | Password |
|---|---|---|
| Admin | `admin` | `12345` |
| Worker (example) | `ahmad@hased.ps` | `123456` |

---

## 📐 Diagrams & Documentation

- **EER / UML Diagram** — [`ER_UML.drawio`](./ER_UML.drawio) (open with [draw.io](https://app.diagrams.net))
- **Business Rules Document** — provided separately in the report submission
- **Referential Integrity Schema** — included in the diagrams folder

---

## 📌 Project Notes

- This system is intentionally **offline-only** (no internet-dependent features) per academic project requirements.
- Passwords are stored as plain text for simplicity in this academic context — not recommended for production use.
- The email verification feature requires a valid Gmail App Password configured in `EmailService.java` to function.
- Tasks/Calendar are **derived** from `Farm_Logs` and `Harvests` records — there is no separate `Tasks` table by design.

---

## 📄 License

This project was developed for academic purposes as part of a university Computer Engineering course.
