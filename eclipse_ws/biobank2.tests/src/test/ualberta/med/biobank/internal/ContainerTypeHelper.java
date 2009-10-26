package test.ualberta.med.biobank.internal;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;

public class ContainerTypeHelper extends DbHelper {

    public static ContainerTypeWrapper newContainerType(SiteWrapper site,
        String name, String nameShort, Integer labelingScheme,
        Integer rowCapacity, Integer colCapacity, boolean isTopLevel) {
        ContainerTypeWrapper ct = new ContainerTypeWrapper(appService);
        ct.setSite(site);
        ct.setName(name);
        ct.setNameShort(nameShort);
        ct.setChildLabelingScheme(labelingScheme);
        if (rowCapacity != null)
            ct.setRowCapacity(rowCapacity);
        if (colCapacity != null)
            ct.setColCapacity(colCapacity);
        ct.setTopLevel(isTopLevel);
        return ct;
    }

    public static ContainerTypeWrapper addContainerType(SiteWrapper site,
        String name, String nameShort, Integer labelingScheme,
        Integer rowCapacity, Integer colCapacity, boolean isTopLevel)
        throws BiobankCheckException, Exception {
        ContainerTypeWrapper container = newContainerType(site, name,
            nameShort, labelingScheme, rowCapacity, colCapacity, isTopLevel);
        container.persist();
        return container;
    }

    public static ContainerTypeWrapper addContainerTypeRandom(SiteWrapper site,
        String name, boolean topContainer) throws Exception {
        return addContainerType(site, name, "", 1, r.nextInt(10) + 1, r
            .nextInt(10) + 1, topContainer);
    }

    public static ContainerTypeWrapper addContainerTypeRandom(SiteWrapper site,
        String name) throws Exception {
        return addContainerTypeRandom(site, name, r.nextBoolean());
    }

    public static void addContainerTypesRandom(SiteWrapper site, String name,
        int count) throws Exception {
        for (int i = 0; i < count; i++) {
            addContainerTypeRandom(site, name + (i + 1));
        }
        site.reload();
    }

}
