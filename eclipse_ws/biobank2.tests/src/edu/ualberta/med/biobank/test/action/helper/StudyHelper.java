package edu.ualberta.med.biobank.test.action.helper;

import java.util.HashSet;
import java.util.Set;

import edu.ualberta.med.biobank.common.action.activityStatus.ActivityStatusEnum;
import edu.ualberta.med.biobank.common.action.study.StudyGetInfoAction.StudyInfo;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction;
import edu.ualberta.med.biobank.model.AliquotedSpecimen;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.SourceSpecimen;
import edu.ualberta.med.biobank.model.StudyEventAttr;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class StudyHelper extends Helper {

    public static Integer createStudy(BiobankApplicationService appService,
        String name, ActivityStatusEnum activityStatus)
        throws ApplicationException {
        StudySaveAction saveStudy = new StudySaveAction();
        saveStudy.setName(name);
        saveStudy.setNameShort(name);
        saveStudy.setActivityStatusId(activityStatus.getId());
        saveStudy.setSiteIds(new HashSet<Integer>());
        saveStudy.setContactIds(new HashSet<Integer>());
        saveStudy.setSourceSpcIds(new HashSet<Integer>());
        saveStudy.setAliquotSpcIds(new HashSet<Integer>());
        saveStudy.setStudyEventAttrSaveIds(new HashSet<Integer>());

        return appService.doAction(saveStudy);
    }

    public static StudySaveAction getSaveAction(
        BiobankApplicationService appService, StudyInfo studyInfo)
        throws ApplicationException {
        StudySaveAction saveStudy = new StudySaveAction();
        saveStudy.setId(studyInfo.study.getId());
        saveStudy.setName(studyInfo.study.getName());
        saveStudy.setNameShort(studyInfo.study.getNameShort());
        saveStudy.setActivityStatusId(studyInfo.study.getActivityStatus()
            .getId());

        saveStudy.setSiteIds(new HashSet<Integer>());

        Set<Integer> ids = new HashSet<Integer>();
        for (Contact c : studyInfo.contacts) {
            ids.add(c.getId());
        }
        saveStudy.setContactIds(ids);

        ids = new HashSet<Integer>();
        for (SourceSpecimen spc : studyInfo.sourceSpcs) {
            ids.add(spc.getId());
        }
        saveStudy.setSourceSpcIds(ids);

        ids = new HashSet<Integer>();
        for (AliquotedSpecimen spc : studyInfo.aliquotedSpcs) {
            ids.add(spc.getId());
        }
        saveStudy.setAliquotSpcIds(ids);

        ids = new HashSet<Integer>();
        for (StudyEventAttr attr : studyInfo.studyEventAttrs) {
            ids.add(attr.getId());
        }
        saveStudy.setStudyEventAttrSaveIds(ids);

        return saveStudy;
    }

}