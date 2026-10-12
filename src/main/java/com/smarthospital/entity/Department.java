package com.smarthospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "t_department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 门诊位置，如"门诊楼 3 层" */
    @Column(length = 100)
    private String location;

    /** 展示图标（emoji） */
    @Column(length = 10)
    private String icon;
}
