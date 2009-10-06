package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.model.Address;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.Study;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class ClinicWrapper extends ModelWrapper<Clinic> implements
    Comparable<ClinicWrapper> {

    private AddressWrapper addressWrapper;

    public ClinicWrapper(WritableApplicationService appService,
        Clinic wrappedObject) {
        super(appService, wrappedObject);
        Address address = wrappedObject.getAddress();
        if (address == null) {
            address = new Address();
            wrappedObject.setAddress(address);
        }
        addressWrapper = new AddressWrapper(appService, address);
    }

    public AddressWrapper getAddressWrapper() {
        return addressWrapper;
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public void setName(String name) {
        String oldName = getName();
        wrappedObject.setName(name);
        propertyChangeSupport.firePropertyChange("name", oldName, name);
    }

    public String getActivityStatus() {
        return wrappedObject.getActivityStatus();
    }

    public void setActivityStatus(String activityStatus) {
        String oldStatus = getActivityStatus();
        wrappedObject.setActivityStatus(activityStatus);
        propertyChangeSupport.firePropertyChange("activityStatus", oldStatus,
            activityStatus);
    }

    public String getComment() {
        return wrappedObject.getComment();
    }

    public void setComment(String comment) {
        String oldComment = getComment();
        wrappedObject.setName(comment);
        propertyChangeSupport
            .firePropertyChange("comment", oldComment, comment);
    }

    public Site getSite() {
        return wrappedObject.getSite();
    }

    public void setSiteWrapper(SiteWrapper siteWrapper) {
        Site oldSite = wrappedObject.getSite();
        Site newSite = siteWrapper.getWrappedObject();
        wrappedObject.setSite(newSite);
        propertyChangeSupport.firePropertyChange("site", oldSite, newSite);
    }

    @Override
    protected String[] getPropertyChangesNames() {
        return new String[] { "name", "activityStatus", "comment", "site" };
    }

    @Override
    protected void persistChecks() throws BiobankCheckException, Exception {
        if (!checkClinicNameUnique()) {
            throw new BiobankCheckException("A clinic with name \"" + getName()
                + "\" already exists.");
        }
    }

    public boolean checkClinicNameUnique() throws ApplicationException {
        HQLCriteria c;

        if (getWrappedObject().getId() == null) {
            c = new HQLCriteria("from " + Clinic.class.getName()
                + " where name = ?", Arrays.asList(new Object[] { getName() }));
        } else {
            c = new HQLCriteria("from " + Clinic.class.getName()
                + " as clinic where site = ? and name = ? and clinic <> ?",
                Arrays.asList(new Object[] { getSite(), getName(),
                    getWrappedObject() }));
        }

        List<Clinic> results = appService.query(c);
        return (results.size() == 0);
    }

    @Override
    protected Class<Clinic> getWrappedClass() {
        return Clinic.class;
    }

    public Collection<ContactWrapper> getContactCollection() {
        Collection<ContactWrapper> wrapperCollection = new HashSet<ContactWrapper>();
        Collection<Contact> collection = wrappedObject.getContactCollection();
        if (collection != null)
            for (Contact contact : collection) {
                wrapperCollection.add(new ContactWrapper(appService, contact));
            }
        return wrapperCollection;
    }

    public List<ContactWrapper> getClinicCollectionSorted() {
        List<ContactWrapper> list = new ArrayList<ContactWrapper>(
            getContactCollection());
        if (list.size() > 1) {
            Collections.sort(list);
        }
        return list;
    }

    public Collection<StudyWrapper> getStudyCollection()
        throws ApplicationException {
        HQLCriteria c = new HQLCriteria("select distinct studies from "
            + Contact.class.getName() + " as contacts"
            + " inner join contacts.studyCollection as studies"
            + " where contacts.clinic = ?", Arrays
            .asList(new Object[] { wrappedObject }));

        Collection<StudyWrapper> wrapperCollection = new HashSet<StudyWrapper>();
        List<Study> collection = appService.query(c);
        for (Study study : collection) {
            wrapperCollection.add(new StudyWrapper(appService, study));
        }
        return wrapperCollection;
    }

    public List<StudyWrapper> getStudyCollectionSorted()
        throws Exception {
        List<StudyWrapper> list = new ArrayList<StudyWrapper>(
            getStudyCollection());
        if (list.size() > 1) {
            Collections.sort(list);
        }
        return list;
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException, Exception {
        // TODO Auto-generated method stub
    }

    public int compareTo(ClinicWrapper wrapper) {
        String myName = wrappedObject.getName();
        String wrapperName = wrapper.wrappedObject.getName();
        return ((myName.compareTo(wrapperName) > 0) ? 1 : (myName
            .equals(wrapperName) ? 0 : -1));
    }

}
