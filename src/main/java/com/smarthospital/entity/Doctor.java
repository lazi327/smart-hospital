package com.smarthospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "t_doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联登录账号 */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    /** 职称：主任医师 / 副主任医师 / 主治医师 ... */
    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 200)
    private String specialty;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;
}
