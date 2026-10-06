package com.example.appointmentservice.client;

import com.example.appointmentservice.dto.PatientDTO;
import com.example.appointmentservice.exception.PatientServiceUnavailableException;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class PatientClient {

    private static final Logger log = LoggerFactory.getLogger(PatientClient.class);
    private final RestTemplate restTemplate;

    public PatientClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "patientRetry", fallbackMethod = "getPatientFallback")
    public PatientDTO getPatientById(Long patientId) {
        log.info("[PatientClient] Calling patient-service for patientId: {}", patientId);
        try {
            return restTemplate.getForObject(
                    "http://patient-service/api/v1/patients/" + patientId,
                    PatientDTO.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Bệnh nhân không tồn tại với ID: " + patientId);
        }
    }

    public PatientDTO getPatientFallback(Long patientId, Throwable t) {
        log.error("[PatientClient Fallback] Failed to fetch patient with ID {} after retries. Cause: {}", patientId, t.getMessage());
        throw new PatientServiceUnavailableException("Hệ thống gián đoạn, không thể kết nối tới dịch vụ bệnh nhân. Vui lòng thử lại sau.", t);
    }
}
