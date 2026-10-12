package com.smarthospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "t_schedule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "date", "period"}))
public class Schedule {

    public enum Period { AM, PM }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Period period;

    /** 号源总量 */
    private int capacity;

    /** 已约数量 */
    private int booked;
}
