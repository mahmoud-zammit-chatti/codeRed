# API Gateway Configuration - Issues Found & Fixes Applied

## ISSUES IDENTIFIED

### 1. **Gateway Routes Not Being Prioritized** ❌
- **Problem**: Spring Cloud Gateway routes defined in `application.yaml` were not taking precedence over WebFlux's default `ResourceWebHandler`
- **Evidence**: Logs showed `Mapped to ResourceWebHandler [classpath [META-INF/resources/], ...]` instead of gateway route processing
- **Root Cause**: Routes were only configured in YAML without explicit `RouteLocator` bean, allowing Spring to use default handlers first

### 2. **Incorrect Security Authorization Policy** ❌
- **Problem**: SecurityConfig had `.anyExchange().authenticated()` which treated all paths equally
- **Evidence**: API requests were passing security but then being caught by resource handler
- **Root Cause**: No distinction between API paths that need authentication and gateway infrastructure paths

### 3. **Missing Path Rewriting Configuration** ❌
- **Problem**: Routes lacked proper path transformation before forwarding to backend services
- **Evidence**: Without `RewritePath` filters, backend services would receive `/api/incidents/...` instead of `/incidents/...`
- **Root Cause**: Gateway routes configuration was incomplete

### 4. **Missing Server Port Configuration** ❌
- **Problem**: `dispatch-service` and `notification-service` had no server port configuration
- **Evidence**: Gateway routes defined ports 8082 and 8084, but services didn't listen on those ports
- **Root Cause**: Minimal application.yaml files without server configuration

### 5. **No CORS Configuration** ❌
- **Problem**: No CORS handling at gateway level, could cause cross-origin request issues
- **Evidence**: Browser requests from different origins would fail
- **Root Cause**: Missing CORS configuration in security setup

### 6. **Spring Boot Version Mismatch** ⚠️
- **Problem**: ai-service uses Spring Boot 4.1.0-RC1 while api-gateway uses 4.0.6
- **Evidence**: Version mismatch in pom.xml files
- **Note**: Not critical but could cause compatibility issues

## FIXES APPLIED

### 1. **Created Explicit Gateway Configuration** ✅
**File**: `/home/niri/codeRed/api-gateway/src/main/java/com/mahmoud/api_gateway/Configs/GatewayConfig.java`

```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("incident-service", r -> r
                .path("/api/incidents/**")
                .filters(f -> f.rewritePath("/api/incidents(?<segment>/?.*)", "${segment}"))
                .uri("http://localhost:8081"))
            // ... other routes
            .build();
    }
}
```

**Impact**: Routes now have explicit bean registration and filter configuration, ensuring they take precedence over default handlers.

---

### 2. **Updated Security Configuration** ✅
**File**: `/home/niri/codeRed/api-gateway/src/main/java/com/mahmoud/api_gateway/Configs/SecurityConfig.java`

**Changes**:
- Separate authentication policy for `/api/**` paths (authenticated)
- Allow health checks without authentication
- Allow other paths without authentication (permits gateway routing)
- Added CORS configuration for cross-origin requests

**Before**:
```java
.anyExchange().authenticated()  // ALL paths require authentication
```

**After**:
```java
.pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
.pathMatchers("/api/**").authenticated()
.anyExchange().permitAll()  // Allows gateway infrastructure paths
```

**Impact**: Security now properly distinguishes between API paths and infrastructure paths.

---

### 3. **Updated Gateway Configuration YAML** ✅
**File**: `/home/niri/codeRed/api-gateway/src/main/resources/application.yaml`

**Added**:
- `RewritePath` filters for each route to strip service prefix before forwarding
- `default-filters` configuration for request size limits
- Proper server compression settings
- Better organized route definitions

**Example Route**:
```yaml
- id: incident-service
  uri: http://localhost:8081
  predicates:
    - Path=/api/incidents/**
  filters:
    - RewritePath=/api/incidents(?<segment>/?.*), $\{segment}
```

**Impact**: Requests are now properly rewritten before forwarding to backend services.

---

### 4. **Added Server Port Configuration** ✅
**Files**:
- `/home/niri/codeRed/dispatch-service/src/main/resources/application.yaml` - Port 8082
- `/home/niri/codeRed/notification-service/src/main/resources/application.yaml` - Port 8084

**Impact**: Services now listen on the correct ports expected by gateway routes.

---

## HOW THE ROUTING NOW WORKS

```
Client Request
    ↓
API Gateway (port 8080)
    ↓
Security Filter (checks if /api/** paths are authenticated)
    ↓
Spring Cloud Gateway RouteLocator
    ↓
Route Matching (Path=/api/incidents/**)
    ↓
RewritePath Filter (/api/incidents/get-all-pending → /get-all-pending)
    ↓
Forward to Backend Service (http://localhost:8081/get-all-pending)
    ↓
Incident Service (port 8081)
    ↓
Response back through gateway
```

---

## TESTING CHECKLIST

- [ ] **Test Incident Service**: `curl -H "Authorization: Bearer <token>" http://localhost:8080/api/incidents/get-all-pending`
- [ ] **Test Dispatch Service**: `curl -H "Authorization: Bearer <token>" http://localhost:8080/api/dispatch/**`
- [ ] **Test AI Service**: `curl -H "Authorization: Bearer <token>" http://localhost:8080/api/ai/**`
- [ ] **Test Notification Service**: `curl -H "Authorization: Bearer <token>" http://localhost:8080/api/notifications/**`
- [ ] **Health Check**: `curl http://localhost:8080/actuator/health` (should not require token)
- [ ] **CORS Headers**: Verify Access-Control-Allow-Origin headers in response
- [ ] **Backend Direct Access**: Verify services still respond when accessed directly (e.g., `http://localhost:8081/get-all-pending`)

---

## ADDITIONAL RECOMMENDATIONS

1. **Version Alignment**: Consider updating ai-service to Spring Boot 4.0.6 to match other services
2. **Add Response Compression**: Already configured in application.yaml
3. **Add Rate Limiting**: Consider adding rate limit filters at gateway level
4. **Add Logging**: Consider adding logging filters to track requests through gateway
5. **Add Health Checks**: Add health endpoints to backend services for monitoring