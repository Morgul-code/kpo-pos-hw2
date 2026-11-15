package ru.kokoev.pos;

import ru.kokoev.pos.application.facade.PosFacade;
import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.repository.EvaluationRepository;
import ru.kokoev.pos.domain.repository.RubricRepository;
import ru.kokoev.pos.domain.repository.StudentRepository;
import ru.kokoev.pos.domain.service.FinalScoreService;
import ru.kokoev.pos.infrastructure.persistence.InMemoryEvaluationRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryRubricRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryStudentRepository;
import ru.kokoev.pos.presentation.console.ConsoleMenu;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        StudentRepository studentRepository = new InMemoryStudentRepository();
        RubricRepository rubricRepository = new InMemoryRubricRepository();
        EvaluationRepository repository = new InMemoryEvaluationRepository();
        FinalScoreService finalScoreService = new FinalScoreService();
        EvaluationApplicationService service = new EvaluationApplicationService(
                repository, finalScoreService
        );

        PosFacade facade = new PosFacade(studentRepository, rubricRepository, service);
        ConsoleMenu menu = new ConsoleMenu(facade, new Scanner(System.in), System.out);
        menu.run();
    }
}
