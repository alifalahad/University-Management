# University Management System

## Run

```
docker compose up --build
```

App: http://localhost:8080 — first login: `admin` / `admin123`

The page at http://localhost:8080 (`src/main/resources/static/index.html`) lets you log in and add / edit / delete from the browser.

## Files

| File | What it does |
|---|---|
| `User.java` | The table. One `users` table; `role` = ADMIN / TEACHER / STUDENT |
| `UserRepository.java` | Database queries (Spring Data JPA writes them for us) |
| `UserController.java` | REST CRUD for `/api/admins`, `/api/teachers`, `/api/students` |
| `SecurityConfig.java` | Login, logout, BCrypt password hashing, who can access what |
| `UniversityApplication.java` | Starts the app and creates the first admin |
| `Dockerfile` | Builds the jar and runs it |
| `compose.yaml` | Runs the app + a PostgreSQL database together |

## Endpoints

| Method | URL | Result |
|---|---|---|
| GET | `/api/students` | list all — 200 |
| GET | `/api/students/1` | get one — 200 or 404 |
| POST | `/api/students` | create — 201 (409 if username taken) |
| PUT | `/api/students/1` | update — 200 or 404 |
| DELETE | `/api/students/1` | delete — 204 or 404 |

Same for `/api/teachers` and `/api/admins`.

## Who can access what

| Role | admins | teachers | students |
|---|---|---|---|
| ADMIN | ✅ | ✅ | ✅ |
| TEACHER | ❌ | ✅ | ❌ |
| STUDENT | ❌ | ❌ | ✅ |

No login → 401. Wrong role → 403.

## Try it (curl)

```
# create a student (as admin)
curl -u admin:admin123 -X POST -H "Content-Type: application/json" \
  -d '{"name":"Sakib","username":"s1","password":"1234"}' \
  localhost:8080/api/students

# student lists students -> 200
curl -u s1:1234 localhost:8080/api/students

# student tries admins -> 403
curl -u s1:1234 localhost:8080/api/admins
```

In a browser: `/login` to log in, `/logout` to log out.
