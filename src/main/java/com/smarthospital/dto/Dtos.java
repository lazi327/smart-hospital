package com.smarthospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 统一 DTO 定义 */
public final class Dtos {

    private Dtos() {
    }

    // ---------- 认证 ----------
    public record RegisterReq(@NotBlank(message = "用户名不能为空") String username,
                              @NotBlank(message = "密码不能为空") String password,
                              @NotBlank(message = "姓名不能为空") String name,
                              String phone, String gender, Integer age) {
    }

    public record LoginReq(@NotBlank(message = "用户名不能为空") String username,
                           @NotBlank(message = "密码不能为空") String password) {
    }

    public record UserVO(Long id, String username, String name, String phone, String role,
                          String gender, Integer age, String idCard, String address) {
    }

    public record ProfileUpdateReq(String name, String phone, String gender, Integer age,
                                   String idCard, String address) {
    }

    public record LoginVO(String token, UserVO user) {
    }

    // ---------- 医生 / 排班 ----------
    public record DoctorVO(Long id, String name, Long departmentId, String departmentName,
                           String title, String specialty, String intro, BigDecimal fee) {
    }

    public record ScheduleVO(Long id, LocalDate date, String period, int capacity, int booked, int available) {
    }

    public record DoctorDetailVO(DoctorVO doctor, List<ScheduleVO> schedules) {
    }

    // ---------- 挂号 ----------
    public record BookReq(@NotNull(message = "排班不能为空") Long scheduleId) {
    }

    public record AppointmentVO(Long id, String patientName, String doctorName, String title,
                                String departmentName, LocalDate date, String period, int queueNo,
                                String status, BigDecimal fee, LocalDateTime createdAt) {
    }

    // ---------- 病历 ----------
    public record RecordReq(@NotNull(message = "挂号单不能为空") Long appointmentId,
                            @NotBlank(message = "主诉不能为空") String chiefComplaint,
                            @NotBlank(message = "诊断不能为空") String diagnosis,
                            String advice) {
    }

    public record RecordVO(Long id, Long appointmentId, String patientName, String doctorName,
                           String departmentName, LocalDate date, String period,
                           String chiefComplaint, String diagnosis, String advice,
                           String aiSummary, LocalDateTime createdAt) {
    }

    // ---------- AI ----------
    public record ChatReq(@NotBlank(message = "会话不能为空") String sessionId,
                          @NotBlank(message = "消息不能为空") String message) {
    }

    public record SummaryReq(String chiefComplaint, String diagnosis, String advice) {
    }

    public record DeptSuggest(Long id, String name) {
    }

    public record ChatResult(String reply, List<DeptSuggest> departments) {
    }

    // ---------- 看板 ----------
    public record DeptStat(String name, long count) {
    }

    public record DayStat(String date, long count) {
    }

    public record DashboardVO(long todayAppointments, long totalPatients, long totalDoctors,
                              List<DeptStat> deptDistribution, List<DayStat> weeklyTrend) {
    }

    // ---------- 管理 ----------
    public record DeptReq(Long id, @NotBlank(message = "科室名不能为空") String name,
                          String description, String location, String icon) {
    }

    public record DoctorReq(Long id, String username, String password,
                            @NotBlank(message = "医生姓名不能为空") String name,
                            @NotNull(message = "科室不能为空") Long departmentId,
                            @NotBlank(message = "职称不能为空") String title,
                            String specialty, String intro, BigDecimal fee) {
    }

    public record ScheduleReq(@NotNull(message = "医生不能为空") Long doctorId,
                              @NotNull(message = "日期不能为空") LocalDate date,
                              @NotNull(message = "时段不能为空") String period,
                              Integer capacity) {
    }
}
