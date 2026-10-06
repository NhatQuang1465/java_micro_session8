package com.example.appointmentservice;

import com.example.appointmentservice.client.DoctorClient;
import com.example.appointmentservice.dto.ApiResponseError;
import com.example.appointmentservice.exception.DoctorServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class DoctorCircuitBreakerTest {

    @Autowired
    private DoctorClient doctorClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("doctorServiceCB");
        cb.reset();
    }

    @Test
    void testCircuitBreakerTransitionsFromClosedToOpenAfter5Failures() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("doctorServiceCB");

        // Trạng thái ban đầu: CLOSED
        Assertions.assertEquals(CircuitBreaker.State.CLOSED, cb.getState());

        // Giả lập RestTemplate ném ra ngoại lệ khi Doctor-Service bị sập
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenThrow(new RestClientException("Doctor-Service is Down"));

        // Gọi 5 lần thất bại để kích hoạt Circuit Breaker (minimum-number-of-calls=5, failure-rate-threshold=50%)
        for (int i = 1; i <= 5; i++) {
            final int callIndex = i;
            Assertions.assertThrows(RuntimeException.class, () -> {
                doctorClient.getDoctorById(1L);
            });
            System.out.println("Call #" + callIndex + " failed. Current CB State: " + cb.getState());
        }

        // Sau 5 lần thất bại, trạng thái phải chuyển sang OPEN
        Assertions.assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        System.out.println("Circuit Breaker has successfully transitioned to OPEN!");
    }

    @Test
    void testGetDoctorFallbackReturnsApiResponseError() {
        Exception ex = new RuntimeException("Doctor-Service is Down");
        ApiResponseError fallbackResponse = doctorClient.getDoctorFallback(ex);
        Assertions.assertNotNull(fallbackResponse);
        Assertions.assertEquals(503, fallbackResponse.getStatus());
        Assertions.assertEquals("Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây", fallbackResponse.getMessage());
    }

    @Test
    void testDoctorServiceUnavailableExceptionMessage() {
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenThrow(new RestClientException("Doctor-Service is Down"));

        DoctorServiceUnavailableException thrown = Assertions.assertThrows(
                DoctorServiceUnavailableException.class,
                () -> doctorClient.getDoctorById(1L)
        );

        Assertions.assertEquals("Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây", thrown.getMessage());
    }
}
