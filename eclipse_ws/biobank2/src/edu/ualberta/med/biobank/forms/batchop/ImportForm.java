package edu.ualberta.med.biobank.forms.batchop;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.batchoperation.BatchOpActionUtil;
import edu.ualberta.med.biobank.common.action.batchoperation.IBatchOpInputPojo;
import edu.ualberta.med.biobank.common.action.exception.AccessDeniedException;
import edu.ualberta.med.biobank.common.action.exception.BatchOpErrorsException;
import edu.ualberta.med.biobank.common.action.exception.BatchOpException;
import edu.ualberta.med.biobank.common.batchoperation.ClientBatchOpErrorsException;
import edu.ualberta.med.biobank.common.batchoperation.ClientBatchOpInputErrorList;
import edu.ualberta.med.biobank.common.batchoperation.IBatchOpPojoReader;
import edu.ualberta.med.biobank.common.util.Holder;
import edu.ualberta.med.biobank.forms.BiobankViewForm;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.FileBrowser;
import edu.ualberta.med.biobank.gui.common.widgets.IBgcFileBrowserListener;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.widgets.infotables.BatchOpExceptionTable;

public abstract class ImportForm extends BiobankViewForm {
    private static final I18n i18n = I18nFactory.getI18n(ImportForm.class);

    private final String formTitle;

    private FileBrowser fileBrowser;
    private Button importButton;
    private BatchOpExceptionTable errorsTable;
    private Label errorsLabel;
    private Composite client;

    public ImportForm(String formTitle) {
        this.formTitle = formTitle;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    protected Image getFormImage() {
        return BgcPlugin.getDefault().getImage(BgcPlugin.Image.DATABASE_GO);
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText(formTitle);
        page.setLayout(new GridLayout(1, false));

        client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        createFileBrowser(client);
        createImportButton(client);
    }

    @SuppressWarnings("nls")
    private void createFileBrowser(Composite parent) {
        final String[] extensions = new String[] { "*.csv", "*.*" };

        widgetCreator.createLabel(parent, i18n.tr("CSV File"));

        fileBrowser = new FileBrowser(parent, SWT.NONE, extensions);
        fileBrowser.addFileSelectedListener(new IBgcFileBrowserListener() {
            @Override
            public void fileSelected(String filename) {
                if (importButton != null && !importButton.isDisposed()) {
                    boolean enabled = filename != null && !filename.isEmpty();
                    importButton.setEnabled(enabled);
                }
            }
        });
        toolkit.adapt(fileBrowser);
    }

    @SuppressWarnings("nls")
    private void createImportButton(Composite parent) {
        // take up a cell.
        new Label(parent, SWT.NONE);

        importButton = toolkit.createButton(parent, i18n.tr("Import"), SWT.NONE);
        importButton.setEnabled(false);
        importButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doImport();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    @SuppressWarnings("nls")
    private void createErrorsTable(Composite parent,
        List<BatchOpException<?>> errors) {
        if (errorsTable != null) errorsTable.dispose();
        if (errorsLabel != null) errorsLabel.dispose();
        if (errors == null || errors.isEmpty()) return;

        // TODO: add a label saying there were errors

        errorsLabel = new Label(parent, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        errorsLabel.setLayoutData(gd);

        errorsLabel.setText(i18n.tr("The following errors occurred when attempting to import:"));
        errorsLabel.setBackground(toolkit.getColors().getBackground());

        errorsTable = new BatchOpExceptionTable(parent, errors);
        gd = (GridData) errorsTable.getLayoutData();
        gd.horizontalSpan = 2;
        errorsTable.adaptToToolkit(toolkit, false);
        book.reflow(true);
        form.layout(true, true);
    }

    @Override
    public void setValues() throws Exception {
        fileBrowser.reset();
    }

    @SuppressWarnings("nls")
    private void doImport() {
        final String filename = fileBrowser.getFilePath();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(i18n.tr("Importing Specimens..."), IProgressMonitor.UNKNOWN);

                final List<BatchOpException<?>> errors = new ArrayList<BatchOpException<?>>();
                final Holder<Boolean> success = new Holder<Boolean>(false);
                final Holder<Integer> batchOpId = new Holder<Integer>(null);

                try {
                    monitor.beginTask(i18n.tr("Processing file..."), IProgressMonitor.UNKNOWN);
                    batchOpId.setValue(processFile(filename));
                    success.setValue(true);
                } catch (ClientBatchOpErrorsException e) {
                    errors.addAll(e.getErrors());
                } catch (BatchOpErrorsException e) {
                    errors.addAll(e.getErrors());
                } catch (AccessDeniedException e) {
                    throw new RuntimeException(
                        i18n.tr("You don't have permission to do this."));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    fileBrowser.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (success.getValue()) {
                                AdapterBase.closeEditor((FormInput) getEditorInput());
                                try {
                                    openForm(batchOpId.getValue(), true);
                                } catch (PartInitException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                updateErrorsTable(errors);
                            }
                        }
                    });
                }

                monitor.done();
            }
        };

        try {
            new ProgressMonitorDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell()).run(true, true, op);
        } catch (InvocationTargetException e) {
            BgcPlugin.openAsyncError(
                // dialog title.
                i18n.tr("Import Error"), e.getTargetException());
        } catch (InterruptedException e) {
            BgcPlugin.openAsyncError(
                // dialog title.
                i18n.tr("Import Error"), e);
        }
    }

    public abstract void openForm(Integer batchOpId, boolean focusOnEditor)
        throws PartInitException;

    private void updateErrorsTable(List<BatchOpException<?>> errors) {
        createErrorsTable(client, errors);
    }

    @SuppressWarnings("nls")
    protected Integer processFile(String filename) throws Exception {
        ICsvBeanReader reader = new CsvBeanReader(
            new FileReader(filename), CsvPreference.EXCEL_PREFERENCE);

        try {
            String[] csvHeaders = reader.getCSVHeader(true);

            if ((csvHeaders == null) || (csvHeaders.length < 1)) {
                throw new IllegalStateException(
                    i18n.tr("Invalid headers in CSV file."));
            }

            Center currentWorkingCenter = SessionManager.getUser()
                .getCurrentWorkingCenter().getWrappedObject();
            IBatchOpPojoReader<? extends IBatchOpInputPojo> pojoReader =
                getCsvPojoReader(currentWorkingCenter, filename, csvHeaders);

            if (pojoReader == null) {
                throw new ClientBatchOpErrorsException("invalid headers or number of columns in file");
            }

            pojoReader.readPojos(reader);
            ClientBatchOpInputErrorList errorList = pojoReader.getErrorList();

            if (!errorList.isEmpty()) {
                throw new ClientBatchOpErrorsException(errorList.getErrors());
            }

            Integer batchOpId = null;

            BiobankApplicationService service = SessionManager.getAppService();
            batchOpId = service.doAction(pojoReader.getAction()).getId();
            return batchOpId;
        } catch (SuperCSVException e) {
            throw new IllegalStateException(
                i18n.tr(BatchOpActionUtil.CSV_PARSE_ERROR, e.getMessage(),
                    e.getCsvContext()));
        } finally {
            reader.close();
        }
    }

    protected abstract IBatchOpPojoReader<? extends IBatchOpInputPojo> getCsvPojoReader(
        Center center, String csvFilename, String[] csvHeaders);
}
