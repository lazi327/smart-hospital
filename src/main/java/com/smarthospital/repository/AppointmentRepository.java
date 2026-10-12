package com.smarthospital.repository;

import com.smarthospital.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Appointment> findByDoctorIdAndDateOrderByPeriodAscIdAsc(Long doctorId, LocalDate date);

    List<Appointment> findByDateBetween(LocalDate start, LocalDate end);

    long countByDateAndStatusNot(LocalDate date, Appointment.Status status);

    boolean existsByDoctorId(Long doctorId);
}
