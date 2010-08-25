package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.Log;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.PatientVisit;
import edu.ualberta.med.biobank.model.Shipment;
import edu.ualberta.med.biobank.model.ShippingMethod;
import edu.ualberta.med.biobank.model.Site;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class ShipmentWrapper extends ModelWrapper<Shipment> {

    private Set<PatientWrapper> patientsAdded = new HashSet<PatientWrapper>();
    private Set<PatientWrapper> patientsRemoved = new HashSet<PatientWrapper>();
    private ClinicWrapper clinic;

    public ShipmentWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public ShipmentWrapper(WritableApplicationService appService,
        Shipment wrappedObject) {
        super(appService, wrappedObject);
    }

    @Override
    protected void deleteChecks() throws Exception {
        checkNoMorePatientVisits();
    }

    private void checkNoMorePatientVisits() throws Exception {
        List<PatientVisitWrapper> visits = getPatientVisitCollection();
        if (visits != null && visits.size() > 0) {
            throw new BiobankCheckException(
                "Visits are still linked to this shipment. Delete them before attempting to remove the shipment.");
        }
    }

    @Override
    protected String[] getPropertyChangeNames() {
        return new String[] { "dateShipped", "dateReceived", "clinic",
            "comment", "patientVisitCollection", "waybill", "boxNumber",
            "shippingMethod", "patientCollection" };
    }

    @Override
    public Class<Shipment> getWrappedClass() {
        return Shipment.class;
    }

    @Override
    public void persist() throws Exception {
        super.persist();
        patientsAdded.clear();
    }

    @Override
    protected void persistChecks() throws BiobankCheckException,
        ApplicationException, WrapperException {
        if (getClinic() != null
            && Boolean.TRUE.equals(getClinic().getSendsShipments())) {
            if (getWaybill() == null || getWaybill().isEmpty()) {
                throw new BiobankCheckException(
                    "A waybill should be set on this shipment");
            }
            if (!checkWaybillUniqueForClinic()) {
                throw new BiobankCheckException("A shipment with waybill "
                    + getWaybill() + " already exist in clinic "
                    + getClinic().getName() + ".");
            }
        } else {
            if (getWaybill() != null) {
                throw new BiobankCheckException(
                    "This shipment clinic doesn't send shipment: waybill should not be set");
            }
        }
        checkAlLeastOnePatient();
        checkPatientsStudy();
        checkRemovedPatients();
        checkDateReceivedNotNull();
    }

    private void checkRemovedPatients() throws BiobankCheckException {
        if (!isNew()) {
            for (PatientWrapper patient : patientsRemoved) {
                checkCanRemovePatient(patient);
            }
        }
    }

    private void checkDateReceivedNotNull() throws BiobankCheckException {
        if (getDateReceived() == null)
            throw new BiobankCheckException(
                "'Date Received' is a required field. You must set this value before saving a shipment.");
    }

    public void checkCanRemovePatient(PatientWrapper patient)
        throws BiobankCheckException {
        if (hasVisitForPatient(patient)) {
            throw new BiobankCheckException("Cannot remove patient "
                + patient.getPnumber()
                + ": a visit related to this shipment has "
                + "already been created. Remove visit first "
                + "and then you will be able to remove this "
                + "patient from this shipment.");
        }
    }

    public boolean hasVisitForPatient(PatientWrapper patient) {
        List<PatientVisitWrapper> pvs = patient.getPatientVisitCollection();
        if (pvs != null)
            for (PatientVisitWrapper pv : pvs)
                if (pv.getShipment() != null && pv.getShipment().equals(this))
                    return true;
        return false;
    }

    public void checkAlLeastOnePatient() throws BiobankCheckException {
        List<PatientWrapper> patients = getPatientCollection();
        if (patients == null || patients.size() == 0) {
            throw new BiobankCheckException(
                "At least one patient should be added to this shipment");
        }
    }

    public void checkPatientsStudy() throws BiobankCheckException,
        ApplicationException {
        String patientsInError = "";
        for (PatientWrapper patient : patientsAdded) {
            if (!patient.canBeAddedToShipment(this)) {
                patientsInError += patient.getPnumber() + ", ";
            }
        }
        if (!patientsInError.isEmpty()) {
            // remove last ", "
            patientsInError = patientsInError.substring(0,
                patientsInError.length() - 2);
            throw new BiobankCheckException("Patient(s) " + patientsInError
                + " are not part of a study that has contact with clinic "
                + getClinic().getName());
        }
    }

    private boolean checkWaybillUniqueForClinic() throws ApplicationException {
        String isSameShipment = "";
        List<Object> params = new ArrayList<Object>();
        params.add(getClinic().getId());
        params.add(getWaybill());
        if (!isNew()) {
            isSameShipment = " and id <> ?";
            params.add(getId());
        }
        HQLCriteria c = new HQLCriteria("from " + Shipment.class.getName()
            + " where clinic.id=? and waybill = ?" + isSameShipment, params);

        List<Object> results = appService.query(c);
        return results.size() == 0;
    }

    @Override
    public int compareTo(ModelWrapper<Shipment> wrapper) {
        if (wrapper instanceof ShipmentWrapper) {
            Date v1Date = wrappedObject.getDateShipped();
            Date v2Date = wrapper.wrappedObject.getDateShipped();
            if (v1Date != null && v2Date != null) {
                return v1Date.compareTo(v2Date);
            }
        }
        return 0;
    }

    public Date getDateShipped() {
        return wrappedObject.getDateShipped();
    }

    public String getFormattedDateShipped() {
        return DateFormatter.formatAsDateTime(getDateShipped());
    }

    public void setDateShipped(Date date) {
        Date oldDate = getDateShipped();
        wrappedObject.setDateShipped(date);
        propertyChangeSupport.firePropertyChange("dateShipped", oldDate, date);
    }

    public Date getDateReceived() {
        return wrappedObject.getDateReceived();
    }

    public String getFormattedDateReceived() {
        return DateFormatter.formatAsDateTime(getDateReceived());
    }

    public void setDateReceived(Date date) {
        Date oldDate = getDateReceived();
        wrappedObject.setDateReceived(date);
        propertyChangeSupport.firePropertyChange("dateReceived", oldDate, date);
    }

    public ClinicWrapper getClinic() {
        if (clinic == null) {
            Clinic c = wrappedObject.getClinic();
            if (c == null)
                return null;
            clinic = new ClinicWrapper(appService, c);
        }
        return clinic;
    }

    protected void setClinic(Clinic clinic) {
        if (clinic == null)
            this.clinic = null;
        else
            this.clinic = new ClinicWrapper(appService, clinic);
        Clinic oldClinic = wrappedObject.getClinic();
        wrappedObject.setClinic(clinic);
        propertyChangeSupport.firePropertyChange("clinic", oldClinic, clinic);
    }

    public void setClinic(ClinicWrapper clinic) {
        if (clinic == null) {
            setClinic((Clinic) null);
        } else {
            setClinic(clinic.wrappedObject);
        }
    }

    public SiteWrapper getSite() {
        Site site = wrappedObject.getSite();
        return (site != null) ? new SiteWrapper(appService, site) : null;
    }

    public void setSite(SiteWrapper siteWrapper) {
        Site oldSite = wrappedObject.getSite();
        Site newSite = siteWrapper.getWrappedObject();
        wrappedObject.setSite(newSite);
        propertyChangeSupport.firePropertyChange("site", oldSite, newSite);
    }

    @SuppressWarnings("unchecked")
    public List<PatientVisitWrapper> getPatientVisitCollection() {
        List<PatientVisitWrapper> patientVisitCollection = (List<PatientVisitWrapper>) propertiesMap
            .get("patientVisitCollection");
        if (patientVisitCollection == null) {
            Collection<PatientVisit> children = wrappedObject
                .getPatientVisitCollection();
            if (children != null) {
                patientVisitCollection = new ArrayList<PatientVisitWrapper>();
                for (PatientVisit pv : children) {
                    patientVisitCollection.add(new PatientVisitWrapper(
                        appService, pv));
                }
                propertiesMap.put("patientVisitCollection",
                    patientVisitCollection);
            }
        }
        return patientVisitCollection;
    }

    private void setPatientVisitCollection(
        Collection<PatientVisit> allVisitObjects,
        List<PatientVisitWrapper> allVisitWrappers) {
        Collection<PatientVisit> oldCollection = wrappedObject
            .getPatientVisitCollection();
        wrappedObject.setPatientVisitCollection(allVisitObjects);
        propertyChangeSupport.firePropertyChange("patientVisitCollection",
            oldCollection, allVisitObjects);
        propertiesMap.put("patientVisitCollection", allVisitWrappers);
    }

    public void addPatientVisits(List<PatientVisitWrapper> newPatientVisits) {
        if (newPatientVisits != null && newPatientVisits.size() > 0) {
            Collection<PatientVisit> allVisitObjects = new HashSet<PatientVisit>();
            List<PatientVisitWrapper> allVisitWrappers = new ArrayList<PatientVisitWrapper>();
            // already added visits
            List<PatientVisitWrapper> currentList = getPatientVisitCollection();
            if (currentList != null) {
                for (PatientVisitWrapper visit : currentList) {
                    allVisitObjects.add(visit.getWrappedObject());
                    allVisitWrappers.add(visit);
                }
            }
            // new
            for (PatientVisitWrapper visit : newPatientVisits) {
                allVisitObjects.add(visit.getWrappedObject());
                allVisitWrappers.add(visit);
            }
            setPatientVisitCollection(allVisitObjects, allVisitWrappers);
        }
    }

    public String getComment() {
        return wrappedObject.getComment();
    }

    public void setComment(String comment) {
        String oldComment = getComment();
        wrappedObject.setComment(comment);
        propertyChangeSupport
            .firePropertyChange("comment", oldComment, comment);
    }

    public String getWaybill() {
        return wrappedObject.getWaybill();
    }

    public void setWaybill(String waybill) {
        String old = getWaybill();
        wrappedObject.setWaybill(waybill);
        propertyChangeSupport.firePropertyChange("waybill", old, waybill);
    }

    public String getBoxNumber() {
        return wrappedObject.getBoxNumber();
    }

    public void setBoxNumber(String boxNumber) {
        String old = getBoxNumber();
        wrappedObject.setBoxNumber(boxNumber);
        propertyChangeSupport.firePropertyChange("boxNumber", old, boxNumber);
    }

    public ShippingMethodWrapper getShippingMethod() {
        ShippingMethod sc = wrappedObject.getShippingMethod();
        if (sc == null) {
            return null;
        }
        return new ShippingMethodWrapper(appService, sc);
    }

    protected void setShippingMethod(ShippingMethod sc) {
        ShippingMethod old = wrappedObject.getShippingMethod();
        wrappedObject.setShippingMethod(sc);
        propertyChangeSupport.firePropertyChange("shippingMethod", old, sc);
    }

    public void setShippingMethod(ShippingMethodWrapper sc) {
        if (sc == null) {
            setShippingMethod((ShippingMethod) null);
        } else {
            setShippingMethod(sc.wrappedObject);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PatientWrapper> getPatientCollection(boolean sort) {
        List<PatientWrapper> patientCollection = (List<PatientWrapper>) propertiesMap
            .get("patientCollection");
        if (patientCollection == null) {
            Collection<Patient> children = wrappedObject.getPatientCollection();
            if (children != null) {
                patientCollection = new ArrayList<PatientWrapper>();
                for (Patient patient : children) {
                    patientCollection.add(new PatientWrapper(appService,
                        patient));
                }
                propertiesMap.put("patientCollection", patientCollection);
            }
        }
        if ((patientCollection != null) && sort)
            Collections.sort(patientCollection);
        return patientCollection;
    }

    public List<PatientWrapper> getPatientCollection() {
        return getPatientCollection(true);
    }

    private void setPatients(Collection<Patient> allPatientObjects,
        List<PatientWrapper> allPatientWrappers) {
        Collection<Patient> oldPatients = wrappedObject.getPatientCollection();
        wrappedObject.setPatientCollection(allPatientObjects);
        propertyChangeSupport.firePropertyChange("patientCollection",
            oldPatients, allPatientObjects);
        propertiesMap.put("patientCollection", allPatientWrappers);
    }

    public void addPatients(List<PatientWrapper> newPatients) {
        if (newPatients != null && newPatients.size() > 0) {
            Collection<Patient> allPatientsObjects = new HashSet<Patient>();
            List<PatientWrapper> allPatientsWrappers = new ArrayList<PatientWrapper>();
            // already in list
            List<PatientWrapper> patientsList = getPatientCollection();
            if (patientsList != null) {
                for (PatientWrapper patient : patientsList) {
                    allPatientsObjects.add(patient.getWrappedObject());
                    allPatientsWrappers.add(patient);
                }
            }
            // new patients
            for (PatientWrapper patient : newPatients) {
                patientsAdded.add(patient);
                patientsRemoved.remove(patient);
                allPatientsObjects.add(patient.getWrappedObject());
                allPatientsWrappers.add(patient);
            }
            setPatients(allPatientsObjects, allPatientsWrappers);
        }
    }

    public void removePatients(List<PatientWrapper> patientsToRemove) {
        if (patientsToRemove != null && patientsToRemove.size() > 0) {
            patientsAdded.removeAll(patientsToRemove);
            patientsRemoved.addAll(patientsToRemove);
            Collection<Patient> allPatientsObjects = new HashSet<Patient>();
            List<PatientWrapper> allPatientsWrappers = new ArrayList<PatientWrapper>();
            // already in list
            List<PatientWrapper> patientsList = getPatientCollection();
            if (patientsList != null) {
                for (PatientWrapper patient : patientsList) {
                    if (!patientsToRemove.contains(patient)) {
                        allPatientsObjects.add(patient.getWrappedObject());
                        allPatientsWrappers.add(patient);
                    }
                }
            }
            setPatients(allPatientsObjects, allPatientsWrappers);
        }
    }

    @Override
    public String toString() {
        String s = getFormattedDateReceived();
        if (getWaybill() != null) {
            s += " (" + getWaybill() + ")";
        }
        return s;
    }

    /**
     * Search for shipments in the site with the given waybill
     */
    public static List<ShipmentWrapper> getShipmentsInSite(
        WritableApplicationService appService, String waybill, SiteWrapper site)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria("from "
            + Shipment.class.getName()
            + " where clinic.site.id = ? and waybill = ?",
            Arrays.asList(new Object[] { site.getId(), waybill }));
        List<Shipment> shipments = appService.query(criteria);
        List<ShipmentWrapper> wrappers = new ArrayList<ShipmentWrapper>();
        for (Shipment s : shipments) {
            wrappers.add(new ShipmentWrapper(appService, s));
        }
        return wrappers;
    }

    /**
     * Search for shipments in the site with the given date received. Don't use
     * hour and minute.
     */
    public static List<ShipmentWrapper> getShipmentsInSite(
        WritableApplicationService appService, Date dateReceived,
        SiteWrapper site) throws ApplicationException {
        Calendar cal = Calendar.getInstance();
        // date at 0:0am
        cal.setTime(dateReceived);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        // date at 0:0pm
        cal.add(Calendar.DATE, 1);
        Date endDate = cal.getTime();
        HQLCriteria criteria = new HQLCriteria(
            "from "
                + Shipment.class.getName()
                + " where clinic.site.id = ? and dateReceived >= ? and dateReceived <= ?",
            Arrays.asList(new Object[] { site.getId(), startDate, endDate }));
        List<Shipment> shipments = appService.query(criteria);
        List<ShipmentWrapper> wrappers = new ArrayList<ShipmentWrapper>();
        for (Shipment s : shipments) {
            wrappers.add(new ShipmentWrapper(appService, s));
        }
        return wrappers;
    }

    /**
     */
    public boolean hasPatient(String patientNumber) {
        List<PatientWrapper> ps = getPatientCollection();
        if (ps != null)
            for (PatientWrapper p : ps)
                if (p.getPnumber().equals(patientNumber))
                    return true;
        return false;
    }

    @Override
    public void resetInternalField() {
        patientsAdded.clear();
        patientsRemoved.clear();
        clinic = null;
    }

    public static List<ShipmentWrapper> getTodayShipments(
        WritableApplicationService appService, SiteWrapper site)
        throws ApplicationException {
        Calendar cal = Calendar.getInstance();
        // yesterday midnight
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        // today midnight
        cal.add(Calendar.DATE, 1);
        Date endDate = cal.getTime();
        HQLCriteria criteria = new HQLCriteria(
            "from "
                + Shipment.class.getName()
                + " where clinic.site.id = ? and dateReceived >= ? and dateReceived <= ?",
            Arrays.asList(new Object[] { site.getId(), startDate, endDate }));
        List<Shipment> res = appService.query(criteria);
        List<ShipmentWrapper> ships = new ArrayList<ShipmentWrapper>();
        for (Shipment s : res) {
            ships.add(new ShipmentWrapper(appService, s));
        }
        return ships;
    }

    public boolean isReceivedToday() {
        Calendar cal = Calendar.getInstance();
        // yesterday midnight
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        // today midnight
        cal.add(Calendar.DATE, 1);
        Date endDate = cal.getTime();
        Date dateReveived = getDateReceived();
        return dateReveived.compareTo(startDate) >= 0
            && dateReveived.compareTo(endDate) <= 0;
    }

    @Override
    protected Log getLogMessage(String action, String site, String details) {
        Log log = new Log();
        log.setAction(action);
        ClinicWrapper clinic = getClinic();
        if (site == null) {
            log.setSite(clinic.getSite().getNameShort());
        } else {
            log.setSite(site);
        }
        details += "Received:" + getFormattedDateReceived();
        String waybill = getWaybill();
        if (waybill != null) {
            details += " - Waybill:" + waybill;
        }
        log.setDetails(details);
        log.setType("Shipment");
        return log;
    }

}
