package com.octavemc.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class AfterInitializationListener implements TypeListener {

    @Override
    public <I> void hear(TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        encounter
                .register((InjectionListener<? super I>) i -> Arrays.stream(i.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(AfterInitialization.class))
                .forEach(method -> {
                    try {
                        method.invoke(i);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        exception.printStackTrace();
                    }
                }));
    }
}
