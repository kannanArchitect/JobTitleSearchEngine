# Job Search Engine

Job search engine built with **Java 21**, Spring Boot, Apache Solr, and **Redis**. Features modern Java capabilities including virtual threads, records, pattern matching, and distributed caching for optimal performance.

## 🚀 Key Features

- **Java 21 Optimizations**: Virtual threads, records, pattern matching, switch expressions
- **High Performance**: Redis distributed caching, parallel processing, connection pooling
- **Bilingual Support**: Full English and French search capabilities
- **RESTful API**: Clean endpoints with validation and pagination
- **Auto Data Loading**: Automatic CSV import from NOC database on startup
- **Docker Ready**: Complete containerization with Docker Compose
- **Distributed Caching**: Redis for scalable, shared caching across instances

## 📋 Technology Stack

- **Java 21** - Modern features (records, virtual threads, pattern matching)
- **Spring Boot 3.2** - Framework with native Java 21 support
- **Apache Solr 9.4** - Full-text search engine
- **Redis 7** - Distributed caching layer
- **Maven 3.8+** - Build tool
- **JUnit 5 + Mockito** - Testing framework
- **Docker & Docker Compose** - Containerization

## 🏗️ Architecture

```
Controller → Service (Cached) → Repository → Solr
                ↓
           Redis Cache (Distributed)
```

### Package Structure
```
com.bet99.exercise.jobsearch
├── config/          SolrConfig (virtual threads, Redis caching)
├── controller/      JobTitleController (REST API)
├── dto/             SearchRequest, SearchResponse (records)
├── loader/          DataLoader (parallel CSV processing)
├── model/           JobTitle (record)
├── repository/      JobTitleRepository (optimized queries)
└── service/         JobTitleService (cached operations)
```

## 📦 Prerequisites

