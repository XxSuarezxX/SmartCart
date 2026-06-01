package com.tienda.ecommerce.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas End-to-End (E2E) con Selenium WebDriver.
 *
 * Verifican que la aplicación, ya desplegada y corriendo, sirve correctamente
 * la SPA de Angular y que las pantallas principales renderizan en un navegador real.
 *
 * Estas pruebas requieren:
 *   - La aplicación corriendo y accesible (por defecto en http://localhost:8080).
 *     Se puede cambiar con -De2e.base-url=http://mi-host:puerto
 *   - Google Chrome instalado (Selenium Manager descarga el driver automáticamente).
 *
 * Están etiquetadas con @Tag("e2e") para que NO se ejecuten en la build normal
 * (ver maven-surefire-plugin en el pom). En el pipeline se lanzan con:
 *   ./mvnw test -Dgroups=e2e -DexcludedGroups=
 */
@Tag("e2e")
@DisplayName("E2E - SmartCart (Selenium)")
class SmartCartE2ETest {

    private static final String BASE_URL =
            System.getProperty("e2e.base-url", "http://localhost:8080");

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void abrirNavegador() {
        ChromeOptions options = new ChromeOptions();
        // Modo headless: imprescindible en CI (sin entorno gráfico)
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    static void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("La página de inicio carga con el título de la tienda")
    void homeCargaConTitulo() {
        driver.get(BASE_URL + "/");

        // El <title> de index.html es estático: "Smartcart"
        assertThat(driver.getTitle()).isEqualToIgnoringCase("Smartcart");
    }

    @Test
    @DisplayName("La barra de navegación muestra la marca SmartCart")
    void navbarMuestraLaMarca() {
        driver.get(BASE_URL + "/");

        // Angular renderiza el navbar en cliente; esperamos a que aparezca <nav>
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("nav")));

        assertThat(driver.getPageSource()).contains("SmartCart");
    }

    @Test
    @DisplayName("La pantalla de login renderiza el formulario de credenciales")
    void loginRenderizaFormulario() {
        driver.get(BASE_URL + "/login");

        // El input de correo tiene id="email" en login.html
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));

        assertThat(driver.findElement(By.id("email")).isDisplayed()).isTrue();
        assertThat(driver.getPageSource()).contains("Iniciar sesión");
    }

    @Test
    @DisplayName("El endpoint público de productos responde dentro de la SPA")
    void catalogoEsAccesible() {
        driver.get(BASE_URL + "/catalogo");

        // Esperamos a que el navbar (parte del shell de la app) esté presente,
        // confirmando que la ruta SPA /catalogo se sirve correctamente.
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("app-root")));

        assertThat(driver.getCurrentUrl()).contains("/catalogo");
    }
}
