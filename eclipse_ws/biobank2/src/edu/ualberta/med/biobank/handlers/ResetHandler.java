package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.forms.BiobankEntryForm;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;

public class ResetHandler extends AbstractHandler implements IHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            @Override
            public void run() {
                try {
                    BiobankEntryForm biobankEntryForm = (BiobankEntryForm) PlatformUI
                        .getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().getActiveEditor();
                    biobankEntryForm.reset();
                } catch (Exception ex) {
                    BgcPlugin.openAsyncError(Messages.ResetHandler_reset_error, ex);
                }
            }
        });
        return null;
    }
}