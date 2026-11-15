package ru.kokoev.pos.presentation.console;

import ru.kokoev.pos.application.facade.PosFacade;
import ru.kokoev.pos.domain.model.Criterion;
import ru.kokoev.pos.domain.model.Evaluation;
import ru.kokoev.pos.domain.model.Rubric;
import ru.kokoev.pos.domain.model.Student;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleMenu {
    private final PosFacade facade;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleMenu(PosFacade facade, Scanner input, PrintStream output) {
        if (facade == null || input == null || output == null) {
            throw new IllegalArgumentException("Зависимости меню не могут быть null");
        }
        this.facade = facade;
        this.input = input;
        this.output = output;
    }

    public void run() {
        while (true) {
            printMenu();
            try {
                String command = readLine("Команда: ");
                switch (command) {
                    case "1" -> createStudent();
                    case "2" -> printStudents(this.facade.findStudents());
                    case "3" -> {
                        Rubric rubric = this.facade.createRubric();
                        this.output.println("Рубрикатор создан: " + rubric.getId());
                    }
                    case "4" -> printRubrics(this.facade.findRubrics());
                    case "5" -> addCriterion();
                    case "6" -> printCriteria(this.facade.findCriteria(selectRubric().getId()));
                    case "7" -> assignScore();
                    case "8" -> calculateFinal();
                    case "9" -> showEvaluations();
                    case "0" -> {
                        this.output.println("До свидания!");
                        return;
                    }
                    default -> this.output.println("Неизвестная команда.");
                }
            } catch (IllegalArgumentException exception) {
                this.output.println("Ошибка: " + exception.getMessage());
            } catch (NoSuchElementException exception) {
                this.output.println("Ввод завершён.");
                return;
            }
        }
    }

    private void printMenu() {
        this.output.println("""

                ========== ПОС ==========
                1. Добавить студента
                2. Показать студентов
                3. Создать рубрикатор
                4. Показать рубрикаторы
                5. Добавить критерий
                6. Показать критерии
                7. Выставить оценку
                8. Рассчитать итог
                9. Показать оценки студента
                0. Выход
                """);
    }

    private void createStudent() {
        Student student = this.facade.createStudent(readLine("ФИО студента: "));
        this.output.println("Студент добавлен: " + student.getFullName());
    }

    private void addCriterion() {
        Rubric rubric = selectRubric();
        String name = readLine("Название критерия: ");
        int maxScore = readInt("Максимальный балл: ");
        BigDecimal weight = readDecimal("Вес (например, 0,60): ");
        Criterion criterion = this.facade.addCriterion(rubric.getId(), name, maxScore, weight);
        this.output.println("Критерий добавлен: " + criterion.getName());
    }

    private void assignScore() {
        Student student = selectStudent();
        Rubric rubric = selectRubric();
        List<Criterion> criteria = this.facade.findCriteria(rubric.getId());
        if (criteria.isEmpty()) {
            throw new IllegalArgumentException("Сначала добавьте критерий.");
        }
        printCriteria(criteria);
        Criterion criterion = criteria.get(readIndex("Номер критерия: ", criteria.size()));
        int points = readInt("Балл: ");
        this.facade.assignScore(student.getId(), rubric.getId(), criterion.getId(), points);
        this.output.println("Оценка сохранена.");
    }

    private void showEvaluations() {
        Student student = selectStudent();
        Rubric rubric = selectRubric();
        List<Evaluation> evaluations = this.facade.findEvaluations(student.getId(), rubric.getId());
        this.output.println("Студент: " + student.getFullName());
        if (evaluations.isEmpty()) {
            this.output.println("Оценок пока нет.");
            return;
        }
        for (Criterion criterion : this.facade.findCriteria(rubric.getId())) {
            for (Evaluation evaluation : evaluations) {
                if (criterion.getId().equals(evaluation.getCriterionId())) {
                    this.output.printf(
                            "%s: %d/%d%n",
                            criterion.getName(), evaluation.getScore().value(), criterion.getMaxScore()
                    );
                }
            }
        }
    }

    private void calculateFinal() {
        Student student = selectStudent();
        Rubric rubric = selectRubric();
        BigDecimal result = this.facade.calculateFinal(student.getId(), rubric.getId());
        BigDecimal percentage = result.multiply(BigDecimal.valueOf(100)).stripTrailingZeros();
        this.output.println("Студент: " + student.getFullName());
        this.output.println("Итоговый результат: " + percentage.toPlainString() + "%");
    }

    private Student selectStudent() {
        List<Student> students = this.facade.findStudents();
        if (students.isEmpty()) {
            throw new IllegalArgumentException("Сначала добавьте студента.");
        }
        printStudents(students);
        return students.get(readIndex("Номер студента: ", students.size()));
    }

    private Rubric selectRubric() {
        List<Rubric> rubrics = this.facade.findRubrics();
        if (rubrics.isEmpty()) {
            throw new IllegalArgumentException("Сначала создайте рубрикатор.");
        }
        printRubrics(rubrics);
        return rubrics.get(readIndex("Номер рубрикатора: ", rubrics.size()));
    }

    private void printStudents(List<Student> students) {
        if (students.isEmpty()) {
            this.output.println("Студентов пока нет.");
        }
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            this.output.printf("%d. %s (%s)%n", i + 1, student.getFullName(), student.getId());
        }
    }

    private void printRubrics(List<Rubric> rubrics) {
        if (rubrics.isEmpty()) {
            this.output.println("Рубрикаторов пока нет.");
        }
        for (int i = 0; i < rubrics.size(); i++) {
            this.output.printf("%d. %s%n", i + 1, rubrics.get(i).getId());
        }
    }

    private void printCriteria(List<Criterion> criteria) {
        if (criteria.isEmpty()) {
            this.output.println("Критериев пока нет.");
        }
        for (int i = 0; i < criteria.size(); i++) {
            Criterion criterion = criteria.get(i);
            this.output.printf(
                    "%d. %s — максимум %d, вес %s%n",
                    i + 1, criterion.getName(), criterion.getMaxScore(),
                    criterion.getWeight().toPlainString()
            );
        }
    }

    private int readIndex(String prompt, int size) {
        int number = readInt(prompt);
        if (number < 1 || number > size) {
            throw new IllegalArgumentException("Номер должен быть от 1 до " + size + ".");
        }
        return number - 1;
    }

    private int readInt(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Введите целое число.");
        }
    }

    private BigDecimal readDecimal(String prompt) {
        try {
            return new BigDecimal(readLine(prompt).replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Введите число с десятичной точкой или запятой.");
        }
    }

    private String readLine(String prompt) {
        this.output.print(prompt);
        this.output.flush();
        return this.input.nextLine().trim();
    }
}
