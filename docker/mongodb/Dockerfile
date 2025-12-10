# Dockerfile for MongoDB Database
FROM mongo:7.0

# Set environment variables
ENV MONGO_INITDB_ROOT_USERNAME=admin
ENV MONGO_INITDB_ROOT_PASSWORD=adminpassword
ENV MONGO_INITDB_DATABASE=security_db

# Expose MongoDB port
EXPOSE 27017

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD mongosh --eval "db.adminCommand('ping')" || exit 1

