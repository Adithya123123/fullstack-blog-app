# Complete Render Deployment Guide – Full-Stack Blog App

This guide walks you through deploying your **Java 17 + JDBC + HttpServer Backend**, **MySQL Database**, and **React Frontend** to **Render** completely for free.

---

## Architecture on Render

```text
[ Browser / User ]
        |
        +---> [ Render Static Site (React Frontend) ]
        |     https://fullstack-blog-frontend.onrender.com
        |
        +---> [ Render Web Service (Java 17 Docker Backend) ]
              https://fullstack-blog-backend.onrender.com
                    |
                    +---> [ Free Cloud MySQL Database ]
                          (TiDB Cloud Serverless / Aiven / Clever Cloud)
```

---

## Step 1: Set Up a Free Cloud MySQL Database (2 Minutes)

Render does not offer a free managed MySQL service directly (only PostgreSQL). However, you can use **TiDB Cloud Serverless** or **Clever Cloud**, which provide a 100% free MySQL-compatible database.

### Recommended: TiDB Cloud Serverless (5GB Free Forever)
1. Go to [https://tidbcloud.com](https://tidbcloud.com) and sign up (or sign in with GitHub/Google).
2. Click **Create Cluster** and select **Serverless (Free)**.
3. In your cluster dashboard, click **Connect**:
   - Choose **General** connection.
   - Note down:
     - **Host** (e.g., `gateway01.us-east-1.prod.aws.tidbcloud.com`)
     - **Port** (usually `4000`)
     - **User** (e.g., `xxxx.root`)
     - **Password** (generate and save it)
4. Click on **SQL Editor** in the TiDB dashboard, paste the contents of `schema.sql`, and click **Run** to create the `users` and `posts` tables.
5. Your JDBC URL will look like:
   ```text
   jdbc:mysql://<HOST>:<PORT>/blog_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   ```

*(Alternative: You can also use [Aiven](https://aiven.io/mysql) or [Clever Cloud](https://www.clever-cloud.com) free tier).*

---

## Step 2: Push Your Code to GitHub

1. Open PowerShell and navigate to your project folder:
   ```powershell
   cd C:\Users\Admin\.gemini\antigravity\scratch\fullstack-blog-app
   ```

2. Initialize Git (if not already done) and commit the code:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Full-stack blog application with Java 17, JDBC, and React"
   ```

3. Create a new repository on your [GitHub](https://github.com/new) account named `fullstack-blog-app`.

4. Link and push to GitHub:
   ```bash
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/fullstack-blog-app.git
   git push -u origin main
   ```

---

## Step 3: Deploy Backend on Render (Web Service)

1. Log into [Render Dashboard](https://dashboard.render.com).
2. Click **New +** and select **Web Service**.
3. Choose **Build and deploy from a Git repository** and connect your `fullstack-blog-app` repository.
4. Configure the Web Service:
   - **Name:** `fullstack-blog-backend`
   - **Region:** Any (e.g., Oregon or Frankfurt)
   - **Language / Runtime:** Select **Docker**
   - **Dockerfile Path:** `./backend/Dockerfile`
   - **Docker Build Context:** `./backend`
   - **Instance Type:** **Free**
5. Expand **Environment Variables** and add the following:
   | Key | Value | Notes |
   |---|---|---|
   | `PORT` | `10000` | Render's default web service port |
   | `ENVIRONMENT` | `production` | Enables secure production mode |
   | `COOKIE_SECURE` | `true` | Enables `SameSite=None; Secure` for HTTPS cookies |
   | `DB_URL` | `jdbc:mysql://<HOST>:<PORT>/blog_db?useSSL=true&allowPublicKeyRetrieval=true` | Your cloud MySQL JDBC URL |
   | `DB_USER` | `your_cloud_db_user` | Database username |
   | `DB_PASSWORD` | `your_cloud_db_password` | Database password |
   | `FRONTEND_URL` | `https://fullstack-blog-frontend-xxxx.onrender.com` | Update this after Step 4! |

6. Click **Deploy Web Service**.
7. Wait ~2–3 minutes for the build to finish. Once live, copy your backend URL:
   `https://fullstack-blog-backend-xxxx.onrender.com`

---

## Step 4: Deploy Frontend on Render (Static Site)

1. In the Render Dashboard, click **New +** and select **Static Site**.
2. Connect the same `fullstack-blog-app` GitHub repository.
3. Configure the Static Site:
   - **Name:** `fullstack-blog-frontend`
   - **Root Directory:** `frontend`
   - **Build Command:** `npm install && npm run build`
   - **Publish Directory:** `dist`
4. Expand **Environment Variables** and add:
   | Key | Value |
   |---|---|
   | `VITE_API_URL` | `https://fullstack-blog-backend-xxxx.onrender.com` *(paste your backend URL from Step 3)* |

5. Scroll down to **Redirects/Rewrites** and click **Add Rule**:
   - **Type:** `Rewrite`
   - **Source:** `/*`
   - **Destination:** `/index.html`
   *(This ensures React Router client-side routes like `/posts/1` and `/login` load properly upon page refresh!)*

6. Click **Create Static Site**.
7. Once deployed, copy your frontend URL:
   `https://fullstack-blog-frontend-xxxx.onrender.com`

---

## Step 5: Final CORS Hookup

1. Return to your **Backend Web Service** in Render.
2. Go to **Environment** tab.
3. Update the `FRONTEND_URL` variable to your actual frontend URL:
   ```text
   FRONTEND_URL = https://fullstack-blog-frontend-xxxx.onrender.com
   ```
4. Render will automatically re-deploy the backend with the new setting.

---

## Testing Your Live Deployment

1. Visit your frontend URL: `https://fullstack-blog-frontend-xxxx.onrender.com`.
2. Click **Register** and create a new account.
3. Log in with your new credentials.
4. Publish a test blog post.
5. Verify that your article appears on the home feed and on the **My Posts** page.
6. Test **Edit** and **Delete** actions.

---

## Summary of Deploy Artifacts Included in Repo

- **`backend/Dockerfile`**: Multi-stage lightweight Docker image using OpenJDK 17 and Maven.
- **`render.yaml`**: Infrastructure-as-Code Blueprint defining both services for 1-click deployment.
- **`frontend/vite.config.js`**: Production-ready client bundling.
- **`schema.sql`**: One-click database table creation script.
