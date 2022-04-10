package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.Main;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.util.List;
import java.util.logging.Level;

public class WebDriver {

    private static ChromeDriver driver = null;
    private static PageLoadStrategy current = null;

    public static ChromeDriver get() {
        return driver;
    }

    public static ChromeDriver init(PageLoadStrategy strategy) {
        if (current != null && current != strategy) {
            dispose();
        }

        if (driver == null) {
            String path = Main.operaDriver.getArgument(0).orElse("/usr/bin/chromedriver");
            System.setProperty("webdriver.opera.driver", path);

            ChromeOptions options = new ChromeOptions();

            LoggingPreferences logs = new LoggingPreferences();
            logs.enable(LogType.BROWSER, Level.ALL);
            options.setCapability(CapabilityType.LOGGING_PREFS, logs);

            options.setPageLoadStrategy(strategy);
            options.addArguments("--auto-open-devtools-for-tabs");
            options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
            //options.setExperimentalOption("useAutomationExtension", false);
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--headless");
            options.addArguments("user-agent=%s".formatted(FakeUserAgent.getUserAgent()));

            driver = new ChromeDriver(options);
            current = strategy;
        }

        return driver;
    }

    public static void dispose() {
        if (driver != null) {
            driver.quit();
            driver = null;
            current = null;
        }
    }
}
