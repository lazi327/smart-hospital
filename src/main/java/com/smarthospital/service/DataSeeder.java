package com.smarthospital.service;

import com.smarthospital.entity.Department;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.Schedule;
import com.smarthospital.entity.User;
import com.smarthospital.repository.DepartmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.ScheduleRepository;
import com.smarthospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 首次启动初始化演示数据：账号、科室、医生、未来7天排班 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            log.info("初始化演示数据...");
            seed();
            log.info("演示数据初始化完成：演示账号 admin/admin123、patient/123456、doctor1~9/123456");
        }
        ensureSchedules();
    }

    private void seed() {
        userRepository.save(user("admin", "admin123", "系统管理员", "0755-88886666", User.Role.ADMIN));
        userRepository.save(user("patient", "123456", "张小明", "13800000001", User.Role.PATIENT));

        Department neike = dept("内科", "诊疗呼吸、消化、心血管、内分泌等内科常见疾病", "门诊楼 3 层", "🩺");
        Department waike = dept("外科", "普外、骨科、创伤等外科诊疗", "门诊楼 2 层", "🏥");
        Department erke = dept("儿科", "儿童常见病、多发病及儿童保健", "门诊楼 1 层东侧", "👶");
        Department pifuke = dept("皮肤科", "皮肤过敏、湿疹、痤疮等皮肤问题", "门诊楼 4 层", "🔬");
        Department yanke = dept("眼科", "屈光不正、干眼症、结膜炎等", "门诊楼 4 层西侧", "👀");
        Department kouqiang = dept("口腔科", "牙体牙髓、牙周、口腔修复与种植", "门诊楼 5 层", "🦷");
        Department zhongyi = dept("中医科", "中医调理、针灸、推拿", "门诊楼 6 层", "🌿");
        dept("急诊科", "24 小时急危重症救治，请直接前往急诊大楼", "急诊大楼 1 层", "🚑");

        doc("doctor1", "王建国", "主任医师", "高血压、冠心病、心律失常等心血管疾病", 50, neike);
        doc("doctor2", "李慧", "主治医师", "感冒、支气管炎、肺炎等呼吸系统疾病", 30, neike);
        doc("doctor3", "陈志强", "主任医师", "骨折、关节损伤、运动创伤", 60, waike);
        doc("doctor4", "赵敏", "副主任医师", "阑尾炎、疝气、胆道疾病等普外疾病", 40, waike);
        doc("doctor5", "孙丽", "主治医师", "儿童呼吸道感染、消化不良、生长发育评估", 30, erke);
        doc("doctor6", "周杰", "副主任医师", "过敏性皮炎、湿疹、荨麻疹", 35, pifuke);
        doc("doctor7", "吴芳", "主治医师", "近视、干眼症、结膜炎", 40, yanke);
        doc("doctor8", "郑浩", "主治医师", "龋齿、根管治疗、洗牙与口腔修复", 45, kouqiang);
        doc("doctor9", "冯时", "主任医师", "失眠调理、脾胃虚弱、颈肩腰腿痛针灸治疗", 50, zhongyi);
    }

    /** 每次启动滚动补齐未来 7 天号源，保证演示数据/排班不过期 */
    private void ensureSchedules() {
        LocalDate today = LocalDate.now();
        int created = 0;
        for (Doctor doctor : doctorRepository.findAll()) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.plusDays(i);
                created += ensureSchedule(doctor, date, Schedule.Period.AM, 20);
                created += ensureSchedule(doctor, date, Schedule.Period.PM, 15);
            }
        }
        if (created > 0) {
            log.info("补齐未来 7 天号源 {} 条", created);
        }
    }

    private int ensureSchedule(Doctor doctor, LocalDate date, Schedule.Period period, int capacity) {
        if (scheduleRepository.findByDoctorIdAndDateAndPeriod(doctor.getId(), date, period).isPresent()) {
            return 0;
        }
        scheduleRepository.save(schedule(doctor, date, period, capacity));
        return 1;
    }

    private User user(String username, String password, String name, String phone, User.Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setName(name);
        u.setPhone(phone);
        u.setRole(role);
        return u;
    }

    private Department dept(String name, String description, String location, String icon) {
        Department d = new Department();
        d.setName(name);
        d.setDescription(description);
        d.setLocation(location);
        d.setIcon(icon);
        return departmentRepository.save(d);
    }

    private void doc(String username, String name, String title, String specialty, int fee, Department dept) {
        User user = user(username, "123456", name, null, User.Role.DOCTOR);
        user = userRepository.save(user);
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(dept);
        doctor.setTitle(title);
        doctor.setSpecialty(specialty);
        doctor.setIntro("从事临床工作多年，经验丰富。");
        doctor.setFee(BigDecimal.valueOf(fee));
        doctorRepository.save(doctor);
    }

    private Schedule schedule(Doctor doctor, LocalDate date, Schedule.Period period, int capacity) {
        Schedule s = new Schedule();
        s.setDoctor(doctor);
        s.setDate(date);
        s.setPeriod(period);
        s.setCapacity(capacity);
        s.setBooked(0);
        return s;
    }
}
