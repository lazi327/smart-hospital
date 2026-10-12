package com.smarthospital.repository;

import com.smarthospital.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctorIdAndDateBetweenOrderByDateAscPeriodAsc(Long doctorId, LocalDate start, LocalDate end);

    List<Schedule> findByDoctorIdAndDateBetween(Long doctorId, LocalDate start, LocalDate end);

    Optional<Schedule> findByDoctorIdAndDateAndPeriod(Long doctorId, LocalDate date, Schedule.Period period);

    boolean existsByDoctorId(Long doctorId);

    /** 查询数据库中最新的已约数量（用于生成排队号） */
    @Query("select s.booked from Schedule s where s.id = :id")
    Integer getBookedById(@Param("id") Long id);

    /** 原子占号：仅当还有余号时 booked+1，返回受影响行数 */
    @Modifying
    @Query("update Schedule s set s.booked = s.booked + 1 where s.id = :id and s.booked < s.capacity")
    int tryBook(@Param("id") Long id);

    /** 释放号源 */
    @Modifying
    @Query("update Schedule s set s.booked = case when s.booked > 0 then s.booked - 1 else 0 end where s.id = :id")
    void release(@Param("id") Long id);

    @Modifying
    void deleteByDoctorId(Long doctorId);
}
