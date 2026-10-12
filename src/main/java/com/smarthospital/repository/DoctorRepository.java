package com.smarthospital.repository;

import com.smarthospital.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartmentIdOrderByIdAsc(Long departmentId);

    List<Doctor> findAllByOrderByIdAsc();

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByDepartmentId(Long departmentId);
}
