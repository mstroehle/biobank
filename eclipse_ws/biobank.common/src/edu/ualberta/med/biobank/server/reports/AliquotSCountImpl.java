package edu.ualberta.med.biobank.server.reports;

import java.util.List;

import edu.ualberta.med.biobank.common.reports2.ReportOption;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.AliquotPosition;

public class AliquotSCountImpl extends AbstractReport {

    private static final String QUERY = "Select Alias.patientVisit.patient.study.nameShort, Alias.sampleType.name, count(*) from "
        + Aliquot.class.getName()
        + " as Alias where Alias.aliquotPosition not in (from "
        + AliquotPosition.class.getName()
        + " a where a.container.label like 'SS%') and Alias.patientVisit.patient.study.site "
        + siteOperatorString
        + siteIdString
        + " GROUP BY Alias.patientVisit.patient.study.nameShort, Alias.sampleType.name";

    public AliquotSCountImpl(List<Object> parameters, List<ReportOption> options) {
        super(QUERY, parameters, options);
    }

}
