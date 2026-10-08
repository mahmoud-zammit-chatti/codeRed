# API Gateway Request Flow & Troubleshooting Guide

## Request Flow After Fixes

### Successful Request Path

```
CLIENT REQUEST
    │
    ├─ GET http://localhost:8080/api/incidents/get-all-pending
    ├─ Header: Authorization: Bearer <JWT_TOKEN>
    │
API GATEWAY (port 8080)
    ├─ Request received by Spring Cloud Gateway
    │
SECURITY FILTER (SecurityConfig)
    ├─ Check path matches "/api/**" ✓
    ├─ Verify JWT token from header
    ├─ Token validation: Valid ✓
    │
GATEWAY ROUTING (GatewayConfig - RouteLocator)
    ├─ Match route: incident-service
    ├─ Check predicate: Path=/api/incidents/** ✓
    │
GATEWAY FILTERS
    ├─ ApplyPredicates ✓
    ├─ RewritePath: /api/incidents/get-all-pending → /get-all-pending ✓
    ├─ ForwardRequest to: http://localhost:8081/get-all-pending
    │
INCIDENT SERVICE (port 8081)
    ├─ Receive GET /get-all-pending
    ├─ Route to IncidentController.getPendingIncident()
    ├─ Query database
    ├─ Return ResponseEntity<ArrayList<IncidentResponseDTO>>
    │
RESPONSE BACK THROUGH GATEWAY
    ├─ Add CORS headers
    ├─ Compress response
    │
CLIENT RECEIVES
    ├─ Status: 200 OK
    ├─ Body: [{"id": 1, ...}, ...]
    └─ Headers: Access-Control-Allow-Origin: *
```

---

## Configuration Summary

### API Gateway (Port 8080)

