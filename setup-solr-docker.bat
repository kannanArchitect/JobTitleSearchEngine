@echo off
echo =========================================
echo Job Title Search - Solr Schema Setup
echo =========================================
echo.

set SOLR_URL=http://localhost:8983/solr/jobtitles

echo Waiting for Solr to be ready...
timeout /t 20 /nobreak >nul
echo Solr should be ready now!
echo.

echo Adding field types...

curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field-type\":{\"name\":\"text_en\",\"class\":\"solr.TextField\",\"positionIncrementGap\":\"100\",\"analyzer\":{\"tokenizer\":{\"class\":\"solr.StandardTokenizerFactory\"},\"filters\":[{\"class\":\"solr.LowerCaseFilterFactory\"},{\"class\":\"solr.EnglishPossessiveFilterFactory\"},{\"class\":\"solr.PorterStemFilterFactory\"}]}}}" %SOLR_URL%/schema

curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field-type\":{\"name\":\"text_fr\",\"class\":\"solr.TextField\",\"positionIncrementGap\":\"100\",\"analyzer\":{\"tokenizer\":{\"class\":\"solr.StandardTokenizerFactory\"},\"filters\":[{\"class\":\"solr.LowerCaseFilterFactory\"},{\"class\":\"solr.ASCIIFoldingFilterFactory\"},{\"class\":\"solr.FrenchLightStemFilterFactory\"}]}}}" %SOLR_URL%/schema

echo.
echo Adding fields...

curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"noc_code\",\"type\":\"string\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema
curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"title_en\",\"type\":\"text_en\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema
curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"title_fr\",\"type\":\"text_fr\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema
curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"description_en\",\"type\":\"text_en\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema
curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"description_fr\",\"type\":\"text_fr\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema
curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"category\",\"type\":\"string\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema
curl -X POST -H "Content-type:application/json" --data-binary "{\"add-field\":{\"name\":\"skill_level\",\"type\":\"string\",\"stored\":true,\"indexed\":true}}" %SOLR_URL%/schema

echo.
echo =========================================
echo Setup complete!
echo =========================================
echo.
echo Solr Admin: http://localhost:8983/solr
echo Application: http://localhost:8080
echo.
pause
```

### 5. **.dockerignore** (Root directory)
```
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
.classpath
.project
.settings/

# Logs
*.log

# OS
.DS_Store
Thumbs.db

# Git
.git/
.gitignore

# Documentation
*.md
docs/

# Test data
*.csv