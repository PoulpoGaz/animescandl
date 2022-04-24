package fr.poulpogaz.animescandl.utils;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class CEFHelper extends JFrame {

    private static boolean isInitialized = false;

    private static CefApp cefApp;
    private static CEFHelper helper;

    public static void initialize()
            throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        if (isInitialized) {
            return;
        }

        CefAppBuilder builder = new CefAppBuilder();
        builder.setProgressHandler((enumProgress, v) -> {

        });

        CefSettings settings = builder.getCefSettings();
        settings.windowless_rendering_enabled = false;
        //settings.log_file = "cef.log";
        settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_DISABLE;

        cefApp = builder.build();
        isInitialized = true;
    }

    public static void shutdown() {
        if (!isInitialized) {
            return;
        }

        if (helper != null) {
            helper.close();
            helper.getClient().dispose();
            helper.dispose();
            helper = null;
        }

        cefApp.dispose();
    }

    public static CEFHelper getInstance() {
        if (helper == null) {
            helper = new CEFHelper();
        }

        return helper;
    }


    private final CefClient client;
    private CefBrowser browser;
    private Component browserUI;

    private CEFHelper() {
        if (!isInitialized) {
            throw new IllegalStateException("Not initialized");
        }

        client = cefApp.createClient();
        setLayout(new BorderLayout());
    }

    public void loadURL(String url) {
        if (browser == null) {
            browser = client.createBrowser(url, false, false);
            browserUI = browser.getUIComponent();

            getContentPane().add(browserUI, BorderLayout.CENTER);
            revalidate();
            repaint();
            pack();
            setSize(1024, 576);
        } else {
            removeAllHandler();
            browser.loadURL(url);
        }
    }

    public void removeAllHandler() {
        client.removeContextMenuHandler();
        client.removeDialogHandler();
        client.removeDisplayHandler();
        client.removeDownloadHandler();
        client.removeDragHandler();
        client.removeFocusHandler();
        client.removeJSDialogHandler();
        client.removeKeyboardHandler();
        client.removeLifeSpanHandler();
        client.removeLoadHandler();
        client.removePrintHandler();
        client.removeRequestHandler();
    }

    public void close() {
        removeAllHandler();
        removeAll();
        if (browser != null) {
            browser.close(true);
        }
        browser = null;
        browserUI = null;
    }

    public static CefApp getCefApp() {
        return cefApp;
    }

    public CefClient getClient() {
        return client;
    }

    public CefBrowser getBrowser() {
        return browser;
    }
}
