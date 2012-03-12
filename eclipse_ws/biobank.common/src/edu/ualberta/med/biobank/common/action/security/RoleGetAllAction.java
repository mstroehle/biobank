package edu.ualberta.med.biobank.common.action.security;

import java.util.List;

import org.hibernate.FetchMode;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.ListResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.security.RoleManagementPermission;
import edu.ualberta.med.biobank.model.Role;

public class RoleGetAllAction implements Action<ListResult<Role>> {
    private static final long serialVersionUID = 1L;
    private static final Permission PERMISSION = new RoleManagementPermission();

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return PERMISSION.isAllowed(context);
    }

    @Override
    public ListResult<Role> run(ActionContext context) throws ActionException {
        @SuppressWarnings("unchecked")
        List<Role> roles = (List<Role>) context.getSession()
            .createCriteria(Role.class)
            .setFetchMode("permissions", FetchMode.JOIN)
            .list();

        return new ListResult<Role>(roles);
    }
}
