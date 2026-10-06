package com.example.appointmentservice.service;

import com.example.appointmentservice.client.DoctorClient;
import com.example.appointmentservice.dto.DoctorDTO;
import com.example.appointmentservice.dto.PatientDTO;
import com.example.appointmentservice.entity.Appointment;
import com.example.appointmentservice.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;
    private final DoctorClient doctorClient;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              RestTemplate restTemplate,
                              DoctorClient doctorClient) {
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
        this.doctorClient = doctorClient;
    }

    public Appointment createAppointment(Appointment appointment) {
        // 1. Kiểm tra sự tồn tại của Patient qua Eureka Service: patient-service
        try {
            PatientDTO patient = restTemplate.getForObject(
                    "http://patient-service/api/v1/patients/" + appointment.getPatientId(),
                    PatientDTO.class
            );
            if (patient == null) {
                throw new IllegalArgumentException("Không tìm thấy Bệnh nhân có ID: " + appointment.getPatientId());
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Bệnh nhân không tồn tại với ID: " + appointment.getPatientId());
        } catch (RestClientException e) {
            throw new IllegalArgumentException("Lỗi khi kiểm tra Bệnh nhân từ patient-service: " + e.getMessage());
        }

        // 2. Kiểm tra sự tồn tại của Doctor qua DoctorClient (tích hợp Resilience4j CircuitBreaker)
        DoctorDTO doctor = doctorClient.getDoctorById(appointment.getDoctorId());
        if (doctor == null) {
            throw new IllegalArgumentException("Không tìm thấy Bác sĩ có ID: " + appointment.getDoctorId());
        }

        // 3. Thiết lập giá trị mặc định nếu chưa có
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            appointment.setStatus("PENDING");
        }
        if (appointment.getAppointmentDate() == null) {
            appointment.getAppointmentDate();
            appointment.setAppointmentDate(LocalDateTime.now());
        }

        // 4. Lưu vào Database
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }
}