| Component | Configuration | Purpose |
|-----------|---|---|
| **Routing** | GatewayConfig.java | Explicit route registration with precedence |
| **Security** | SecurityConfig.java | JWT authentication for /api/** paths |
| **Paths** | application.yaml | Route definitions with RewritePath filters |

### Backend Services

| Service | Port | Path Prefix | Actual Endpoint |
|---------|------|-------------|-----------------|
| Incident Service | 8081 | `/api/incidents/**` | `/incidents/**` |
| Dispatch Service | 8082 | `/api/dispatch/**` | `/dispatch/**` |
| AI Service | 8083 | `/api/ai/**` | `/ai/**` |
| Notification Service | 8084 | `/api/notifications/**` | `/notifications/**` |

---

## Troubleshooting Guide

### Problem 1: "Resource not found" Error

**Symptoms**:
```
o.s.w.r.resource.ResourceWebHandler : [id-123] Resource not found
```

**Causes & Solutions**:

1. **Route Not Matching**
   - Check the request path matches a route in GatewayConfig
   - Verify predicates match (e.g., `Path=/api/incidents/**`)
   - Solution: Check logs for `Trying to match using PathMatcherServerWebExchangeMatcher`

2. **Backend Service Not Running**
   - Check if incident-service is running on port 8081
   - Solution: `curl http://localhost:8081/actuator/health`

3. **RewritePath Filter Issue**
   - Check if path is being rewritten correctly
   - Solution: Enable DEBUG logging for `org.springframework.cloud.gateway`
   - Look for: `Applying default filters...` in logs

**Debug Commands**:
```bash
# Test backend service directly
curl http://localhost:8081/get-all-pending

# Test through gateway (should fail with auth error if working)
curl http://localhost:8080/api/incidents/get-all-pending

# Test with token
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/incidents/get-all-pending
```

---

### Problem 2: "Unauthorized" or "403 Forbidden"

**Symptoms**:
```
2026-10-08T13:18:51.335+01:00 DEBUG 1452616 --- [api-gateway] [...] Authorization failed
```

**Causes & Solutions**:

1. **Invalid/Missing JWT Token**
   - Token may be expired
   - Token issuer-uri mismatch
   - Solution: Verify token with: `echo <TOKEN> | base64 -d`

2. **Token Issuer Configuration**
   - Check `application.yaml` issuer-uri matches your OAuth2 provider
   - Current config: `http://localhost:8180/realms/code-red`
   - Solution: Verify this matches your Keycloak realm

3. **Wrong JWT Credentials**
   - Ensure you're using a valid JWT token from your provider
   - Solution: Test directly with OAuth2 provider

**Debug Commands**:
```bash
# Decode JWT token (online tool or jwt.io)
# Check 'exp' claim hasn't passed current time

# Test OAuth2 token endpoint
curl -X POST http://localhost:8180/realms/code-red/protocol/openid-connect/token \
  -d "client_id=<client_id>" \
  -d "grant_type=password" \
  -d "username=<username>" \
  -d "password=<password>"
```

---

### Problem 3: "Connection Refused" to Backend Service

**Symptoms**:
```
Unable to connect to incident-service:8081
Connection refused
```

**Causes & Solutions**:

1. **Backend Service Not Running**
   - Solution: Start the service with Maven
   ```bash
   cd incident-service
   ./mvnw spring-boot:run
   ```

2. **Wrong Port Configuration**
   - Check `dispatch-service/application.yaml` has `port: 8082`
   - Check `notification-service/application.yaml` has `port: 8084`
   - Solution: Verify ports in gateway routes match service configurations

3. **Network/Firewall Issues**
   - Test localhost connectivity
   - Solution: `telnet localhost 8081`

**Debug Commands**:
```bash
# Check if port is listening
netstat -tulpn | grep 8081

# Test connectivity
curl -v http://localhost:8081/actuator/health

# Check service logs
docker logs <container_name>
```

---

### Problem 4: CORS Error in Browser

**Symptoms**:
```
Access to XMLHttpRequest at 'http://localhost:8080/api/incidents/...' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Causes & Solutions**:

1. **CORS Headers Not Present**
   - SecurityConfig now includes CORS configuration
   - Verify: Check response headers include `Access-Control-Allow-Origin`
   - Solution: Restart gateway after SecurityConfig update

2. **Wildcard CORS Too Permissive**
   - Current config: `configuration.addAllowedOrigin("*")`
   - For production: Replace with specific origins
   ```java
   configuration.addAllowedOrigin("http://localhost:3000");
   configuration.addAllowedOrigin("https://yourdomain.com");
   ```

**Debug Commands**:
```bash
# Check CORS headers
curl -i -X OPTIONS \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  http://localhost:8080/api/incidents/get-all-pending
```

---

### Problem 5: Path Rewriting Issue

**Symptoms**:
```
Backend receives: GET /api/incidents/get-all-pending
Expected: GET /get-all-pending
```

**Causes & Solutions**:

1. **RewritePath Filter Not Applied**
   - Check GatewayConfig has rewritePath filter
   - Current regex: `/api/incidents(?<segment>/?.*), ${segment}`
   - Solution: Verify filter order in route definition

2. **Regex Pattern Issue**
   - Pattern must match actual request path
   - Solution: Enable DEBUG logging and check actual path

**Debug Commands**:
```bash
# Check logs for filter application
grep -i "rewritepath\|applying filter" <gateway_logs>

# Monitor actual request path
curl -v http://localhost:8080/api/incidents/get-all-pending \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Verification Commands

### 1. Gateway Health Check
```bash
curl http://localhost:8080/actuator/health
```
Expected response: `{"status":"UP"}`

### 2. Test Each Service Route

```bash
# Incident Service
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/incidents/get-all-pending

# Dispatch Service
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/dispatch/...

# AI Service
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/ai/...

# Notification Service
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/notifications/...
```

### 3. Check Gateway Routes Registered
```bash
curl http://localhost:8080/actuator/gateway/routes
```
Expected: List of all 4 routes with their configurations

### 4. Monitor Gateway Logs
```bash
# For Spring Cloud Gateway processing
docker logs <gateway_container> | grep -i "gateway\|route\|rewrite"

# For security processing
docker logs <gateway_container> | grep -i "authorization\|security"
```

---

## Key Changes Made

| File | Change | Why |
|------|--------|-----|
| `application.yaml` | Added RewritePath filters | Routes requests properly to backend services |
| `SecurityConfig.java` | Split auth policies | Allows gateway infrastructure + auth for API paths |
| `GatewayConfig.java` | NEW - Explicit routes | Ensures routes take precedence over resource handlers |
| `dispatch-service/application.yaml` | Added port 8082 | Service now listens on correct port |
| `notification-service/application.yaml` | Added port 8084 | Service now listens on correct port |

---

## Performance Considerations

1. **Request Size Limit**: 5MB (configured in application.yaml)
2. **Response Compression**: Enabled for faster transfer
3. **CORS Validation**: Applied at gateway level (reduces backend load)
4. **Route Caching**: Spring Cloud Gateway caches matched routes

To adjust, modify in `application.yaml`:
```yaml
default-filters:
  - name: RequestSize
    args:
      maxSize: 10000000  # Increase to 10MB if needed
```

---

## Next Steps

1. **Start all services**:
   ```bash
   docker-compose up
   # or manually start each service
   ```

2. **Obtain JWT token** from your OAuth2 provider (Keycloak)

3. **Test endpoints** using the verification commands above

4. **Monitor logs** during requests to verify routing path

5. **Adjust CORS origins** for production use

6. **Consider adding rate limiting** filters for production

---