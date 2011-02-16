package edu.ualberta.med.biobank.test.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.SourceVesselTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SourceVesselWrapper;
import edu.ualberta.med.biobank.test.Utils;

public class SourceVesselHelper extends DbHelper {

    public static List<SourceVesselWrapper> createdSourceVessels = new ArrayList<SourceVesselWrapper>();

    public static SourceVesselWrapper newSourceVessel(PatientWrapper patient,
        Date timeDrawn, Double volume) {
        SourceVesselWrapper source = new SourceVesselWrapper(appService);
        SourceVesselTypeWrapper svt = SourceVesselTypeHelper
            .addSourceVesselType(Utils.getRandomString(11));
        source.setSourceVesselType(svt);
        source.setPatient(patient);
        source.setTimeDrawn(timeDrawn);
        source.setVolume(volume);
        return source;
    }

    public static void deleteCreatedSourceVessels() throws Exception {
        for (SourceVesselWrapper source : createdSourceVessels) {
            source.reload();
            source.delete();
        }
        createdSourceVessels.clear();
    }

}
