package com.example.appointmentservice;

import com.example.appointmentservice.client.PatientClient;
import com.example.appointmentservice.dto.PatientDTO;
import com.example.appointmentservice.exception.PatientServiceUnavailableException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class PatientRetryTest {

    @Autowired
    private PatientClient patientClient;

    @Autowired
    private RetryRegistry retryRegistry;

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        Retry retry = retryRegistry.retry("patientRetry");
        retry.getEventPublisher();
    }

    @Test
    void testRetrySuccessOnSecondAttempt() {
        PatientDTO mockPatient = new PatientDTO();
        mockPatient.setId(1L);
        mockPatient.setName("Nguyen Van A");

        // Lần 1 ném ResourceAccessException, Lần 2 trả về mockPatient thành công
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(PatientDTO.class)))
                .thenThrow(new ResourceAccessException("Transient network error"))
                .thenReturn(mockPatient);

        PatientDTO result = patientClient.getPatientById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Nguyen Van A", result.getName());
        Mockito.verify(restTemplate, Mockito.times(2)).getForObject(Mockito.anyString(), Mockito.eq(PatientDTO.class));
    }

    @Test
    void testRetryFailsAfterMaxAttemptsAndTriggersFallback() {
        // Cả 3 lần đều ném ResourceAccessException
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(PatientDTO.class)))
                .thenThrow(new ResourceAccessException("Transient network error"));

        PatientServiceUnavailableException exception = Assertions.assertThrows(
                PatientServiceUnavailableException.class,
                () -> patientClient.getPatientById(1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("không thể kết nối tới dịch vụ bệnh nhân"));
        Mockito.verify(restTemplate, Mockito.times(3)).getForObject(Mockito.anyString(), Mockito.eq(PatientDTO.class));
    }
}
