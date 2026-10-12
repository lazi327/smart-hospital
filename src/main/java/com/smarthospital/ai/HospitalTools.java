package com.smarthospital.ai;

import com.smarthospital.entity.Department;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.Schedule;
import com.smarthospital.repository.DepartmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** AI 导诊工具集：供大模型通过 Function Calling 实时查询医院数据 */
@Component
@RequiredArgsConstructor
public class HospitalTools {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    @Tool(description = "查询医院所有科室的名称、位置和简介")
    public String listDepartments() {
        List<Department> list = departmentRepository.findAllByOrderByIdAsc();
        if (list.isEmpty()) {
            return "暂无科室信息";
        }
        StringBuilder sb = new StringBuilder("医院科室列表：\n");
        for (Department d : list) {
            sb.append("- ").append(d.getName())
                    .append("（位置：").append(d.getLocation() == null ? "不详" : d.getLocation()).append("）");
            if (d.getDescription() != null) {
                sb.append("：").append(d.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Tool(description = "根据科室名称查询该科室的医生列表（含职称、专长、挂号费）以及未来3天的排班余号情况")
    public String findDoctors(@ToolParam(description = "科室名称，例如：内科") String departmentName) {
        Department dept = departmentRepository.findAll().stream()
                .filter(d -> d.getName().contains(departmentName) || departmentName.contains(d.getName()))
                .findFirst()
                .orElse(null);
        if (dept == null) {
            return "未找到名称包含「" + departmentName + "」的科室";
        }
        List<Doctor> doctors = doctorRepository.findByDepartmentIdOrderByIdAsc(dept.getId());
        if (doctors.isEmpty()) {
            return dept.getName() + "暂无医生排班";
        }
        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder(dept.getName()).append("医生排班：\n");
        for (Doctor doc : doctors) {
            sb.append("- ").append(doc.getUser().getName())
                    .append("，").append(doc.getTitle())
                    .append("，专长：").append(doc.getSpecialty() == null ? "综合" : doc.getSpecialty())
                    .append("，挂号费：").append(doc.getFee()).append("元\n");
            List<Schedule> schedules = scheduleRepository.findByDoctorIdAndDateBetweenOrderByDateAscPeriodAsc(
                    doc.getId(), today, today.plusDays(2));
            for (Schedule s : schedules) {
                int remain = Math.max(0, s.getCapacity() - s.getBooked());
                sb.append("  · ").append(s.getDate())
                        .append(s.getPeriod() == Schedule.Period.AM ? "上午" : "下午")
                        .append(" 余号 ").append(remain).append("\n");
            }
        }
        return sb.toString();
    }
}
