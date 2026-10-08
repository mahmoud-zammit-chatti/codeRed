#!/bin/bash

# API Gateway Configuration Verification Script
# This script helps verify that the API Gateway is properly configured

echo "==================================================="
echo "API Gateway Configuration Verification"
echo "==================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if services are running
check_service() {
    local service_name=$1
    local port=$2

    echo -n "Checking $service_name (port $port)... "
    if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Running${NC}"
        return 0
    else
        echo -e "${RED}✗ Not responding${NC}"
        return 1
    fi
}

echo "Step 1: Checking if services are running"
echo "-------------------------------------------"

services=(
    "API Gateway:8080"
    "Incident Service:8081"
    "Dispatch Service:8082"
    "AI Service:8083"
    "Notification Service:8084"
)

all_running=true
for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if ! check_service "$name" "$port"; then
        all_running=false
    fi
done

echo ""
echo "Step 2: Testing Gateway Routes"
echo "-------------------------------------------"

# Note: These require a valid JWT token in production
# For testing, you may need to disable authentication or use a test token

echo -n "Testing health endpoint (no auth required)... "
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ Failed${NC}"
fi

echo -n "Testing incident service route structure... "
if curl -s http://localhost:8080/api/incidents/get-all-pending 2>&1 | grep -q "error\|Unauthorized\|Forbidden"; then
    echo -e "${YELLOW}⚠ Expected auth error (gateway routing works)${NC}"
else
    echo -e "${YELLOW}⚠ Need to test with valid JWT token${NC}"
fi

echo ""
echo "Step 3: Configuration Files Status"
echo "-------------------------------------------"

files=(
    "api-gateway/src/main/resources/application.yaml"
    "api-gateway/src/main/java/com/mahmoud/api_gateway/Configs/SecurityConfig.java"
    "api-gateway/src/main/java/com/mahmoud/api_gateway/Configs/GatewayConfig.java"
    "incident-service/src/main/resources/application.yaml"
    "dispatch-service/src/main/resources/application.yaml"
    "ai-service/src/main/resources/application.yaml"
    "notification-service/src/main/resources/application.yaml"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file (MISSING)"
    fi
done

echo ""
echo "==================================================="
echo "Verification Complete"
echo "==================================================="
echo ""
echo "Next Steps:"
echo "1. Ensure all services are running (use docker-compose up)"
echo "2. Test with a valid JWT token from your OAuth2 provider"
echo "3. Example: curl -H 'Authorization: Bearer <TOKEN>' http://localhost:8080/api/incidents/get-all-pending"
echo ""