# API Gateway - Expected Readable Response Format

## How to See the Readable Result

You've successfully set up the gateway! Now you need to execute the request and view the response.

---

## Step 1: Get Your JWT Token from Keycloak

```bash
# Replace with your actual password
TOKEN=$(curl -s -X POST http://localhost:8180/realms/code-red/protocol/openid-connect/token \
  -d "client_id=postman" \
  -d "grant_type=password" \
  -d "username=testuser" \
  -d "password=<your_password>" | jq -r '.access_token')

echo $TOKEN
```

---

## Step 2: Test the Gateway and View the Response

### **Option A: Simple curl (displays raw JSON)**

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/incidents/get-all-pending
```

**Output**:
```json
[{"id":"550e8400-e29b-41d4-a716-446655440000","title":"Building Fire","description":"Fire detected in downtown building","location":"123 Main St","type":"FIRE","status":"PENDING","severity":"CRITICAL","agentId":null,"dispatchedUnitId":null,"reportedBy":"John Doe","createdAt":"2026-10-08T13:00:00Z","updatedAt":"2026-10-08T13:00:00Z"},{"id":"550e8400-e29b-41d4-a716-446655440001","title":"Medical Emergency","description":"Person unconscious at train station","location":"Central Station","type":"MEDICAL","status":"PENDING","severity":"HIGH","agentId":null,"dispatchedUnitId":null,"reportedBy":"Jane Smith","createdAt":"2026-10-08T13:15:00Z","updatedAt":"2026-10-08T13:15:00Z"}]
```

### **Option B: Pretty-printed with jq (formatted & readable)**

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/incidents/get-all-pending | jq .
```

**Output** (formatted):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Building Fire",
    "description": "Fire detected in downtown building",
    "location": "123 Main St",
    "type": "FIRE",
    "status": "PENDING",
    "severity": "CRITICAL",
    "agentId": null,
    "dispatchedUnitId": null,
    "reportedBy": "John Doe",
    "createdAt": "2026-10-08T13:00:00Z",
    "updatedAt": "2026-10-08T13:00:00Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Medical Emergency",
    "description": "Person unconscious at train station",
    "location": "Central Station",
    "type": "MEDICAL",
    "status": "PENDING",
    "severity": "HIGH",
    "agentId": null,
    "dispatchedUnitId": null,
    "reportedBy": "Jane Smith",
    "createdAt": "2026-10-08T13:15:00Z",
    "updatedAt": "2026-10-08T13:15:00Z"
  }
]
```

### **Option C: Verbose (shows headers + response)**

```bash
curl -v -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/incidents/get-all-pending
```

**Output** (includes request/response headers):
```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /api/incidents/get-all-pending HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.85.0
> Accept: */*
> Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
>
< HTTP/1.1 200 OK
< Content-Type: application/json
< Content-Length: 1245
< Transfer-Encoding: chunked
< Date: Wed, 08 Oct 2026 14:30:39 GMT
<
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Building Fire",
    ...
  }
]
```

### **Option D: Just HTTP status and headers**

```bash
curl -i -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/incidents/get-all-pending
```

**Output**:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 1245
Transfer-Encoding: chunked
Date: Wed, 08 Oct 2026 14:30:39 GMT

[...]
```

---

## Response Structure Explanation

Each incident in the response contains:

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Unique incident identifier |
| `title` | String | Short incident title |
| `description` | String | Detailed incident description |
| `location` | String | Physical location of incident |
| `type` | Enum | Type: FIRE, MEDICAL, ACCIDENT, OTHER |
| `status` | Enum | Status: PENDING, IN_PROGRESS, RESOLVED |
| `severity` | Enum | Severity: LOW, MEDIUM, HIGH, CRITICAL |
| `agentId` | UUID | ID of assigned dispatch agent (null if unassigned) |
| `dispatchedUnitId` | UUID | ID of dispatched response unit (null if not dispatched) |
| `reportedBy` | String | Name of person who reported incident |
| `createdAt` | ISO DateTime | When incident was created |
| `updatedAt` | ISO DateTime | When incident was last updated |

---

## Using Postman (Alternative to curl)

1. **Create a new GET request**
   ```
   GET http://localhost:8080/api/incidents/get-all-pending
   ```

2. **Add Authorization header**
   - Tab: "Headers"
   - Key: `Authorization`
   - Value: `Bearer YOUR_JWT_TOKEN`

3. **Click "Send"**

4. **View Response**
   - Body tab shows the formatted JSON response
   - Status shows `200 OK` if successful

---

## Complete Example Script

```bash
#!/bin/bash

# 1. Get token from Keycloak
echo "Getting JWT token from Keycloak..."
TOKEN=$(curl -s -X POST http://localhost:8180/realms/code-red/protocol/openid-connect/token \
  -d "client_id=postman" \
  -d "grant_type=password" \
  -d "username=testuser" \
  -d "password=yourpassword" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "Failed to get token. Check credentials and Keycloak."
    exit 1
fi

echo "Token obtained successfully!"
echo ""

# 2. Test Incident Service through API Gateway
echo "Testing Incident Service endpoint..."
echo "Request: GET http://localhost:8080/api/incidents/get-all-pending"
echo "Authorization: Bearer $TOKEN"
echo ""
echo "Response:"
echo "=========="

curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/incidents/get-all-pending | jq .

echo ""
echo "=========="
echo "Done!"
```

Save this as `test_api.sh` and run:
```bash
chmod +x test_api.sh
./test_api.sh
```

---

## Testing All Services

### Dispatch Service
```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/dispatch/... | jq .
```

### AI Service
```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/ai/... | jq .
```

### Notification Service
```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/notifications/... | jq .
```

---

## If You Don't See Data

### Problem 1: "401 Unauthorized"
```json
{
  "error": "unauthorized",
  "error_description": "Full authentication is required to access this resource"
}
```
**Solution**: Token is invalid or expired. Get a new token from Keycloak.

### Problem 2: "403 Forbidden"
```json
{
  "error": "access_denied",
  "error_description": "Access is denied"
}
```
**Solution**: User doesn't have required permissions. Check Keycloak roles.

### Problem 3: Empty Array `[]`
```json
[]
```
**Solution**: No incidents in PENDING status. Create a new incident first using:
```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Incident",
    "description": "Test description",
    "location": "Test Location",
    "type": "FIRE",
    "severity": "HIGH",
    "reportedBy": "Test User"
  }' \
  http://localhost:8080/api/incidents/create
```

### Problem 4: "Connection refused"
```
curl: (7) Failed to connect to localhost port 8080: Connection refused
```
**Solution**: Gateway is not running. Start it with:
```bash
docker-compose up -d api-gateway
# or
cd api-gateway && ./mvnw spring-boot:run
```

---

## Summary

✅ **Gateway is working correctly** (as shown in your logs)  
✅ **You need to execute the curl command** to see the readable response  
✅ **The response is JSON** containing incident data  
✅ **Use `jq .` to format it nicely** (or use Postman)

---

**Try this now:**
```bash
# Save token
TOKEN=$(curl -s -X POST http://localhost:8180/realms/code-red/protocol/openid-connect/token \
  -d "client_id=postman" -d "grant_type=password" \
  -d "username=testuser" -d "password=yourpassword" | jq -r '.access_token')

# View incidents
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/incidents/get-all-pending | jq .
```

The response will be a JSON array of incident objects like shown in "Option B" above.