package test.ualberta.med.biobank.internal;

import edu.ualberta.med.biobank.common.wrappers.SampleStorageWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;

public class SampleStorageHelper extends DbHelper {

    public static SampleStorageWrapper newSampleStorage(StudyWrapper study,
        SampleTypeWrapper type) {
        SampleStorageWrapper sampleStorage = new SampleStorageWrapper(
            appService);
        sampleStorage.setStudy(study);
        sampleStorage.setSampleType(type);
        sampleStorage.setQuantity(r.nextInt(10));
        sampleStorage.setVolume(r.nextDouble());
        return sampleStorage;
    }

    public static SampleStorageWrapper addSampleStorage(StudyWrapper study,
        SampleTypeWrapper type) throws Exception {
        SampleStorageWrapper sampleStorage = newSampleStorage(study, type);
        sampleStorage.persist();
        return sampleStorage;
    }

    public static int addSampleStorages(StudyWrapper study, String name)
        throws Exception {
        int nber = r.nextInt(15) + 1;
        for (int i = 0; i < nber; i++) {
            SampleTypeWrapper type = SampleTypeHelper.addSampleType(study
                .getSite(), name + i);
            addSampleStorage(study, type);
        }
        study.reload();
        return nber;
    }
}