- Java 21+ ([Download](https://adoptium.net/))
- Maven 3.8+ ([Download](https://maven.apache.org/))
- Docker & Docker Compose ([Download](https://www.docker.com/))
- NOC CSV files (see Setup below)

```bash
# Verify installations
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.8+
docker --version
```

## 🚀 Quick Start

### 1. Download NOC Data Files

Download from [Statistics Canada NOC 2021](https://www.statcan.gc.ca/en/subjects/standard/noc/2021/indexV1):
- `noc_2021_version_1.0__classification_structure.csv` (822 rows)
- `noc_2021_version_1.0__elements.csv` (44,037 rows)

Place in: `src/main/resources/data/`
## Docker Setup (Recommended)

### Prerequisites
- Docker Desktop installed
- Docker Compose v2.0+

### Quick Start

1. **Clone the repository**
```bash
   git clone https://github.com/kannanArchitect/JobTitleSearchEngine.git
   cd JobTitleSearchEngine
```

2. **Start services**
```bash
   docker-compose up -d --build
```

3. **Setup Solr schema** (one-time setup)
```bash
   # Wait for Solr to start (30 seconds)
   sleep 30
   
   # Run setup script
   bash setup-solr-docker.sh
```

4. **Verify**
   - Application: http://localhost:8080/api/v1/jobtitles/health
   - Solr Admin: http://localhost:8983/solr
   - Test Search: http://localhost:8080/api/v1/jobtitles/search?query=developer&language=en

5. **View logs**
```bash
   docker-compose logs -f app
```

### Stop Services
```bash
docker-compose down
```

### Clean Reset
```bash
docker-compose down -v
docker-compose up -d
bash setup-solr-docker.sh
```



### 5. Add .dockerignore
```
target/
.mvn/
*.log
*.class
.git/
.idea/
*.iml
node_modules/

### 6. Verify 

```bash
# Test search
curl "http://localhost:8080/api/v1/jobtitles/search?query=developer&language=en"

# Check health
curl http://localhost:8080/actuator/health

# IntelliJ IDEA Setup Guide

## Prerequisites

Before running the application in IntelliJ IDEA, ensure you have:

1. **Java 21** - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java21) or [OpenJDK](https://adoptium.net/)
2. **IntelliJ IDEA** - 2023.3 or later (Community or Ultimate)
3. **Maven** - 3.8+ (bundled with IntelliJ)
4. **Docker Desktop** - For running Solr and Redis

---

## Step 1: Install Java 21

### Verify Java Installation
```bash
java -version
```

You should see output like:
```
openjdk version "21.0.x"
```

### Set JAVA_HOME (if needed)

**macOS/Linux:**
```bash
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
```

**Windows:**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
```

---

## Step 2: Import Project into IntelliJ IDEA

### Option A: From Version Control
1. Open IntelliJ IDEA
2. Click **File → New → Project from Version Control**
3. Enter the repository URL
4. Click **Clone**

### Option B: Open Existing Project
1. Open IntelliJ IDEA
2. Click **File → Open**
3. Navigate to the project root directory
4. Select the `pom.xml` file
5. Click **Open as Project**

---

## Step 3: Configure IntelliJ Project Settings

### 3.1 Set Project SDK
1. Go to **File → Project Structure** (Ctrl+Alt+Shift+S / Cmd+;)
2. Under **Project**, set:
   - **SDK**: Select Java 21
   - **Language Level**: 21 (Preview)
3. Click **Apply**

### 3.2 Configure Maven
1. In **File → Project Structure → Modules**
2. Verify Maven is detected
3. Go to **File → Settings → Build, Execution, Deployment → Build Tools → Maven**
4. Set:
   - **Maven home directory**: Use bundled Maven or specify custom
   - **JDK for importer**: Java 21
5. Click **Apply**

### 3.3 Reload Maven Project
1. Right-click on `pom.xml` in Project view
2. Select **Maven → Reload Project**
3. Wait for dependencies to download

---

## Step 4: Start Dependencies with Docker

### 4.1 Start Docker Desktop
Ensure Docker Desktop is running

### 4.2 Start Solr and Redis
Open terminal in project root and run:

```bash
docker-compose up -d solr redis
```

### 4.3 Verify Services
Check if services are healthy:
```bash
docker-compose ps
```

Expected output:
```
NAME                  STATUS              PORTS
job-search-solr       Up (healthy)        0.0.0.0:8983->8983/tcp
job-search-redis      Up (healthy)        0.0.0.0:6379->6379/tcp
```

### 4.4 Access Solr Admin UI
Open browser: http://localhost:8983/solr

---

## Step 5: Configure Run Configuration in IntelliJ

### 5.1 Create Application Run Configuration
1. Click **Run → Edit Configurations**
2. Click **+** (Add New Configuration)
3. Select **Application**
4. Configure:
   - **Name**: `JobSearchApplication`
   - **Module**: `job-search`
   - **Main class**: `com.bet99.exercise.jobsearch.JobSearchApplication`
   - **JDK**: Java 21
   - **VM options**: `--enable-preview -XX:+UseZGC`
   - **Working directory**: `$PROJECT_DIR$`
   - **Use classpath of module**: `job-search`
5. Click **Apply** and **OK**

### 5.2 Alternative: Run with Maven
Create Maven Run Configuration:
1. Click **Run → Edit Configurations**
2. Click **+** (Add New Configuration)
3. Select **Maven**
4. Configure:
   - **Name**: `Maven Spring Boot Run`
   - **Command line**: `spring-boot:run`
   - **Working directory**: `$PROJECT_DIR$`
5. Click **Apply** and **OK**

---

## Step 6: Run the Application

### Method 1: Using Run Configuration
1. Select `JobSearchApplication` from the run configurations dropdown
2. Click the green **Run** button (▶) or press **Shift+F10**

### Method 2: Right-click Run
1. Navigate to `JobSearchApplication.java`
2. Right-click on the file or the class name
3. Select **Run 'JobSearchApplication.main()'**

### Method 3: Maven Command
In IntelliJ terminal:
```bash
./mvnw spring-boot:run
```

---

## Step 7: Verify Application is Running

### 7.1 Check Console Output
You should see:
```
Started JobSearchApplication in X.XXX seconds
```

### 7.2 Test Health Endpoint
Open browser or use curl:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
   "status": "UP"
}
```

### 7.3 Test Search Endpoint
```bash
curl "http://localhost:8080/api/v1/jobtitles/search?query=software&language=en"
```

---

### Maven Tool Window
1. View → Tool Windows → Maven
2. Use this to:
   - Refresh dependencies
   - Run Maven goals
   - View dependency tree

### Database/Redis Tool (Ultimate Edition)
1. View → Tool Windows → Database
2. Add Redis data source to inspect cache

---

## Step 8: Load Sample Data

The application automatically loads NOC data on startup if the CSV files are present in `src/main/resources/data/`.

### Place Data Files
1. Download NOC 2021 CSV files:
   - `noc_2021_version_1.0__classification_structure.csv`
   - `noc_2021_version_1.0__elements.csv`
2. Place them in: `src/main/resources/data/`
3. Restart the application

### Monitor Data Loading
Check console for:
```
Starting NOC data loading with parallel processing...
Loaded X classifications in Xms
Processed X unique job titles
Progress: X/X batches
```
# Browser Testing Guide

## 🌐 How to Test the Job Search API in Your Browser

### Prerequisites
- ✅ Application is running (see green "Started JobSearchApplication" in console)
- ✅ Solr is configured (ran setup-solr script)
- ✅ Data is loaded (check console logs)

---

## Quick Test URLs (Copy & Paste)

### 1. Health Check
```
http://localhost:8080/api/v1/jobtitles/health
```
**Expected Result:**
```
Job Title Search Service is running
```

---

### 2. Basic Search - English

**Search for "software":**
```
http://localhost:8080/api/v1/jobtitles/search?query=software&language=en
```

**Search for "engineer":**
```
http://localhost:8080/api/v1/jobtitles/search?query=engineer&language=en
```

**Search for "manager":**
```
http://localhost:8080/api/v1/jobtitles/search?query=manager&language=en
```

**Search for "developer":**
```
http://localhost:8080/api/v1/jobtitles/search?query=developer&language=en
```

---
---

## Troubleshooting

### Issue: "Cannot resolve symbol" errors
**Solution**:
- Right-click project → Maven → Reload Project
- File → Invalidate Caches → Invalidate and Restart

### Issue: Java 21 preview features not working
**Solution**:
- Verify `--enable-preview` is in VM options
- Check Java Compiler settings have `--enable-preview`
- Rebuild project: Build → Rebuild Project

### Issue: Solr connection refused
**Solution**:
```bash
docker-compose restart solr
# Wait for healthy status
docker-compose ps
```

### Issue: Port 8080 already in use
**Solution**:
- Change port in `application.yml`: `server.port: 8081`
- Or kill the process using port 8080

### Issue: Out of memory
**Solution**:
- Increase heap size in VM options: `-Xmx2g`
- Or use: `-XX:MaxRAMPercentage=75.0`

### Issue: Maven dependencies won't download
**Solution**:
- Check internet connection
- File → Settings → Maven → uncheck "Work offline"
- Delete `~/.m2/repository` and reload

---

## Additional Resources

- [IntelliJ IDEA Documentation](https://www.jetbrains.com/idea/documentation/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)
- [Apache Solr Guide](https://solr.apache.org/guide/)
