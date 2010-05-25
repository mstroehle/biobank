package edu.ualberta.med.biobank.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.PvSourceVesselWrapper;
import edu.ualberta.med.biobank.common.wrappers.SourceVesselWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudySourceVesselWrapper;
import edu.ualberta.med.biobank.validators.IntegerNumberValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.infotables.entry.PvSourceVesselEntryInfoTable;

public class PvSourceVesselDialog extends BiobankDialog {

    private static final String TITLE = "Source Vessel";

    private PvSourceVesselWrapper editedSourceVessel;

    private PvSourceVesselWrapper internalSourceVessel;

    private ComboViewer sourceVesselsComboViewer;

    private Map<String, StudySourceVesselWrapper> mapStudySourceVessel;

    private DateTimeWidget timeDrawnWidget;

    private Control volumeText;

    private List<SourceVesselWrapper> allSourceVessels;

    private Label timeDrawnLabel;

    private Label volumeLabel;

    private PvSourceVesselEntryInfoTable infotable;

    private PatientVisitWrapper patientVisit;

    private boolean addMode;

    public PvSourceVesselDialog(Shell parent,
        PvSourceVesselWrapper pvSourceVessel,
        List<StudySourceVesselWrapper> studySourceVessels,
        List<SourceVesselWrapper> allSourceVessels,
        PvSourceVesselEntryInfoTable infoTable) {
        super(parent);
        Assert.isNotNull(studySourceVessels);
        internalSourceVessel = new PvSourceVesselWrapper(SessionManager
            .getAppService());
        if (pvSourceVessel == null) {
            addMode = true;
        } else {
            internalSourceVessel.setSourceVessel(pvSourceVessel
                .getSourceVessel());
            internalSourceVessel.setQuantity(pvSourceVessel.getQuantity());
            internalSourceVessel.setVolume(pvSourceVessel.getVolume());
            internalSourceVessel.setTimeDrawn(pvSourceVessel.getTimeDrawn());
            editedSourceVessel = pvSourceVessel;
            addMode = false;
        }
        mapStudySourceVessel = new HashMap<String, StudySourceVesselWrapper>();
        for (StudySourceVesselWrapper ssv : studySourceVessels) {
            mapStudySourceVessel.put(ssv.getSourceVessel().getName(), ssv);
        }
        this.allSourceVessels = allSourceVessels;
        this.infotable = infoTable;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        String title = new String();
        if (addMode) {
            title = "Add ";
        } else {
            title = "Edit ";
        }
        title += TITLE;
        shell.setText(title);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitleImage(BioBankPlugin.getDefault().getImageRegistry().get(
            BioBankPlugin.IMG_COMPUTER_KEY));
        if (addMode) {
            setTitle("Add Source Vessel");
            setMessage("Add a source vessel to a patient visit");
        } else {
            setTitle("Edit Source Vessel");
            setMessage("Edit a source vessel in a patient visit");
        }
        return contents;
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(3, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        boolean useStudyOnlySourceVessels = true;
        StudySourceVesselWrapper ssv = null;
        SourceVesselWrapper currentSourceVessel = internalSourceVessel
            .getSourceVessel();
        if (currentSourceVessel != null) {
            ssv = mapStudySourceVessel.get(currentSourceVessel.getName());
        }
        if (ssv == null && currentSourceVessel != null
            && allSourceVessels.contains(currentSourceVessel)) {
            useStudyOnlySourceVessels = false;
        }
        sourceVesselsComboViewer = getWidgetCreator()
            .createComboViewerWithNoSelectionValidator(contents,
                "Source Vessel", mapStudySourceVessel.values(), ssv,
                "A source vessel should be selected");
        if (!useStudyOnlySourceVessels) {
            sourceVesselsComboViewer.setInput(allSourceVessels);
            sourceVesselsComboViewer.setSelection(new StructuredSelection(
                currentSourceVessel));
        }
        sourceVesselsComboViewer
            .addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    Object selection = ((IStructuredSelection) sourceVesselsComboViewer
                        .getSelection()).getFirstElement();
                    if (selection instanceof StudySourceVesselWrapper) {
                        internalSourceVessel
                            .setSourceVessel(((StudySourceVesselWrapper) selection)
                                .getSourceVessel());
                    } else {
                        internalSourceVessel
                            .setSourceVessel((SourceVesselWrapper) selection);
                    }
                    updateWidgetVisibilityAndValues();
                }
            });

        final Button allSourceVesselCheckBox = new Button(contents, SWT.CHECK);
        allSourceVesselCheckBox.setText("Show only study source vessels");
        allSourceVesselCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (allSourceVesselCheckBox.getSelection()) {
                    sourceVesselsComboViewer.setInput(mapStudySourceVessel
                        .values());
                } else {
                    sourceVesselsComboViewer.setInput(allSourceVessels);
                }
            }
        });
        allSourceVesselCheckBox.setSelection(useStudyOnlySourceVessels);

        BiobankText quantityText = (BiobankText) createBoundWidgetWithLabel(
            contents, BiobankText.class, SWT.BORDER, "Quantity", new String[0],
            BeansObservables.observeValue(internalSourceVessel, "quantity"),
            new IntegerNumberValidator("quantity should be a whole number",
                false));
        GridData gd = (GridData) quantityText.getLayoutData();
        gd.horizontalSpan = 2;

        timeDrawnLabel = widgetCreator.createLabel(contents, "Time drawn");
        timeDrawnWidget = widgetCreator.createDateTimeWidget(contents,
            timeDrawnLabel, internalSourceVessel.getTimeDrawn(),
            BeansObservables.observeValue(internalSourceVessel, "timeDrawn"),
            null, SWT.TIME);
        gd = (GridData) timeDrawnWidget.getLayoutData();
        gd.horizontalSpan = 2;

        volumeLabel = widgetCreator.createLabel(contents, "Volume (ml)");
        volumeLabel.setLayoutData(new GridData(
            GridData.VERTICAL_ALIGN_BEGINNING));
        volumeText = widgetCreator
            .createBoundWidget(contents, BiobankText.class, SWT.BORDER,
                volumeLabel, new String[0], BeansObservables.observeValue(
                    internalSourceVessel, "volume"), null);
        gd = (GridData) volumeText.getLayoutData();
        gd.horizontalSpan = 2;

        updateWidgetVisibilityAndValues();
    }

    public void updateWidgetVisibilityAndValues() {
        StudySourceVesselWrapper ssv = null;
        SourceVesselWrapper currentSourceVessel = internalSourceVessel
            .getSourceVessel();
        if (currentSourceVessel != null) {
            ssv = mapStudySourceVessel.get(currentSourceVessel.getName());
        }
        boolean enableTimeDrawn = (currentSourceVessel != null)
            && (ssv == null || Boolean.TRUE.equals(ssv.getNeedTimeDrawn()));
        boolean enableVolume = (currentSourceVessel != null)
            && (ssv == null || Boolean.TRUE.equals(ssv.getNeedOriginalVolume()));
        timeDrawnLabel.setVisible(enableTimeDrawn);
        timeDrawnWidget.setVisible(enableTimeDrawn);
        volumeLabel.setVisible(enableVolume);
        volumeText.setVisible(enableVolume);
        if (!enableTimeDrawn) {
            internalSourceVessel.setTimeDrawn(null);
        }
        if (!enableVolume) {
            internalSourceVessel.setVolume(null);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (addMode) {
            createButton(parent, IDialogConstants.FINISH_ID,
                IDialogConstants.FINISH_LABEL, false);
            createButton(parent, IDialogConstants.NEXT_ID,
                IDialogConstants.NEXT_LABEL, true);
        } else {
            super.createButtonsForButtonBar(parent);
        }
    }

    @Override
    protected void setOkButtonEnabled(boolean enabled) {
        if (addMode) {
            Button nextButton = getButton(IDialogConstants.NEXT_ID);
            if (nextButton != null && !nextButton.isDisposed()) {
                nextButton.setEnabled(enabled);
            } else {
                okButtonEnabled = enabled;
            }
        } else {
            super.setOkButtonEnabled(enabled);
        }
    }

    @Override
    protected void okPressed() {
        editedSourceVessel.setSourceVessel(internalSourceVessel
            .getSourceVessel());
        editedSourceVessel.setQuantity(internalSourceVessel.getQuantity());
        editedSourceVessel.setVolume(internalSourceVessel.getVolume());
        editedSourceVessel.setTimeDrawn(internalSourceVessel.getTimeDrawn());
        super.okPressed();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (addMode) {
            if (IDialogConstants.FINISH_ID == buttonId) {
                Button nextButton = getButton(IDialogConstants.NEXT_ID);
                if (nextButton.isEnabled()) {
                    addNewPvSourceVessel();
                }
                setReturnCode(OK);
                close();
            } else if (IDialogConstants.NEXT_ID == buttonId) {
                addNewPvSourceVessel();
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }

    private void addNewPvSourceVessel() {
        try {
            PvSourceVesselWrapper newPvSourceVessel = new PvSourceVesselWrapper(
                SessionManager.getAppService());
            newPvSourceVessel.initObjectWith(internalSourceVessel);
            newPvSourceVessel.setPatientVisit(patientVisit);
            infotable.addPvSourceVessel(newPvSourceVessel);
            internalSourceVessel.reset();
            sourceVesselsComboViewer.getCombo().deselectAll();
            sourceVesselsComboViewer.getCombo().setFocus();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setPatientVisit(PatientVisitWrapper patientVisit) {
        this.patientVisit = patientVisit;
    }
}
