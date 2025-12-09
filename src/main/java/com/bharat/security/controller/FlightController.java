package com.bharat.security.controller;

import com.bharat.security.dto.Flight;
import com.bharat.security.dto.FlightSearchRequest;
import com.bharat.security.service.FlightAggregatorService;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for flight search with Server-Sent Events (SSE) support.
 * Demonstrates non-blocking flight data aggregation from multiple sources.
 */
@RestController
@RequestMapping("/api/flights")
@EnableAsync
public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);
    private static final long SSE_TIMEOUT_MS = 300_000; // 5 minutes
    private static final long AGGREGATOR_TIMEOUT_SECONDS = 3; // Don't wait more than 3 seconds for slow aggregators

    private final FlightAggregatorService flightAggregatorService;
    private final ObjectMapper objectMapper;

    public FlightController(FlightAggregatorService flightAggregatorService, ObjectMapper objectMapper) {
        this.flightAggregatorService = flightAggregatorService;
        this.objectMapper = objectMapper;
        logger.info("FlightController initialized with SSE support");
    }

    /**
     * Server-Sent Events endpoint for streaming flight search results.
     * Results are sent as they arrive from each aggregator, without waiting for slow ones.
     *
     * @param request Flight search request with origin, destination, and dates
     * @return SseEmitter for streaming flight data
     */
    @GetMapping(value = "/search/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter searchFlightsStream(@Valid FlightSearchRequest request) {
        logger.info("Starting SSE flight search: {} -> {} on {}",
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        AtomicInteger totalFlightsReceived = new AtomicInteger(0);
        AtomicInteger aggregatorsCompleted = new AtomicInteger(0);

        // Send initial connection event
        sendEvent(emitter, "connected", "Flight search started. Fetching from 3 aggregators...");

        // Fetch from all aggregators in parallel
        CompletableFuture<List<Flight>> aggregator1Future = flightAggregatorService.fetchFromAggregator1(request);
        CompletableFuture<List<Flight>> aggregator2Future = flightAggregatorService.fetchFromAggregator2(request);
        CompletableFuture<List<Flight>> aggregator3Future = flightAggregatorService.fetchFromAggregator3(request);

        // Handle Aggregator 1 (Fast - ~500ms)
        aggregator1Future.whenComplete((flights, exception) -> {
            aggregatorsCompleted.incrementAndGet();
            if (exception == null && flights != null && !flights.isEmpty()) {
                int count = flights.size();
                totalFlightsReceived.addAndGet(count);
                logger.info("Aggregator 1 completed: {} flights", count);
                sendEvent(emitter, "aggregator1", createFlightsJson(flights));
                sendEvent(emitter, "status", String.format("Aggregator 1: %d flights received", count));
            } else {
                logger.warn("Aggregator 1 failed or returned no results", exception);
                sendEvent(emitter, "error", "Aggregator 1: Failed to fetch flights");
            }
            checkAndComplete(emitter, aggregatorsCompleted, totalFlightsReceived);
        });

        // Handle Aggregator 2 (Medium - ~1.5s)
        aggregator2Future.whenComplete((flights, exception) -> {
            aggregatorsCompleted.incrementAndGet();
            if (exception == null && flights != null && !flights.isEmpty()) {
                int count = flights.size();
                totalFlightsReceived.addAndGet(count);
                logger.info("Aggregator 2 completed: {} flights", count);
                sendEvent(emitter, "aggregator2", createFlightsJson(flights));
                sendEvent(emitter, "status", String.format("Aggregator 2: %d flights received", count));
            } else {
                logger.warn("Aggregator 2 failed or returned no results", exception);
                sendEvent(emitter, "error", "Aggregator 2: Failed to fetch flights");
            }
            checkAndComplete(emitter, aggregatorsCompleted, totalFlightsReceived);
        });

        // Handle Aggregator 3 (Slow - ~5s) - We don't wait for this one!
        aggregator3Future.whenComplete((flights, exception) -> {
            aggregatorsCompleted.incrementAndGet();
            if (exception == null && flights != null && !flights.isEmpty()) {
                int count = flights.size();
                totalFlightsReceived.addAndGet(count);
                logger.info("Aggregator 3 (SLOW) completed: {} flights", count);
                sendEvent(emitter, "aggregator3", createFlightsJson(flights));
                sendEvent(emitter, "status", String.format("Aggregator 3 (late): %d flights received", count));
            } else {
                logger.warn("Aggregator 3 failed, timed out, or returned no results", exception);
                sendEvent(emitter, "status", "Aggregator 3: Timed out or failed (this is expected - it's slow!)");
            }
            checkAndComplete(emitter, aggregatorsCompleted, totalFlightsReceived);
        });

        // Set timeout for slow aggregator - complete after 3 seconds even if aggregator 3 hasn't responded
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(AGGREGATOR_TIMEOUT_SECONDS * 1000);
                if (aggregatorsCompleted.get() < 3) {
                    logger.info("Timeout reached. Completing SSE stream with {} aggregators completed",
                            aggregatorsCompleted.get());
                    sendEvent(emitter, "status",
                            String.format("Search completed. Received flights from %d aggregators. " +
                                    "Slow aggregator may still be processing.", aggregatorsCompleted.get()));
                    sendEvent(emitter, "complete", String.format("Total flights: %d", totalFlightsReceived.get()));
                    emitter.complete();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Timeout thread interrupted", e);
            }
        });

        // Handle client disconnection
        emitter.onCompletion(() -> {
            logger.info("SSE connection completed. Total flights sent: {}", totalFlightsReceived.get());
        });

        emitter.onTimeout(() -> {
            logger.warn("SSE connection timed out");
            emitter.complete();
        });

        emitter.onError((ex) -> {
            logger.error("SSE connection error", ex);
            emitter.completeWithError(ex);
        });

        return emitter;
    }

    /**
     * Traditional REST endpoint for flight search (for comparison).
     * This endpoint waits for all aggregators, including the slow one.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(@Valid FlightSearchRequest request) {
        logger.info("Traditional REST search: {} -> {} on {}",
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        try {
            List<Flight> allFlights = flightAggregatorService.fetchFromAllAggregators(request, AGGREGATOR_TIMEOUT_SECONDS)
                    .get(); // This will wait, but with timeout

            logger.info("Traditional search completed: {} flights", allFlights.size());
            return ResponseEntity.ok(allFlights);
        } catch (Exception e) {
            logger.error("Error in traditional search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Sends an SSE event to the client.
     */
    private void sendEvent(SseEmitter emitter, String eventName, String data) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(eventName)
                    .data(data);
            emitter.send(event);
        } catch (IOException e) {
            logger.error("Error sending SSE event: {}", eventName, e);
            emitter.completeWithError(e);
        }
    }

    /**
     * Converts flight list to JSON string.
     */
    private String createFlightsJson(List<Flight> flights) {
        try {
            return objectMapper.writeValueAsString(flights);
        } catch (Exception e) {
            logger.error("Error serializing flights to JSON", e);
            return "[]";
        }
    }

    /**
     * Checks if all aggregators have completed and sends completion event.
     */
    private void checkAndComplete(SseEmitter emitter, AtomicInteger aggregatorsCompleted, AtomicInteger totalFlights) {
        // Don't auto-complete - let the timeout handle it
        // This allows slow aggregator to send data even after timeout
    }
}

