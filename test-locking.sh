#!/bin/bash

# Script to test optimistic and pessimistic locking

BASE_URL="http://localhost:8080/api/locking"

echo "🧪 Testing Optimistic and Pessimistic Locking"
echo "=============================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Create a product
echo -e "${YELLOW}Test 1: Creating a product...${NC}"
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/mysql/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Laptop",
    "description": "Gaming Laptop for Testing",
    "price": 1299.99,
    "stock": 10
  }')

PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✅ Product created with ID: $PRODUCT_ID${NC}"
echo ""

# Test 2: Get product
echo -e "${YELLOW}Test 2: Getting product...${NC}"
curl -s "$BASE_URL/mysql/products/$PRODUCT_ID" | jq '.'
echo ""

# Test 3: Optimistic locking - concurrent update simulation
echo -e "${YELLOW}Test 3: Testing Optimistic Locking (concurrent update)...${NC}"
echo "Sending two update requests simultaneously..."
echo ""

# First request (background)
curl -s -X PUT "$BASE_URL/mysql/products/$PRODUCT_ID/optimistic" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated V1","price":1200,"stock":9}' > /tmp/request1.json &

# Second request (should fail with optimistic lock)
curl -s -X PUT "$BASE_URL/mysql/products/$PRODUCT_ID/optimistic" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated V2","price":1100,"stock":8}' > /tmp/request2.json &

wait

echo "Request 1 result:"
cat /tmp/request1.json | jq '.'
echo ""
echo "Request 2 result (should show OptimisticLockException):"
cat /tmp/request2.json | jq '.'
echo ""

# Test 4: Pessimistic locking
echo -e "${YELLOW}Test 4: Testing Pessimistic Locking...${NC}"
echo "Sending two update requests (second will wait)..."
echo ""

# First request (takes 2 seconds)
curl -s -X PUT "$BASE_URL/mysql/products/$PRODUCT_ID/pessimistic" \
  -H "Content-Type: application/json" \
  -d '{"name":"Pessimistic V1","price":1300,"stock":7}' > /tmp/pessimistic1.json &
PESSIMISTIC_PID=$!

sleep 0.5

# Second request (will wait for first to complete)
curl -s -X PUT "$BASE_URL/mysql/products/$PRODUCT_ID/pessimistic" \
  -H "Content-Type: application/json" \
  -d '{"name":"Pessimistic V2","price":1400,"stock":6}' > /tmp/pessimistic2.json

wait $PESSIMISTIC_PID

echo "Request 1 result:"
cat /tmp/pessimistic1.json | jq '.'
echo ""
echo "Request 2 result (waited for first to complete):"
cat /tmp/pessimistic2.json | jq '.'
echo ""

# Test 5: MongoDB - Create inventory
echo -e "${YELLOW}Test 5: Creating MongoDB inventory item...${NC}"
INVENTORY_RESPONSE=$(curl -s -X POST "$BASE_URL/mongodb/inventory" \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "Test Smartphone",
    "category": "Electronics",
    "price": 699.99,
    "quantity": 20
  }')

INVENTORY_ID=$(echo $INVENTORY_RESPONSE | grep -o '"_id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "${GREEN}✅ Inventory created with ID: $INVENTORY_ID${NC}"
echo ""

# Test 6: MongoDB optimistic locking
echo -e "${YELLOW}Test 6: Testing MongoDB Optimistic Locking...${NC}"
curl -s -X PUT "$BASE_URL/mongodb/inventory/$INVENTORY_ID/optimistic" \
  -H "Content-Type: application/json" \
  -d '{"itemName":"Updated Phone","price":649.99,"quantity":18}' | jq '.'
echo ""

echo -e "${GREEN}✅ All tests completed!${NC}"
echo ""
echo "💡 Tip: Run this script multiple times to see different locking behaviors"
echo "💡 Tip: Use two terminals to test true concurrency"

