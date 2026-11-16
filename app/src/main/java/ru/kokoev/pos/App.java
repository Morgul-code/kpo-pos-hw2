package ru.kokoev.pos;

import com.google.inject.Guice;
import com.google.inject.Injector;
import ru.kokoev.pos.infrastructure.di.PosModule;
import ru.kokoev.pos.presentation.console.ConsoleMenu;

public class App {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new PosModule());
        ConsoleMenu menu = injector.getInstance(ConsoleMenu.class);
        menu.run();
    }
}
