package edu.ualberta.med.biobank.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.WorkbenchException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.rcp.MainPerspective;
import edu.ualberta.med.biobank.rcp.PatientsAdministrationPerspective;

public class MainAdministrationHanlder extends AbstractHandler implements
    IHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbench workbench = BioBankPlugin.getDefault().getWorkbench();
        boolean open = true;
        if (workbench.getActiveWorkbenchWindow().getActivePage()
            .getPerspective().getId().equals(
                PatientsAdministrationPerspective.ID)) {
            open = BioBankPlugin
                .openConfirm("Quit patients management",
                    "You are about to quit the patients management, are you sure ?");
        }
        if (open) {
            try {
                workbench.showPerspective(MainPerspective.ID, workbench
                    .getActiveWorkbenchWindow());
            } catch (WorkbenchException e) {
                throw new ExecutionException(
                    "Error while opening Main perpective", e);
            }
        }
        return null;
    }
}
