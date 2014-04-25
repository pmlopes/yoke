package com.jetdrone.vertx.yoke.util;

import com.jetdrone.vertx.yoke.core.YokeException;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.util.validation.Assertion;
import com.jetdrone.vertx.yoke.util.validation.That;

import java.util.ArrayList;
import java.util.List;

public class Validator {

    private boolean failOnFirstError = true;

    private final Assertion[] assertions;

    public Validator(Assertion... assertions) {
        this.assertions = assertions;
    }

    public void setFailOnFirstError(boolean failOnFirstError) {
        this.failOnFirstError = failOnFirstError;
    }

    public static That that(String path) {
        return new That(path);
    }

    public boolean isValid(final YokeRequest request) {
        return validate(request).size() == 0;
    }

    public List<String> validate(final YokeRequest request) {

        final List<String> failures = new ArrayList<>();

        // check all items
        for (Assertion assertion : assertions) {
            try {
                assertion.ok(request);
            } catch (YokeException e) {
                if (failOnFirstError) {
                    failures.add(e.getMessage());
                    return failures;
                } else {
                    failures.add(e.getMessage());
                }
            }
        }

        return failures;
    }
}
