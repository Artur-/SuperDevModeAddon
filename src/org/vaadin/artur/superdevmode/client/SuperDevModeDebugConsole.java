package org.vaadin.artur.superdevmode.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.vaadin.terminal.gwt.client.VDebugConsole;

public class SuperDevModeDebugConsole extends VDebugConsole {
    private CheckBox superDevMode = new CheckBox("SD");

    @Override
    public void init() {
        super.init();
        addSuperDevModeIfSupported();
    }

    private void addSuperDevModeIfSupported() {
        final Storage sessionStorage = Storage.getSessionStorageIfSupported();
        if (sessionStorage != null) {
            getActions().add(superDevMode);
            if (Location.getParameter("superdevmode") != null) {
                superDevMode.setValue(true);
            }
            superDevMode
                    .addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                        public void onValueChange(
                                ValueChangeEvent<Boolean> event) {
                            SuperDevMode.redirect(event.getValue());
                        }

                    });
        }

    }

    private Panel getActions() {
        FlowPanel panel = (FlowPanel) getWidget();
        return (Panel) panel.getWidget(0);
    }
}
