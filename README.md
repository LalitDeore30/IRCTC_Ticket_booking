# 🚆 IRCTC Ticket Booking System

A full-fledged **Java-based train ticket booking system** built using **Spring Boot**, **Microservices architecture**, and **Gradle** for build automation. This backend-focused project allows users to search trains, book/cancel tickets, and view their booking history — all with a modular and scalable codebase.

---

## 🚀 Features

- 🔐 **Secure User Authentication**  
  Only registered users can access booking services.

- 🔍 **Train Search**  
  Search trains based on source, destination, and date.

- 🎟️ **Ticket Booking & Cancellation**  
  Book and cancel train tickets with real-time seat updates.

- 🧾 **Booking History**  
  View all previous and current bookings.

- 💾 **Local Storage-Based Session**  
  User session is handled locally (no external cloud or frontend logic).

---

## 💻 Tech Stack

| Technology         | Purpose                                          |
|--------------------|--------------------------------------------------|
| **Java (JDK 17+)** | Core application logic                           |
| **Spring Boot**     | API development and microservices architecture   |
| **Gradle**          | Build and dependency management                  |
| **MySQL**           | Database to store users, trains, and bookings    |
| **IntelliJ IDEA**   | IDE for development                              |


---

## 🧪 Development Approach

✅ Followed modular microservice structure: `User`, `Train`, and `Booking` services  
✅ RESTful APIs developed with Spring Boot and tested via Postman  
✅ Session and temporary data stored in local memory  
✅ Emphasis on clean design, transactional safety, and data consistency

---


## 📈 Scalability & Modularity

- 🧩 Microservices architecture to support independent scaling  
- 🛠️ Separate modules for users, trains, and booking functionality  
- 🗃️ Designed for real-time use and transactional safety

---

## 🔧 Setup & Run Locally

1. Clone this repository:
   ```bash
   git clone https://github.com/YourUsername/IRCTC-Ticket-Booking-System
