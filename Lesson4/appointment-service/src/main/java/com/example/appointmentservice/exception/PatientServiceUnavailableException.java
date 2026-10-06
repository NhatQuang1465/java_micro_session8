package com.example.appointmentservice.exception;

public class PatientServiceUnavailableException extends RuntimeException {

    public PatientServiceUnavailableException(String message) {
        super(message);
    }

    public PatientServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
