# Running This Project on Another Machine

Follow these steps to run the ESM (English School Management) project on a new computer.

---

## Option 1: With Docker (recommended – persistent data)

### Prerequisites
- **Docker Desktop** installed and running
- **Node.js 18+** and **npm** (for the Angular frontend)

### Steps

1. **Copy the project** to the new machine (USB, zip, or clone from Git):
   ```
   Faddaoui/
   ├── course-service/
   ├── Enrollment-service/
   ├── esm-front-main/
   ├── docker-compose.persist.yml
   ├── run-docker-persist.ps1
   └── ...
   ```

2. **Start the backend (PostgreSQL + APIs):**
   - **Windows (PowerShell):**
     ```powershell
     cd path\to\Faddaoui
     .\run-docker-persist.ps1
     ```
   - **Linux/macOS:**
     ```bash
     cd path/to/Faddaoui
     docker-compose -f docker-compose.persist.yml up -d --build
     ```
   Wait until containers are up (about 1–2 minutes).

3. **Install and run the frontend:**
   ```bash
   cd esm-front-main
   npm install
   ng serve --port 4202
   ```

4. **Open the app:**  
   http://localhost:4202

**Ports:** course-service **8083**, enrollment-service **8084**, frontend **4202**.  
Data is stored in Docker volumes and persists after restart.

---

## Option 2: Without Docker (Java + Node only)

### Prerequisites
- **Java 17** (JDK)
- **Node.js 18+** and **npm**
- **Maven** (or use the included `mvnw` in each service)

### Steps

1. **Copy the project** to the new machine.

2. **Start the backend (Windows PowerShell):**
   ```powershell
   cd path\to\Faddaoui
   .\run-services.ps1
   ```
   This starts course-service (8083), enrollment-service (8084), and the Angular app (4202).  
   On **Linux/macOS** you need to start each service manually in separate terminals:
   ```bash
   # Terminal 1 – course-service
   cd course-service && ./mvnw spring-boot:run

   # Terminal 2 – enrollment-service (after course-service is up)
   cd Enrollment-service && ./mvnw spring-boot:run

   # Terminal 3 – frontend
   cd esm-front-main && npm install && ng serve --port 4202
   ```

3. **Open the app:**  
   http://localhost:4202

**Note:** Without Docker, backends use H2 (in-memory/file). Data may be lost on restart unless you keep the H2 data files.

---

## Summary

| Item        | With Docker              | Without Docker   |
|------------|---------------------------|------------------|
| Backend    | `run-docker-persist.ps1` | `run-services.ps1` (Win) or 3 terminals (Linux/mac) |
| Frontend   | `cd esm-front-main && npm install && ng serve --port 4202` | Same |
| Data       | Persistent (PostgreSQL in Docker) | H2 (can be lost on restart) |
| Ports      | 4202, 8083, 8084         | Same             |

---

## Troubleshooting

- **Port already in use:** Stop whatever is using 8083, 8084, or 4202, or change ports in config.
- **Certificate / design not updating:** Rebuild: `.\run-docker-persist.ps1 -Rebuild` (Docker) or restart enrollment-service.
- **Frontend can’t reach APIs:** Ensure the app runs with the proxy (e.g. `ng serve --port 4202`); `proxy.conf.json` forwards `/api` and `/enrollment-api` to the backends.
