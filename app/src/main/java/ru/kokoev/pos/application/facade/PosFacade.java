package ru.kokoev.pos.application.facade;

import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.RubricRepository;
import ru.kokoev.pos.domain.repository.StudentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Фасад действий преподавателя: скрывает работу с репозиториями и сервисом оценок.
 */
public class PosFacade {
    private final StudentRepository studentRepository;
    private final RubricRepository rubricRepository;
    private final EvaluationApplicationService evaluationService;

    public PosFacade(
            StudentRepository studentRepository,
            RubricRepository rubricRepository,
            EvaluationApplicationService evaluationService
    ) {
        if (studentRepository == null || rubricRepository == null || evaluationService == null) {
            throw new IllegalArgumentException("Зависимости фасада не могут быть null");
        }
        this.studentRepository = studentRepository;
        this.rubricRepository = rubricRepository;
        this.evaluationService = evaluationService;
    }

    public Student createStudent(String fullName) {
        Student student = new Student(UUID.randomUUID(), fullName);
        this.studentRepository.save(student);
        return student;
    }

    public List<Student> findStudents() {
        return this.studentRepository.findAll();
    }

    public Rubric createRubric() {
        Rubric rubric = new Rubric(UUID.randomUUID());
        this.rubricRepository.save(rubric);
        return rubric;
    }

    public List<Rubric> findRubrics() {
        return this.rubricRepository.findAll();
    }

    public Criterion addCriterion(UUID rubricId, String name, int maxScore, BigDecimal weight) {
        Rubric rubric = this.rubricRepository.getById(rubricId);
        for (Student student : this.studentRepository.findAll()) {
            if (!this.evaluationService.findEvaluations(student, rubric).isEmpty()) {
                throw new IllegalArgumentException(
                        "Нельзя изменять рубрикатор после начала оценивания"
                );
            }
        }
        Criterion criterion = new Criterion(UUID.randomUUID(), name, maxScore, weight);
        rubric.addCriterion(criterion);
        this.rubricRepository.save(rubric);
        return criterion;
    }

    public List<Criterion> findCriteria(UUID rubricId) {
        return this.rubricRepository.getById(rubricId).getCriteria();
    }

    public Evaluation assignScore(UUID studentId, UUID rubricId, UUID criterionId, int points) {
        Student student = this.studentRepository.getById(studentId);
        Rubric rubric = this.rubricRepository.getById(rubricId);
        return this.evaluationService.assignScore(student, rubric, criterionId, points);
    }

    public List<Evaluation> findEvaluations(UUID studentId, UUID rubricId) {
        Student student = this.studentRepository.getById(studentId);
        Rubric rubric = this.rubricRepository.getById(rubricId);
        return this.evaluationService.findEvaluations(student, rubric);
    }

    public BigDecimal calculateFinal(UUID studentId, UUID rubricId) {
        Student student = this.studentRepository.getById(studentId);
        Rubric rubric = this.rubricRepository.getById(rubricId);
        return this.evaluationService.calculateFinal(student, rubric);
    }
}
