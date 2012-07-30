package edu.ualberta.med.biobank.action.specimenType;

import java.util.HashSet;
import java.util.Set;

import edu.ualberta.med.biobank.action.Action;
import edu.ualberta.med.biobank.action.ActionContext;
import edu.ualberta.med.biobank.action.IdResult;
import edu.ualberta.med.biobank.action.exception.ActionException;
import edu.ualberta.med.biobank.action.util.SetDifference;
import edu.ualberta.med.biobank.model.SpecimenType;
import edu.ualberta.med.biobank.permission.Permission;
import edu.ualberta.med.biobank.permission.specimenType.SpecimenTypeCreatePermission;
import edu.ualberta.med.biobank.permission.specimenType.SpecimenTypeUpdatePermission;

public class SpecimenTypeSaveAction implements Action<IdResult> {

    private static final long serialVersionUID = 1L;

    public Integer specimenTypeId;
    public String name;
    public String nameShort;
    public Set<Integer> childSpecimenTypeIds;

    private SpecimenType specimenType;

    public SpecimenTypeSaveAction(String name, String nameShort) {
        this.name = name;
        this.nameShort = nameShort;
        this.childSpecimenTypeIds = new HashSet<Integer>();
    }

    public void setId(Integer specimenTypeId) {
        this.specimenTypeId = specimenTypeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameShort(String nameShort) {
        this.nameShort = nameShort;
    }

    public void setChildSpecimenTypeIds(Set<Integer> childSpecimenTypeIds) {
        this.childSpecimenTypeIds = childSpecimenTypeIds;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        Permission permission;
        if (specimenTypeId == null)
            permission = new SpecimenTypeCreatePermission();
        else
            permission = new SpecimenTypeUpdatePermission();
        return permission.isAllowed(context);
    }

    @Override
    public IdResult run(ActionContext context) throws ActionException {
        specimenType =
            context.get(SpecimenType.class, specimenTypeId, new SpecimenType());
        specimenType.setDescription(name);
        specimenType.setName(nameShort);

        saveChildSpecimenTypes(context);

        context.getSession().saveOrUpdate(specimenType);
        context.getSession().flush();

        return new IdResult(specimenType.getId());
    }

    private void saveChildSpecimenTypes(ActionContext context) {
        Set<SpecimenType> studies =
            context.load(SpecimenType.class, childSpecimenTypeIds);
        SetDifference<SpecimenType> sitesDiff =
            new SetDifference<SpecimenType>(
                specimenType.getChildSpecimenTypes(), studies);
        specimenType.setChildSpecimenTypes(sitesDiff.getNewSet());
        for (SpecimenType childType : sitesDiff.getRemoveSet()) {
            context.getSession().delete(childType);
        }

    }
}
