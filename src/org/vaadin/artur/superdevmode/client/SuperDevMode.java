package org.vaadin.artur.superdevmode.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VNotification;
import com.vaadin.terminal.gwt.client.ui.VNotification.EventListener;
import com.vaadin.terminal.gwt.client.ui.VNotification.HideEvent;

public class SuperDevMode {

    protected static final String SKIP_RECOMPILE = "VaadinSuperDevMode_skip_recompile";

    private static String getWidgetsetName() {
        return GWT.getModuleName();
    }

    public static void recompileAndEnable(String serverUrl) {
        recompileWidgetsetAndStartInDevMode(serverUrl);
    }

    public static class RecompileResult extends JavaScriptObject {
        protected RecompileResult() {

        }

        public final native boolean ok()
        /*-{
         return this.status == "ok";
        }-*/;
    }

    private static void recompileWidgetsetAndStartInDevMode(
            final String serverUrl) {
        VConsole.log("Recompiling widgetset using<br/>" + serverUrl
                + "<br/>and then reloading in super dev mode");
        VNotification n = new VNotification();
        n.show("<b>Recompiling widgetset, this should not take too long</b>",
                VNotification.CENTERED, VNotification.STYLE_SYSTEM);

        JsonpRequestBuilder b = new JsonpRequestBuilder();
        b.setCallbackParam("_callback");
        b.setTimeout(60000);
        b.requestObject(serverUrl + "recompile/" + getWidgetsetName() + "?"
                + getRecompileParameters(getWidgetsetName()),
                new AsyncCallback<RecompileResult>() {

                    public void onSuccess(RecompileResult result) {
                        VConsole.log("JSONP compile call successful");

                        if (!result.ok()) {
                            VConsole.log("* result: " + result);
                            failed();
                            return;
                        }

                        setSession(
                                getSuperDevModeHookKey(),
                                getSuperDevWidgetSetUrl(getWidgetsetName(),
                                        serverUrl));
                        setSession(SKIP_RECOMPILE, "1");

                        VConsole.log("* result: OK. Reloading");
                        Location.reload();
                    }

                    public void onFailure(Throwable caught) {
                        VConsole.error("JSONP compile call failed");
                        VConsole.error(caught);
                        failed();

                    }

                    private void failed() {
                        VNotification n = new VNotification();
                        n.addEventListener(new EventListener() {

                            public void notificationHidden(HideEvent event) {
                                recompileWidgetsetAndStartInDevMode(serverUrl);
                            }
                        });
                        n.show("Recompilation failed.<br/>"
                                + "Make sure CodeServer is running, "
                                + "check its output and click to retry",
                                VNotification.CENTERED,
                                VNotification.STYLE_SYSTEM);
                    }
                });

    }

    protected static String getSuperDevWidgetSetUrl(String widgetsetName,
            String serverUrl) {
        return serverUrl + getWidgetsetName() + "/" + getWidgetsetName()
                + ".nocache.js";
    }

    private native static String getRecompileParameters(String moduleName)
    /*-{
        var prop_map = $wnd.__gwt_activeModules[moduleName].bindings();
        
        // convert map to URL parameter string
        var props = [];
        for (var key in prop_map) {
           props.push(encodeURIComponent(key) + '=' + encodeURIComponent(prop_map[key]))
        }
        
        return props.join('&') + '&';
    }-*/;

    private static void setSession(String key, String value) {
        Storage.getSessionStorageIfSupported().setItem(key, value);
    }

    private static String getSession(String key) {
        return Storage.getSessionStorageIfSupported().getItem(key);
    }

    private static void removeSession(String key) {
        Storage.getSessionStorageIfSupported().removeItem(key);
    }

    public static void disableDevModeAndReload() {
        removeSession(getSuperDevModeHookKey());

        redirect(false);
    }

    public static void redirect(boolean devModeOn) {
        UrlBuilder createUrlBuilder = Location.createUrlBuilder();
        if (!devModeOn) {
            createUrlBuilder.removeParameter("superdevmode");
        } else {
            createUrlBuilder.setParameter("superdevmode", "");
        }

        Location.assign(createUrlBuilder.buildString());

    }

    private static String getSuperDevModeHookKey() {
        String widgetsetName = getWidgetsetName();
        final String superDevModeKey = "__gwtDevModeHook:" + widgetsetName;
        return superDevModeKey;
    }

    private static boolean hasSession(String key) {
        return getSession(key) != null;
    }

    /**
     * The URL of the code server. The default URL (http://localhost:9876/) will
     * be used if this is empty or null.
     * 
     * @param serverUrl
     * @return
     */
    public static boolean recompileIfNeeded(String serverUrl) {
        if (serverUrl == null || "".equals(serverUrl)) {
            serverUrl = "http://localhost:9876/";
        } else {
            serverUrl = "http://" + serverUrl + "/";
        }

        if (hasSession(SKIP_RECOMPILE)) {
            VConsole.log("Was just recompiled, starting super dev mode");
            // When we get here, we are running in super dev mode

            // Remove the flag so next reload will recompile
            removeSession(SKIP_RECOMPILE);

            // Remove the gwt flag so we will not end up in dev mode if we
            // remove the url parameter manually
            removeSession(getSuperDevModeHookKey());

            return false;
        }
        VConsole.log("Was NOT recently compiled. Recompiling...");
        recompileAndEnable(serverUrl);

        return true;

    }

}
