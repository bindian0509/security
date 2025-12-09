# Server-Sent Events (SSE) Flight Search Implementation

## Overview

This implementation demonstrates **Server-Sent Events (SSE)** in a Spring Boot REST API for real-time flight search results. The system fetches flight data from 3 mock aggregators with different response times and streams results to the client as they arrive, **without waiting for slow aggregators**.

## Key Features

✅ **Non-blocking SSE streaming** - Results sent as they arrive
✅ **Multiple aggregators** - 3 mock flight data sources
✅ **Different response times** - Aggregator 1 (500ms), Aggregator 2 (1.5s), Aggregator 3 (5s)
✅ **Timeout handling** - Doesn't wait for slow aggregators
✅ **Best practices** - Proper error handling, logging, async configuration
✅ **Interactive demo** - HTML page for testing SSE in browser

## Architecture

```
Client (Browser)
    ↓
FlightController (SSE Endpoint)
    ↓
FlightAggregatorService (@Async methods)
    ↓
┌─────────────┬─────────────┬─────────────┐
│ Aggregator1 │ Aggregator2 │ Aggregator3 │
│   (500ms)   │   (1.5s)    │   (5s)      │
└─────────────┴─────────────┴─────────────┘
    ↓             ↓             ↓
Results streamed via SSE as they arrive
```

## Components

### 1. **FlightController** (`/api/flights`)
- **SSE Endpoint**: `GET /api/flights/search/stream`
- **Traditional REST**: `GET /api/flights/search` (for comparison)
- Handles SSE connection lifecycle
- Streams results as they arrive from aggregators

### 2. **FlightAggregatorService**
- Three `@Async` methods simulating different aggregators
- Uses `CompletableFuture` for parallel execution
- Mock data generation with realistic flight information

### 3. **DTOs**
- `Flight` - Flight data transfer object
- `FlightSearchRequest` - Search request with validation

### 4. **AsyncConfig**
- Thread pool configuration for async operations
- Enables `@Async` support

## API Endpoints

### SSE Stream Endpoint

```http
GET /api/flights/search/stream?origin=JFK&destination=LAX&departureDate=2024-12-05&passengers=1
Accept: text/event-stream
```

**Response Events:**
- `connected` - Connection established
- `status` - Status updates
- `aggregator1` - Flights from Aggregator 1 (JSON array)
- `aggregator2` - Flights from Aggregator 2 (JSON array)
- `aggregator3` - Flights from Aggregator 3 (JSON array, may arrive late)
- `error` - Error messages
- `complete` - Search completed

**Example Event:**
```
event: aggregator1
data: [{"flightNumber":"AA1001","airline":"American Airlines","origin":"JFK","destination":"LAX","price":350.00,"aggregator":"Aggregator1"}]
```

### Traditional REST Endpoint (for comparison)

```http
GET /api/flights/search?origin=JFK&destination=LAX&departureDate=2024-12-05&passengers=1
```

Returns all flights after waiting for all aggregators (with timeout).

## Testing

### Option 1: Interactive HTML Demo

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Open browser: `http://localhost:8080/flight-search.html`

3. Enter search criteria and click "Search Flights (SSE Stream)"

4. Watch results stream in real-time:
   - Aggregator 1 appears after ~500ms
   - Aggregator 2 appears after ~1.5s
   - Aggregator 3 appears after ~5s (if you wait)
   - The UI doesn't wait for Aggregator 3 - it completes after 3 seconds

### Option 2: Using curl

```bash
# Start SSE connection
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/api/flights/search/stream?origin=JFK&destination=LAX&departureDate=2024-12-05"

# You'll see events streaming:
# event: connected
# data: Flight search started. Fetching from 3 aggregators...
#
# event: aggregator1
# data: [{"flightNumber":"AA1001",...}]
#
# event: aggregator2
# data: [{"flightNumber":"DL2001",...}]
```

### Option 3: Using JavaScript EventSource

