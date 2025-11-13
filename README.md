# MadeNews  
AI-Powered Satirical News Generator

MadeNews is an Android application that transforms user-written headlines into humorous, fictional, satirical news stories using AI. The project focuses on clean architecture, a playful UI theme, and real-world mobile + backend + AI integration.

---

## Features

- Generate satirical stories from user-submitted headlines  
- Weekly automatic refresh: 5 categories × 5 stories each  
- Firebase authentication and data storage  
- Integration with GroqCloud (Llama 3 series) for story generation  
- MVVM + Clean Architecture  
- Lightweight mobile-first design  

---

## Project Vision

- Deliver fast, witty, AI-generated satire  
- Provide affordable, accessible humor for emerging markets  
- Build a strong portfolio piece showcasing:
  - Android development  
  - AI integration  
  - Backend automation  
  - Product thinking  

---

## Tech Stack

### Frontend (Android)
- Kotlin  
- Android Studio  
- MVVM  
- Clean Architecture  
- Jetpack components  

### Backend / Cloud
- Firebase Authentication  
- Firestore / Realtime Database  
- Weekly backend cron for auto-refresh  
- REST endpoints (optional future expansion)

### AI Integration
- GroqCloud API  
- Llama 3 series models  
- Story prompt engineering for humor consistency  

---

## Project Structure

```
MadeNews_App/
├── app/
│   ├── src/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Getting Started

### 1. Clone the Repository
```
git clone https://github.com/amankumar110/MadeNews_App.git
```

### 2. Open in Android Studio
- Import the project folder  
- Wait for Gradle sync to finish  

### 3. Add Firebase Configuration
- Place `google-services.json` inside `/app`  
- Enable Email/Password auth  
- Configure Firestore rules  

### 4. Configure AI API
- Add GroqCloud API key  
- Update the endpoint URL  

### 5. Run the App
- Use emulator or physical device  
- Build → Run → Generate stories  

---

## Current Progress

### Completed
- Kotlin app structure  
- MVVM + Clean Architecture  
- Basic UI screens  
- Firebase setup  
- AI story generation flow  

### Upcoming
- Public weekly story feed  
- Leaderboard + Aura reactions  
- Private/public story toggle  
- Auto-refresh for 25 weekly articles  
- Play Store release  

---

## UI/UX Theme

- Yellow paper-style visual theme  
- Clean, readable layouts  
- Friendly and fun look  

---

## Why This Project Matters

- Demonstrates full-stack mobile + AI integration  
- Strong addition to portfolio and résumés  
- Great discussion topic for interviews  
- Shows real-world product thinking  
- Designed as a true MVP experiment  

---

## Contributing

Contributions are welcome.  
Feel free to open issues or submit pull requests.

---

## License

Open-source for learning and portfolio usage.
