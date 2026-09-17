# REST API — примеры тестовых запросов (PowerShell)

## Настройка кодировки для кириллицы
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

---

## 1. Создать person
```powershell
$body = '{"firstName":"Иван","lastName":"Иванов","birthDate":"1990-01-01","ownerUsername":"admin"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -ContentType "application/json; charset=utf-8"
```

## 2. Создать person (латиница)
```powershell
$body = '{"firstName":"Maria","lastName":"Ivanova","birthDate":"1992-05-15","ownerUsername":"admin"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
```

## 3. Создать ребёнка с parent ссылками
```powershell
$body = '{"firstName":"Petya","lastName":"Ivanov","birthDate":"2015-03-20","fatherId":"1","motherId":"2"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
```

## 4. Получить person по ID
```powershell
Invoke-RestMethod "http://localhost:8080/api/persons/1"
```

## 5. Список всех persons
```powershell
Invoke-RestMethod "http://localhost:8080/api/persons"
```

## 6. Семейное дерево
```powershell
Invoke-RestMethod "http://localhost:8080/api/persons/tree?username=admin"
```

## 7. Обновить person
```powershell
$body = '{"firstName":"Иван","lastName":"Иванов","bio":"Любит рыбалку","birthDate":"1990-01-01"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/1" -Method Put -Body $body -ContentType "application/json"
```

## 8. Обновить фото (stub — URL строкой)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/1/photo" -Method Post -Body '"http://example.com/photo.jpg"' -ContentType "application/json"
```

## 9. Claim профиля
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/1/claim" -Method Patch -Body '"admin"' -ContentType "application/json"
```

## 10. Удалить person
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/persons/3" -Method Delete
```

---

## 11. Валидация — пустое firstName (ожидается 400)
```powershell
$body = '{"firstName":"","lastName":"Ivanov","birthDate":"1990-01-01"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.ErrorDetails.Message
}
```

## 12. Валидация — пустое lastName (ожидается 400)
```powershell
$body = '{"firstName":"Ivan","lastName":"   ","birthDate":"1990-01-01"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.ErrorDetails.Message
}
```

## 13. Валидация — будущая birthDate (ожидается 400)
```powershell
$body = '{"firstName":"Ivan","lastName":"Ivanov","birthDate":"2099-01-01"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.ErrorDetails.Message
}
```

## 14. Валидация — bio > 2000 символов (ожидается 400)
```powershell
$longBio = "x" * 2001
$body = '{"firstName":"Ivan","lastName":"Ivanov","birthDate":"1990-01-01","bio":"' + $longBio + '"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
} catch {
    $_.ErrorDetails.Message
}
```

## 15. Валидация — успешный create (ожидается 200)
```powershell
$body = '{"firstName":"Ivan","lastName":"Ivanov","bio":"Любит рыбалку","birthDate":"1990-01-01","ownerUsername":"admin"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/persons" -Method Post -Body $body -ContentType "application/json"
```

## 16. Валидация на update — пустое firstName (ожидается 400)
```powershell
$body = '{"firstName":"","lastName":"Ivanov"}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons/1" -Method Put -Body $body -ContentType "application/json"
} catch {
    $_.ErrorDetails.Message
}
```

## 17. Entity not found — несуществующий ID (ожидается 404)
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/persons/999999"
} catch {
    $_.ErrorDetails.Message
}
```

---

## Формат JSON-ответов при ошибках

**Validation error (HTTP 400):**
```json
{
  "status": 400,
  "error": "Validation failed",
  "fieldErrors": {
    "firstName": "First name is required",
    "lastName": "Last name is required"
  }
}
```

**Entity not found (HTTP 404):**
```json
{
  "status": 404,
  "error": "Person not found with id: 999999"
}
```
