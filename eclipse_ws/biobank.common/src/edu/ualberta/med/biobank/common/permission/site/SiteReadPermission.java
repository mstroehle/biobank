package edu.ualberta.med.biobank.common.permission.site;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.model.PermissionEnum;
import edu.ualberta.med.biobank.model.Site;

public class SiteReadPermission implements Permission {
    private static final long serialVersionUID = 1L;

    private final Integer siteId;

    public SiteReadPermission(Integer siteId) {
        this.siteId = siteId;
    }

    public SiteReadPermission(Site site) {
        this(site.getId());
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        Site site = context.load(Site.class, siteId);
        return PermissionEnum.SITE_READ.isAllowed(context.getUser(), site);
    }
}
