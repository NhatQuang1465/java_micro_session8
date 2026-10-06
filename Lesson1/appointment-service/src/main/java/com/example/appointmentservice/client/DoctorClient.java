package com.example.appointmentservice.client;

import com.example.appointmentservice.dto.DoctorDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class DoctorClient {

    private static final Logger log = LoggerFactory.getLogger(DoctorClient.class);
    private final RestTemplate restTemplate;

    public DoctorClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "getDoctorFallback")
    public DoctorDTO getDoctorById(Long doctorId) {
        log.info("[DoctorClient] Calling doctor-service for doctorId: {}", doctorId);
        return restTemplate.getForObject(
                "http://doctor-service/api/v1/doctors/" + doctorId,
                DoctorDTO.class
        );
    }

    public DoctorDTO getDoctorFallback(Long doctorId, Throwable t) {
        log.error("[DoctorClient Fallback] Failed to fetch doctor with ID {}. Cause: {}", doctorId, t.getMessage());
        if (t instanceof HttpClientErrorException.NotFound) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại với ID: " + doctorId);
        }
        throw new RuntimeException("Doctor-Service không khả dụng (Circuit Breaker OPEN / Down). Chi tiết: " + t.getMessage(), t);
    }
}
