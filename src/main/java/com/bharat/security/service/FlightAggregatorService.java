package com.bharat.security.service;

import com.bharat.security.dto.Flight;
import com.bharat.security.dto.FlightSearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service that simulates fetching flight data from multiple aggregators.
 * Aggregator 3 is intentionally slow to demonstrate non-blocking SSE behavior.
 */
@Service
public class FlightAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(FlightAggregatorService.class);
    private final Random random = new Random();

    /**
     * Fetches flights from Aggregator 1 (Fast - 500ms response time).
     */
    @Async
    public CompletableFuture<List<Flight>> fetchFromAggregator1(FlightSearchRequest request) {
        logger.info("Fetching flights from Aggregator 1 for {}-{}", request.getOrigin(), request.getDestination());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate network delay (500ms)
                Thread.sleep(500);

                List<Flight> flights = new ArrayList<>();
                LocalDateTime baseTime = request.getDepartureDate().atTime(8, 0);

                // Generate 3-5 flights
                int flightCount = 3 + random.nextInt(3);
                for (int i = 0; i < flightCount; i++) {
                    flights.add(createMockFlight(
                        "AA" + (1000 + i),
                        "American Airlines",
                        request.getOrigin(),
                        request.getDestination(),
                        baseTime.plusHours(i * 2),
                        baseTime.plusHours(i * 2 + 3),
                        BigDecimal.valueOf(250 + random.nextInt(300)),
                        "Aggregator1"
                    ));
                }

                logger.info("Aggregator 1 returned {} flights", flights.size());
                return flights;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Aggregator 1 interrupted", e);
                return new ArrayList<>();
            } catch (Exception e) {
                logger.error("Error fetching from Aggregator 1", e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Fetches flights from Aggregator 2 (Medium - 1.5s response time).
     */
    @Async
    public CompletableFuture<List<Flight>> fetchFromAggregator2(FlightSearchRequest request) {
        logger.info("Fetching flights from Aggregator 2 for {}-{}", request.getOrigin(), request.getDestination());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate network delay (1.5s)
                Thread.sleep(1500);

                List<Flight> flights = new ArrayList<>();
                LocalDateTime baseTime = request.getDepartureDate().atTime(10, 0);

                // Generate 2-4 flights
                int flightCount = 2 + random.nextInt(3);
                for (int i = 0; i < flightCount; i++) {
                    flights.add(createMockFlight(
                        "DL" + (2000 + i),
                        "Delta Airlines",
                        request.getOrigin(),
                        request.getDestination(),
                        baseTime.plusHours(i * 3),
                        baseTime.plusHours(i * 3 + 4),
                        BigDecimal.valueOf(300 + random.nextInt(400)),
                        "Aggregator2"
                    ));
                }

                logger.info("Aggregator 2 returned {} flights", flights.size());
                return flights;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Aggregator 2 interrupted", e);
                return new ArrayList<>();
            } catch (Exception e) {
                logger.error("Error fetching from Aggregator 2", e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Fetches flights from Aggregator 3 (Slow - 5s response time).
     * This is intentionally slow to demonstrate non-blocking behavior.
     */
    @Async
    public CompletableFuture<List<Flight>> fetchFromAggregator3(FlightSearchRequest request) {
        logger.info("Fetching flights from Aggregator 3 (SLOW) for {}-{}", request.getOrigin(), request.getDestination());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate slow network delay (5 seconds)
                Thread.sleep(5000);

                List<Flight> flights = new ArrayList<>();
                LocalDateTime baseTime = request.getDepartureDate().atTime(14, 0);

                // Generate 1-3 flights
                int flightCount = 1 + random.nextInt(3);
                for (int i = 0; i < flightCount; i++) {
                    flights.add(createMockFlight(
                        "UA" + (3000 + i),
                        "United Airlines",
                        request.getOrigin(),
                        request.getDestination(),
                        baseTime.plusHours(i * 4),
                        baseTime.plusHours(i * 4 + 5),
                        BigDecimal.valueOf(200 + random.nextInt(500)),
                        "Aggregator3"
                    ));
                }

                logger.info("Aggregator 3 (SLOW) returned {} flights", flights.size());
                return flights;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Aggregator 3 interrupted", e);
                return new ArrayList<>();
            } catch (Exception e) {
                logger.error("Error fetching from Aggregator 3", e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Creates a mock flight object with realistic data.
     */
    private Flight createMockFlight(String flightNumber, String airline, String origin,
                                     String destination, LocalDateTime departureTime,
                                     LocalDateTime arrivalTime, BigDecimal price, String aggregator) {
        Flight flight = new Flight(flightNumber, airline, origin, destination,
                departureTime, arrivalTime, price, aggregator);

        // Add additional details
        flight.setAircraftType(getRandomAircraftType());
        flight.setDurationMinutes((int) java.time.Duration.between(departureTime, arrivalTime).toMinutes());
        flight.setStops(random.nextInt(2)); // 0 or 1 stops

        return flight;
    }

    /**
     * Returns a random aircraft type.
     */
    private String getRandomAircraftType() {
        String[] aircraftTypes = {"Boeing 737", "Boeing 787", "Airbus A320", "Airbus A350", "Boeing 777"};
        return aircraftTypes[random.nextInt(aircraftTypes.length)];
    }

    /**
     * Fetches flights from all aggregators with a timeout.
     * Returns results as they become available, without waiting for slow aggregators.
     */
    public CompletableFuture<List<Flight>> fetchFromAllAggregators(FlightSearchRequest request, long timeoutSeconds) {
        logger.info("Starting parallel fetch from all aggregators with {}s timeout", timeoutSeconds);

        CompletableFuture<List<Flight>> aggregator1 = fetchFromAggregator1(request);
        CompletableFuture<List<Flight>> aggregator2 = fetchFromAggregator2(request);
        CompletableFuture<List<Flight>> aggregator3 = fetchFromAggregator3(request);

        // Combine all results, but don't wait for all - use timeout
        return CompletableFuture.allOf(
                aggregator1.orTimeout(timeoutSeconds, TimeUnit.SECONDS),
                aggregator2.orTimeout(timeoutSeconds, TimeUnit.SECONDS),
                aggregator3.orTimeout(timeoutSeconds, TimeUnit.SECONDS)
        ).thenApply(v -> {
            List<Flight> allFlights = new ArrayList<>();

            // Collect results from each aggregator (may be empty if timed out)
            aggregator1.whenComplete((flights, ex) -> {
                if (ex == null && flights != null) {
                    allFlights.addAll(flights);
                }
            });

            aggregator2.whenComplete((flights, ex) -> {
                if (ex == null && flights != null) {
                    allFlights.addAll(flights);
                }
            });

            aggregator3.whenComplete((flights, ex) -> {
                if (ex == null && flights != null) {
                    allFlights.addAll(flights);
                }
            });

            return allFlights;
        }).exceptionally(ex -> {
            logger.warn("Some aggregators timed out or failed, returning partial results", ex);
            return new ArrayList<>();
        });
    }
}

