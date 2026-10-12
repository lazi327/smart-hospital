package com.smarthospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "t_medical_record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    /** 主诉 */
    @Column(columnDefinition = "TEXT")
    private String chiefComplaint;

    /** 诊断 */
    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    /** 处理意见/医嘱 */
    @Column(columnDefinition = "TEXT")
    private String advice;

    /** AI 生成的患者可读就诊摘要 */
    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
