package com.example.doctorservice.config;

import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(DoctorRepository doctorRepository) {
        return args -> {
            if (doctorRepository.count() == 0) {
                Doctor d1 = Doctor.builder()
                        .name("Nguyen Van A")
                        .specialization("Noi khoa")
                        .experienceYears(10)
                        .email("nguyenvana@hospital.com")
                        .status(true)
                        .build();

                Doctor d2 = Doctor.builder()
                        .name("Tran Thi B")
                        .specialization("Ngoai khoa")
                        .experienceYears(8)
                        .email("tranthib@hospital.com")
                        .status(true)
                        .build();

                Doctor d3 = Doctor.builder()
                        .name("Le Van C")
                        .specialization("Nhi khoa")
                        .experienceYears(5)
                        .email("levanc@hospital.com")
                        .status(false)
                        .build();

                doctorRepository.saveAll(List.of(d1, d2, d3));
            }
        };
    }
}
