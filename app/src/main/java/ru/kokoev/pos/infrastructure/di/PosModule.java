package ru.kokoev.pos.infrastructure.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ru.kokoev.pos.application.facade.PosFacade;
import ru.kokoev.pos.application.port.ResultExporter;
import ru.kokoev.pos.application.service.EvaluationApplicationService;
import ru.kokoev.pos.domain.factory.EvaluationFactory;
import ru.kokoev.pos.domain.repository.EvaluationRepository;
import ru.kokoev.pos.domain.repository.RubricRepository;
import ru.kokoev.pos.domain.repository.StudentRepository;
import ru.kokoev.pos.domain.service.FinalScoreService;
import ru.kokoev.pos.domain.strategy.ScoringStrategy;
import ru.kokoev.pos.domain.strategy.WeightedScoringStrategy;
import ru.kokoev.pos.infrastructure.export.CsvResultExporter;
import ru.kokoev.pos.infrastructure.persistence.InMemoryEvaluationRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryRubricRepository;
import ru.kokoev.pos.infrastructure.persistence.InMemoryStudentRepository;
import ru.kokoev.pos.presentation.console.ConsoleMenu;

import java.util.Scanner;

public class PosModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StudentRepository.class)
                .to(InMemoryStudentRepository.class)
                .in(Singleton.class);
        bind(RubricRepository.class)
                .to(InMemoryRubricRepository.class)
                .in(Singleton.class);
        bind(EvaluationRepository.class)
                .to(InMemoryEvaluationRepository.class)
                .in(Singleton.class);
        bind(ScoringStrategy.class)
                .to(WeightedScoringStrategy.class)
                .in(Singleton.class);
        bind(EvaluationFactory.class).in(Singleton.class);
        bind(ResultExporter.class)
                .to(CsvResultExporter.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    FinalScoreService provideFinalScoreService(ScoringStrategy strategy) {
        return new FinalScoreService(strategy);
    }

    @Provides
    @Singleton
    EvaluationApplicationService provideEvaluationApplicationService(
            EvaluationRepository repository,
            FinalScoreService finalScoreService,
            EvaluationFactory evaluationFactory
    ) {
        return new EvaluationApplicationService(repository, finalScoreService, evaluationFactory);
    }

    @Provides
    @Singleton
    PosFacade providePosFacade(
            StudentRepository studentRepository,
            RubricRepository rubricRepository,
            EvaluationApplicationService evaluationService,
            ResultExporter resultExporter
    ) {
        return new PosFacade(studentRepository, rubricRepository, evaluationService, resultExporter);
    }

    @Provides
    @Singleton
    ConsoleMenu provideConsoleMenu(PosFacade facade) {
        return new ConsoleMenu(facade, new Scanner(System.in), System.out);
    }
}
