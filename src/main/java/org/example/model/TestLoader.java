package org.example.model;

import java.io.IOException;
import java.util.List;

public class TestLoader {
    private List<DiagnosticTest> availableTests;

    public TestLoader() {
        try {
            this.availableTests = TestConfigLoader.loadTests();
        } catch (IOException e) {
            e.printStackTrace();
            this.availableTests = List.of();
        }
    }

    public DiagnosticTest loadTest(String testName) {
        return availableTests.stream()
                .filter(t -> t.getTestName().contains(testName))
                .findFirst()
                .orElse(null);
    }
}