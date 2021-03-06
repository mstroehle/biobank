package edu.ualberta.med.biobank;

import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.mvp.AbstractAppContext;

public class BiobankAppContext extends AbstractAppContext {
    @Override
    public User getUser() {
        return SessionManager.getUser().getWrappedObject();
    }

    @Override
    public Center getWorkingCenter() {
        CenterWrapper<?> current =
            SessionManager.getUser().getCurrentWorkingCenter();

        return current != null ? current.getWrappedObject() : null;
    }
}
