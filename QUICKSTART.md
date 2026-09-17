# Quick Start — запуск на новой машине

## 1. Требования
- **JDK 17** (Temurin 17.0.20.1 или новее) — `C:\Users\akoly\.jdks\temurin-17.0.20.1`
- **Docker Desktop**
- **Maven** (входит в IDEA)
- **PostgreSQL** (через Docker)

## 2. Настройка JAVA_HOME
```powershell
$env:JAVA_HOME = "C:\Users\akoly\.jdks\temurin-17.0.20.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version  # проверить: 17.0.x
```

> 💡 Постоянно добавить в переменные среды → Переменные среды → JAVA_HOME → `C:\Users\akoly\.jdks\temurin-17.0.20.1`

## 3. Запуск через Docker (рекомендуется)
```powershell
docker-compose up -d --build
```
Приложение доступно на `http://localhost:8080`

## 4. Запуск из IntelliJ IDEA (локально)
1. В `src/main/resources/application.properties` хост БД должен быть `localhost`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/familytree
   ```
2. Запустить Docker:
   ```powershell
   docker-compose up -d db
   ```
3. Правый клик на `FamilyTreeApplication.java` → `Run`

## 5. Проверка
```powershell
Invoke-RestMethod "http://localhost:8080/api/persons"
```

## 6. Тесты
```powershell
$env:JAVA_HOME = "C:\Users\akoly\.jdks\temurin-17.0.20.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
mvn test
```
