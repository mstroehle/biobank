package edu.ualberta.med.biobank.test.reports;

import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.ContainerLabelingScheme;
import edu.ualberta.med.biobank.model.util.RowColPos;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ContainerEmptyLocationsPostProcessTester implements
    PostProcessTester {
    @Override
    public List<Object> postProcess(WritableApplicationService appService,
        Collection<Object> results) {
        List<Object> processedResults = new ArrayList<Object>();

        for (Object c : results) {
            try {
                ContainerWrapper container = new ContainerWrapper(appService, (Container) c);
                ContainerTypeWrapper ctype = container.getContainerType();

                Map<RowColPos, SpecimenWrapper> aliquots = container.getSpecimens();

                for (int i = 0, numRows = container.getRowCapacity(); i < numRows; i++) {
                    for (int j = 0, numCols = container.getColCapacity(); j < numCols; j++) {
                        RowColPos rowColPos = new RowColPos(i, j);
                        if (!aliquots.containsKey(rowColPos)) {
                            processedResults.add(new Object[] {
                                container.getLabel()
                                    + ContainerLabelingScheme.getPositionString(
                                        rowColPos, ctype.getChildLabelingSchemeId(),
                                        numRows, numCols, ctype.getLabelingLayout()),
                                container.getContainerType().getNameShort() });
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return processedResults;
    }
}
