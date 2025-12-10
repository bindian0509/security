#!/bin/bash

# Script to start MySQL and MongoDB databases using Docker Compose

echo "🚀 Starting MySQL and MongoDB databases..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Start databases
docker-compose up -d

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 5

# Check MySQL health
if docker-compose exec -T mysql mysqladmin ping -h localhost -uroot -prootpassword > /dev/null 2>&1; then
    echo "✅ MySQL is ready!"
else
    echo "⚠️  MySQL is starting... (may take a few more seconds)"
fi

# Check MongoDB health
if docker-compose exec -T mongodb mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
    echo "✅ MongoDB is ready!"
else
    echo "⚠️  MongoDB is starting... (may take a few more seconds)"
fi

echo ""
echo "📊 Database Status:"
docker-compose ps

echo ""
echo "🔗 Connection Details:"
echo "MySQL:    localhost:3306 (user: root, password: rootpassword)"
echo "MongoDB:  localhost:27017 (user: admin, password: adminpassword)"
echo ""
echo "🛠️  Optional Admin Tools (start with: docker-compose --profile tools up -d):"
echo "phpMyAdmin:  http://localhost:8082"
echo "Mongo Express: http://localhost:8081"
echo ""
echo "📝 To stop databases: docker-compose down"
echo "📝 To view logs: docker-compose logs -f"

