package ru.kokoev.pos;

import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;
import ru.kokoev.pos.domain.repository.EvaluationRepository;
import ru.kokoev.pos.domain.service.FinalScoreService;
import ru.kokoev.pos.infrastructure.persistence.InMemoryEvaluationRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        Student student = new Student(UUID.randomUUID(), "Иван Иванов");
        Rubric rubric = new Rubric(UUID.randomUUID());
        Criterion exam = new Criterion(
                UUID.randomUUID(), "Экзамен", 10, new BigDecimal("0.60")
        );
        Criterion project = new Criterion(
                UUID.randomUUID(), "Проект", 20, new BigDecimal("0.40")
        );
        rubric.addCriterion(exam);
        rubric.addCriterion(project);

        EvaluationRepository repository = new InMemoryEvaluationRepository();
        FinalScoreService finalScoreService = new FinalScoreService();
        EvaluationApplicationService service = new EvaluationApplicationService(
                repository, finalScoreService
        );

        Evaluation examEvaluation = service.assignScore(student, rubric, exam.getId(), 8);
        Evaluation projectEvaluation = service.assignScore(student, rubric, project.getId(), 18);
        BigDecimal result = service.calculateFinal(student, rubric);
        BigDecimal percentage = result.multiply(BigDecimal.valueOf(100)).stripTrailingZeros();

        System.out.println("Студент: " + student.getFullName());
        System.out.println(exam.getName() + ": " + examEvaluation.getScore().value() + "/" + exam.getMaxScore());
        System.out.println(project.getName() + ": " + projectEvaluation.getScore().value() + "/" + project.getMaxScore());
        System.out.println("Итоговый результат: " + percentage.toPlainString() + "%");
    }
}
