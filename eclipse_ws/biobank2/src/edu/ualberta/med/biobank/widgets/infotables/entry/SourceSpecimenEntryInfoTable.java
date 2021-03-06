package edu.ualberta.med.biobank.widgets.infotables.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.SourceSpecimenWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenTypeWrapper;
import edu.ualberta.med.biobank.dialogs.PagedDialog.NewListener;
import edu.ualberta.med.biobank.dialogs.StudySourceSpecimenDialog;
import edu.ualberta.med.biobank.gui.common.BgcLogger;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableAddItemListener;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableDeleteItemListener;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableEditItemListener;
import edu.ualberta.med.biobank.gui.common.widgets.InfoTableEvent;
import edu.ualberta.med.biobank.widgets.infotables.BiobankTableSorter;
import edu.ualberta.med.biobank.widgets.infotables.SourceSpecimenInfoTable;

public class SourceSpecimenEntryInfoTable extends SourceSpecimenInfoTable {
    public static final I18n i18n = I18nFactory
        .getI18n(SourceSpecimenEntryInfoTable.class);

    @SuppressWarnings("unused")
    private static BgcLogger LOGGER = BgcLogger
        .getLogger(SourceSpecimenEntryInfoTable.class.getName());

    private List<SpecimenTypeWrapper> availableSpecimenTypes;

    private List<SourceSpecimenWrapper> addedOrModifiedSourceSpecimens;

    private List<SourceSpecimenWrapper> deletedSourceSpecimen;

    private StudySourceSpecimenDialog dlg;

    public SourceSpecimenEntryInfoTable(Composite parent,
        List<SourceSpecimenWrapper> collection) {
        super(parent, collection);
    }

    /**
     * 
     * @param parent a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param study the study the source specimens belong to.
     */
    public SourceSpecimenEntryInfoTable(Composite parent,
        List<SourceSpecimenWrapper> sourceSpecimens,
        List<SpecimenTypeWrapper> specimenTypes) {
        super(parent, null);

        availableSpecimenTypes = specimenTypes;
        if (sourceSpecimens == null) {
            sourceSpecimens = new ArrayList<SourceSpecimenWrapper>();
        }
        for (SourceSpecimenWrapper ss : sourceSpecimens)
            availableSpecimenTypes.remove(ss.getSpecimenType());

        setList(sourceSpecimens);
        addedOrModifiedSourceSpecimens = new ArrayList<SourceSpecimenWrapper>();
        deletedSourceSpecimen = new ArrayList<SourceSpecimenWrapper>();

        setLayout(new GridLayout(1, false));
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addEditSupport();
    }

    private void addEditSupport() {
        addAddItemListener(new IInfoTableAddItemListener<SourceSpecimenWrapper>() {
            @Override
            public void addItem(InfoTableEvent<SourceSpecimenWrapper> event) {
                addSourceSpecimen();
            }
        });

        addEditItemListener(new IInfoTableEditItemListener<SourceSpecimenWrapper>() {
            @Override
            public void editItem(InfoTableEvent<SourceSpecimenWrapper> event) {
                SourceSpecimenWrapper sourceSpecimen = getSelection();
                if (sourceSpecimen != null)
                    addOrEditStudySourceSpecimen(false, sourceSpecimen);
            }
        });

        addDeleteItemListener(new IInfoTableDeleteItemListener<SourceSpecimenWrapper>() {
            @SuppressWarnings("nls")
            @Override
            public void deleteItem(
                InfoTableEvent<SourceSpecimenWrapper> event) {
                SourceSpecimenWrapper sourceSpecimen = getSelection();
                if (sourceSpecimen != null) {
                    if (!MessageDialog
                        .openConfirm(
                            PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getShell(),
                            // dialog title.
                            i18n.tr("Delete source specimen"),
                            // dialog message.
                            i18n.tr("Are you sure you want to delete this source specimen?"))) {
                        return;
                    }

                    getList().remove(sourceSpecimen);
                    deletedSourceSpecimen.add(sourceSpecimen);
                    availableSpecimenTypes.add(sourceSpecimen
                        .getSpecimenType());
                    notifyListeners();
                }
            }
        });
    }

    @Override
    protected boolean isEditMode() {
        return true;
    }

    public void addSourceSpecimen() {
        SourceSpecimenWrapper newSourceSpecimen = new SourceSpecimenWrapper(
            SessionManager.getAppService());
        // DO NOT set the study on newSourceSpecimen - if it was done
        // then it would have to be unset if the user presses the Cancel button
        addOrEditStudySourceSpecimen(true, newSourceSpecimen);
    }

    private void addOrEditStudySourceSpecimen(final boolean add,
        final SourceSpecimenWrapper sourceSpecimen) {
        List<SpecimenTypeWrapper> dialogSpecimenTypes;
        if (!add) {
            dialogSpecimenTypes =
                Arrays.asList(sourceSpecimen.getSpecimenType());
        } else
            dialogSpecimenTypes = availableSpecimenTypes;

        NewListener newListener = null;
        if (add) {
            // only add to the collection when adding and not editing
            newListener = new NewListener() {
                @Override
                public void newAdded(Object spec) {
                    SourceSpecimenWrapper ss = (SourceSpecimenWrapper) spec;
                    availableSpecimenTypes.remove(ss.getSpecimenType());
                    dlg.setSpecimenTypes(availableSpecimenTypes);
                    getList().add(ss);
                    addedOrModifiedSourceSpecimens.add(ss);
                    notifyListeners();
                }
            };
        }

        dlg = new StudySourceSpecimenDialog(
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            sourceSpecimen.getNeedOriginalVolume(), sourceSpecimen
                .getSpecimenType(), dialogSpecimenTypes, newListener);

        int res = dlg.open();
        if (res == Dialog.OK) {
            sourceSpecimen.setNeedOriginalVolume(dlg.getNeedOriginalVolume());
            sourceSpecimen.setSpecimenType(dlg.getSpecimenType());

            if (!add) {
                notifyListeners();
            }
        }
    }

    public List<SourceSpecimenWrapper> getAddedOrModifiedSourceSpecimens() {
        return addedOrModifiedSourceSpecimens;
    }

    public List<SourceSpecimenWrapper> getDeletedSourceSpecimens() {
        return deletedSourceSpecimen;
    }

    public void reload(List<SourceSpecimenWrapper> sourceSpecimens) {
        if (sourceSpecimens == null) {
            sourceSpecimens = new ArrayList<SourceSpecimenWrapper>();
        }
        setList(sourceSpecimens);
        addedOrModifiedSourceSpecimens = new ArrayList<SourceSpecimenWrapper>();
        deletedSourceSpecimen = new ArrayList<SourceSpecimenWrapper>();
    }

    @SuppressWarnings("serial")
    @Override
    protected BiobankTableSorter getComparator() {
        return new BiobankTableSorter() {
            @Override
            public int compare(Object e1, Object e2) {
                try {
                    TableRowData i1 = getCollectionModelObject(e1);
                    TableRowData i2 = getCollectionModelObject(e2);
                    return super.compare(i1.name, i2.name);
                } catch (Exception e) {
                    return 0;
                }
            }
        };
    }

}
