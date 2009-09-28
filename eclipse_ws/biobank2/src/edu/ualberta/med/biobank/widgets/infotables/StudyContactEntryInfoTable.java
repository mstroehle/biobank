package edu.ualberta.med.biobank.widgets.infotables;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.model.Contact;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.model.StudyContactInfo;

public class StudyContactEntryInfoTable extends InfoTableWidget<Contact> {

    private static final String[] HEADINGS = new String[] { "Clinic",
        "Contact Name", "Title", "Email", "Phone #", "Fax #" };

    private static final int[] BOUNDS = new int[] { 150, 150, 100, 100, 100,
        100 };

    public StudyContactEntryInfoTable(Composite parent, Study study) {
        super(parent, null, HEADINGS, BOUNDS);
        Collection<Contact> collection = study.getContactCollection();
        for (int i = 0, n = collection.size(); i < n; ++i) {
            model.add(new BiobankCollectionModel());
        }
        getTableViewer().refresh();
        setCollection(collection);
    }

    @Override
    public void setCollection(final Collection<Contact> collection) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    final TableViewer viewer = getTableViewer();
                    Display display = viewer.getTable().getDisplay();
                    int count = 0;

                    if (collection != null)
                        if (model.size() != collection.size()) {
                            model.clear();
                            for (int i = 0, n = collection.size(); i < n; ++i) {
                                model.add(new BiobankCollectionModel());
                            }
                            display.asyncExec(new Runnable() {
                                public void run() {
                                    if (!viewer.getTable().isDisposed())
                                        getTableViewer().refresh();
                                }
                            });
                        }

                    for (Contact contact : collection) {
                        if (getTableViewer().getTable().isDisposed()) {
                            return;
                        }
                        final BiobankCollectionModel item = model.get(count);
                        StudyContactInfo info = new StudyContactInfo();
                        item.o = info;
                        model.add(item);
                        info.contact = contact;

                        display.asyncExec(new Runnable() {
                            public void run() {
                                if (!viewer.getTable().isDisposed())
                                    viewer.refresh(item, false);
                            }
                        });
                        ++count;
                    }
                } catch (final RemoteConnectFailureException exp) {
                    BioBankPlugin.openRemoteConnectErrorMessage();
                } catch (Exception e) {
                    SessionManager.getLogger().error(
                        "Error while retrieving the clinic", e);
                }
            }
        };
        t.start();
    }

    @Override
    public Collection<Contact> getCollection() {
        Collection<Contact> collection = new HashSet<Contact>();
        for (BiobankCollectionModel item : model) {
            collection.add(((StudyContactInfo) item.o).contact);
        }
        return collection;
    }
}