```javascript
const eventSource = new EventSource('/api/flights/search/stream?origin=JFK&destination=LAX&departureDate=2024-12-05');

eventSource.addEventListener('aggregator1', (e) => {
    const flights = JSON.parse(e.data);
    console.log('Aggregator 1:', flights);
});

eventSource.addEventListener('aggregator2', (e) => {
    const flights = JSON.parse(e.data);
    console.log('Aggregator 2:', flights);
});

eventSource.addEventListener('aggregator3', (e) => {
    const flights = JSON.parse(e.data);
    console.log('Aggregator 3 (late):', flights);
});

eventSource.addEventListener('complete', (e) => {
    console.log('Search completed:', e.data);
    eventSource.close();
});
```

## Best Practices Implemented

### 1. **Async Configuration**
- ✅ `@EnableAsync` with custom thread pool
- ✅ Proper thread naming for debugging
- ✅ Configurable pool size and queue capacity

### 2. **Error Handling**
- ✅ Try-catch in async methods
- ✅ Graceful degradation (empty list on error)
- ✅ Proper exception logging
- ✅ SSE error events to client

### 3. **SSE Best Practices**
- ✅ Proper timeout configuration (5 minutes)
- ✅ Connection lifecycle handlers (`onCompletion`, `onTimeout`, `onError`)
- ✅ Event naming for different data types
- ✅ JSON serialization for complex data

### 4. **Logging**
- ✅ Structured logging with SLF4J
- ✅ Parameterized log messages
- ✅ Appropriate log levels (INFO, WARN, ERROR)
- ✅ Context information (aggregator name, flight count)

### 5. **Code Quality**
- ✅ Constructor injection (not field injection)
- ✅ Immutable configuration constants
- ✅ Clear method names and documentation
- ✅ Separation of concerns (Service, Controller, DTO)

### 6. **Security**
- ✅ Input validation with `@Valid`
- ✅ Endpoint security configuration
- ✅ No sensitive data in logs

## Response Times

| Aggregator | Response Time | Flights Returned |
|------------|---------------|------------------|
| Aggregator 1 | ~500ms | 3-5 flights |
| Aggregator 2 | ~1.5s | 2-4 flights |
| Aggregator 3 | ~5s | 1-3 flights |

**Note**: The SSE stream completes after 3 seconds, so Aggregator 3 results may arrive after the stream is closed (but will still be sent if connection is open).

## Mock Data

The service generates realistic mock flight data including:
- Flight numbers (AA, DL, UA prefixes)
- Airlines (American, Delta, United)
- Departure/arrival times
- Prices ($200-$800 range)
- Aircraft types (Boeing 737, 787, Airbus A320, etc.)
- Duration and stops information

## Configuration

### Async Thread Pool
```java
// AsyncConfig.java
corePoolSize: 5
maxPoolSize: 10
queueCapacity: 100
```

### SSE Timeout
```java
// FlightController.java
SSE_TIMEOUT_MS: 300,000 (5 minutes)
AGGREGATOR_TIMEOUT_SECONDS: 3 seconds
```

## Security

The flight search endpoints are configured as public (no authentication required) for demo purposes. In production, you should:

1. Add authentication/authorization
2. Rate limiting for SSE connections
3. Input validation and sanitization
4. CORS configuration if needed

## Future Enhancements

- [ ] Add real flight API integration (e.g., Amadeus, Skyscanner)
- [ ] Implement caching for frequently searched routes
- [ ] Add filtering and sorting options
- [ ] WebSocket support for bidirectional communication
- [ ] Metrics and monitoring (connection count, response times)
- [ ] Circuit breaker pattern for aggregator failures

## Troubleshooting

### SSE connection closes immediately
- Check browser console for errors
- Verify endpoint URL is correct
- Check server logs for exceptions

### No events received
- Verify aggregator service is working (check logs)
- Ensure async configuration is loaded
- Check thread pool isn't exhausted

### Slow aggregator never completes
- This is expected behavior - timeout is 3 seconds
- Aggregator 3 takes 5 seconds, so it may timeout
- Results will still be sent if connection is open when data arrives

## References

- [Spring SSE Documentation](https://docs.spring.io/spring-framework/reference/web/sse.html)
- [MDN: Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Spring @Async Documentation](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)

---

**Happy Streaming! 🚀✈️**

