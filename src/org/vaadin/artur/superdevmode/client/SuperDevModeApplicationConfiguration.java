package org.vaadin.artur.superdevmode.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootPanel;

public class SuperDevModeApplicationConfiguration implements EntryPoint {

    public void onModuleLoad() {
        String superDevModeParameter = Location.getParameter("superdevmode");
        if (superDevModeParameter != null) {
            if (SuperDevMode.recompileIfNeeded(superDevModeParameter)) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {
                        // Hide the app until we reload
                        Element root = RootPanel.get().getBodyElement();
                        for (int i = 0; i < root.getChildCount(); i++) {
                            Node child = root.getChild(i);
                            if (child instanceof Element) {
                                Element e = (Element) child;
                                if (e.getClassName() != null) {
                                    if (e.getClassName().startsWith("v-app ")) {
                                        e.getStyle().setDisplay(Display.NONE);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}
