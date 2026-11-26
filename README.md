# Full-Stack Blog Application

A simple, beginner-friendly Full-Stack Blog Website built with **Java**, **JDBC**, **MySQL**, and **React**.

This project was built from scratch without using complex enterprise frameworks like Spring Boot or Hibernate. It focuses on the core fundamentals of web development: creating a user interface, writing clean Java code to handle requests, connecting to a MySQL database with JDBC, and performing CRUD (Create, Read, Update, Delete) operations.

---

## 1. Project Overview (In Simple Terms)

Think of this project as three main parts working together:

1. **Frontend (React):** The screen that users see in their web browser. It displays the blog posts, navigation bar, login forms, and article editors.
2. **Backend (Java):** The engine that receives requests from the frontend, checks if inputs are valid, and communicates with the database.
3. **Database (MySQL):** The storage system where registered user accounts and blog posts are saved so they don't disappear.

---

## 2. Technologies Used

- **Backend:** Java 17, Java, JDBC, MySQL Connector/J
- **Frontend:** React JS, JavaScript, HTML, CSS, React Router, Vite
- **Database:** MySQL 8.0
- **Build Tool:** Apache Maven

*(No Spring Boot, No Hibernate, No JPA, and No Node.js backend were used, keeping the project easy to learn, modify, and explain).*

---

## 3. Features

- **User Registration:** Create a new account with a username, email, and password. Prevents duplicate usernames or emails.
- **User Login & Logout:** Registered users can log in using their email and password. A secure session keeps them logged in until they click Logout.
- **View All Posts:** Anyone (even visitors who aren't logged in) can browse and read published blog posts.
- **Search Posts:** Filter and find articles by typing keywords into the search bar.
- **Create Posts:** Logged-in users can write and publish new articles with a title and content.
- **My Posts Page:** A dedicated page where users can see all the articles they personally wrote.
- **Edit & Delete:** Users can edit or delete their own posts. If someone tries to edit someone else's post, the Java backend blocks them.

---

## 4. Project Structure

```text
fullstack-blog-app/
├── schema.sql                     # Database creation script
├── README.md                      # Project documentation
├── LEARNING_GUIDE.md              # Easy explanation guide for interviews
│
├── backend/                       # Java Backend
│   ├── pom.xml                    # Maven dependencies
│   └── src/main/java/com/blog/
│       ├── Main.java              # Starts the Java server on port 8080
│       ├── config/
│       │   └── DBConnection.java  # Connects Java to MySQL
│       ├── model/
│       │   ├── User.java          # User data class (id, username, email)
│       │   └── Post.java          # Post data class (id, title, content, author)
│       ├── dao/
│       │   ├── UserDAO.java       # SQL queries for users table
│       │   └── PostDAO.java       # SQL queries for posts table (with JOIN)
│       ├── service/
│       │   ├── UserService.java   # Checks registration & login inputs
│       │   └── PostService.java   # Checks post ownership before editing
│       ├── handler/
│       │   ├── AuthHandler.java   # Handles /api/register and /api/login
│       │   └── PostHandler.java   # Handles /api/posts and /api/my-posts
│       └── util/
│           ├── PasswordUtil.java  # Hashes passwords so they aren't saved in plaintext
│           ├── SessionManager.java# Keeps users logged in
│           └── ResponseUtil.java  # Sends clean JSON back to the browser
│
└── frontend/                      # React Frontend
    ├── package.json               # Frontend dependencies
    ├── vite.config.js             # Connects frontend (port 5173) to backend (port 8080)
    └── src/
        ├── components/
        │   ├── Navbar.jsx         # Top menu bar
        │   ├── Footer.jsx         # Bottom footer
        │   └── PostCard.jsx       # Card displaying article title and snippet
        ├── pages/
        │   ├── Home.jsx           # All articles list + search box
        │   ├── Login.jsx          # Login page
        │   ├── Register.jsx       # Sign-up page
        │   ├── CreatePost.jsx     # New article editor
        │   ├── EditPost.jsx       # Edit article editor
        │   ├── PostDetails.jsx    # Full article reading page
        │   └── MyPosts.jsx        # User's own articles page
        ├── services/
        │   └── api.js             # Makes fetch calls to the Java backend
        └── styles/
            └── style.css          # Plain CSS styling
```

---

## 5. Database Setup (MySQL)

Open your MySQL command line or MySQL Workbench and run `schema.sql`:

```sql
CREATE DATABASE IF NOT EXISTS blog_db;
USE blog_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Posts Table
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

Set your MySQL password in `backend/src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/blog_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

---

## 6. How to Run the Project Locally

### Step 1: Start the Backend (Java)
Open a terminal in the `backend` folder:
```bash
cd backend
mvn clean compile exec:java
```
*The backend server will start at: `http://localhost:8080`*

### Step 2: Start the Frontend (React)
Open a second terminal in the `frontend` folder:
```bash
cd frontend
npm install
npm run dev
```
*The frontend will start at: `http://localhost:5173`*

Open your browser and visit: **`http://localhost:5173`**

---

## 7. Authors & Project Contributors

Developed collaboratively as a full-stack B.Tech Computer Science project by:

- **Adithya** &ndash; [adithyajn833@gmail.com](mailto:adithyajn833@gmail.com)
- **Seela Nandini** &ndash; [GitHub: @SeelaNandini](https://github.com/SeelaNandini) | [seelanandini2005@gmail.com](mailto:seelanandini2005@gmail.com)
