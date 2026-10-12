package com.smarthospital.service;

import com.smarthospital.ai.AiAssistantService;
import com.smarthospital.common.BizException;
import com.smarthospital.dto.Dtos.RecordReq;
import com.smarthospital.dto.Dtos.RecordVO;
import com.smarthospital.entity.Appointment;
import com.smarthospital.entity.Doctor;
import com.smarthospital.entity.MedicalRecord;
import com.smarthospital.entity.User;
import com.smarthospital.repository.AppointmentRepository;
import com.smarthospital.repository.DoctorRepository;
import com.smarthospital.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final AiAssistantService aiAssistantService;

    /** 医生填写病历，并调用 AI 生成患者可读摘要 */
    @Transactional
    public RecordVO create(Long doctorUserId, RecordReq req) {
        Appointment appointment = appointmentRepository.findById(req.appointmentId())
                .orElseThrow(() -> new BizException("挂号单不存在"));
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new BizException("医生信息不存在"));
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new BizException(403, "只能为本人接诊的挂号填写病历");
        }
        if (medicalRecordRepository.existsByAppointmentId(appointment.getId())) {
            throw new BizException("该挂号已填写过病历");
        }
        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setPatient(appointment.getPatient());
        record.setDoctor(doctor);
        record.setChiefComplaint(req.chiefComplaint());
        record.setDiagnosis(req.diagnosis());
        record.setAdvice(req.advice());
        record.setAiSummary(aiAssistantService.summarizeRecord(req.chiefComplaint(), req.diagnosis(), req.advice()));
        appointment.setStatus(Appointment.Status.FINISHED);
        medicalRecordRepository.save(record);
        return toVO(record);
    }

    @Transactional(readOnly = true)
    public List<RecordVO> mine(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toVO).toList();
    }

    /** 按挂号单查询病历（校验数据归属权限） */
    @Transactional(readOnly = true)
    public RecordVO findByAppointment(Long appointmentId, User user) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId).orElse(null);
        if (record == null) {
            return null;
        }
        if (user.getRole() == User.Role.PATIENT) {
            if (!record.getPatient().getId().equals(user.getId())) {
                throw new BizException(403, "无权查看该病历");
            }
        } else if (user.getRole() == User.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BizException("医生信息不存在"));
            if (!record.getDoctor().getId().equals(doctor.getId())) {
                throw new BizException(403, "无权查看该病历");
            }
        }
        return toVO(record);
    }

    public RecordVO toVO(MedicalRecord r) {
        return new RecordVO(
                r.getId(),
                r.getAppointment().getId(),
                r.getPatient().getName(),
                r.getDoctor().getUser().getName(),
                r.getDoctor().getDepartment().getName(),
                r.getAppointment().getDate(),
                r.getAppointment().getPeriod().name(),
                r.getChiefComplaint(),
                r.getDiagnosis(),
                r.getAdvice(),
                r.getAiSummary(),
                r.getCreatedAt()
        );
    }
}
