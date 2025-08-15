package fr.robotv2.placeholderannotationlib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Expansion {

    String identifier();

    String version();

    String[] author() default {};

    boolean persist() default false;
}
