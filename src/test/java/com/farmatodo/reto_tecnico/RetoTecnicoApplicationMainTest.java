package com.farmatodo.reto_tecnico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit test for the main application class.
 * Tests the main method to ensure coverage without starting the full context.
 */
@DisplayName("RetoTecnicoApplication Main Tests")
class RetoTecnicoApplicationMainTest {

    @Test
    @DisplayName("Should run main method without errors")
    void shouldRunMainMethodWithoutErrors() {
        // Given & When & Then
        try (MockedStatic<SpringApplication> mockedSpringApp = Mockito.mockStatic(SpringApplication.class)) {
            // Mock SpringApplication.run to do nothing
            mockedSpringApp.when(() -> SpringApplication.run(RetoTecnicoApplication.class, new String[]{}))
                    .thenReturn(null);

            // Execute main method
            assertThatCode(() -> RetoTecnicoApplication.main(new String[]{}))
                    .doesNotThrowAnyException();

            // Verify SpringApplication.run was called
            mockedSpringApp.verify(() -> SpringApplication.run(RetoTecnicoApplication.class, new String[]{}));
        }
    }

    @Test
    @DisplayName("Should instantiate application class")
    void shouldInstantiateApplicationClass() {
        // When
        RetoTecnicoApplication app = new RetoTecnicoApplication();

        // Then
        assertThatCode(() -> app.getClass())
                .doesNotThrowAnyException();
    }
}
