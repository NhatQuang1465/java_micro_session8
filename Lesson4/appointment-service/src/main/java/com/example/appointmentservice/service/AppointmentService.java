package com.example.appointmentservice.service;

import com.example.appointmentservice.client.DoctorClient;
import com.example.appointmentservice.client.PatientClient;
import com.example.appointmentservice.dto.DoctorDTO;
import com.example.appointmentservice.dto.PatientDTO;
import com.example.appointmentservice.entity.Appointment;
import com.example.appointmentservice.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorClient doctorClient;
    private final PatientClient patientClient;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               DoctorClient doctorClient,
                               PatientClient patientClient) {
        this.appointmentRepository = appointmentRepository;
        this.doctorClient = doctorClient;
        this.patientClient = patientClient;
    }

    public Appointment createAppointment(Appointment appointment) {
        // 1. Kiểm tra sự tồn tại của Patient qua PatientClient (tích hợp Resilience4j Retry: patientRetry)
        PatientDTO patient = patientClient.getPatientById(appointment.getPatientId());
        if (patient == null) {
            throw new IllegalArgumentException("Không tìm thấy Bệnh nhân có ID: " + appointment.getPatientId());
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
