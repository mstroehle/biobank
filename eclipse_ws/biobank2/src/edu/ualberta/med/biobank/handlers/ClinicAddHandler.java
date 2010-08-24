package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.treeview.ClinicAdapter;

public class ClinicAddHandler extends AbstractHandler {
    public static final String ID = "edu.ualberta.med.biobank.commands.addClinic";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ClinicWrapper clinic = new ClinicWrapper(SessionManager.getAppService());
        ClinicAdapter clinicNode = new ClinicAdapter(SessionManager
            .getInstance().getSession().getClinicGroupNode(), clinic);
        clinicNode.openEntryForm();
        return null;
    }

    @Override
    public boolean isEnabled() {
        return SessionManager.canCreate(ClinicWrapper.class)
            && SessionManager.getInstance().getSession() != null;
    }
}
