package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.widgets.infotables.InfoTableSelection;
import edu.ualberta.med.biobank.widgets.utils.WidgetCreator;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

/**
 * Base class for data all BioBank2 view and entry forms. This class is the
 * superclass for {@link BiobankEntryForm} and {@link BiobankViewForm}. Please
 * extend from these two classes instead of <code>BiobankFormBase</code>.
 * <p>
 * Form creation is called in a non-UI thread so making calls to the ORM layer
 * possible. See {@link #createFormContent()}
 */
public abstract class BiobankFormBase extends EditorPart {

    private static BiobankLogger logger = BiobankLogger
        .getLogger(BiobankFormBase.class.getName());

    protected WritableApplicationService appService;

    protected AdapterBase adapter;

    protected ManagedForm mform;

    protected FormToolkit toolkit;

    protected ScrolledForm form;

    private Map<String, Control> widgets;

    protected WidgetCreator widgetCreator;

    public static List<BiobankFormBase> currentLinkedForms;

    public List<BiobankFormBase> linkedForms;

    protected IDoubleClickListener collectionDoubleClickListener = new IDoubleClickListener() {
        public void doubleClick(DoubleClickEvent event) {
            Object selection = event.getSelection();
            if (selection instanceof StructuredSelection) {
                Object element = ((StructuredSelection) selection)
                    .getFirstElement();
                if (element instanceof AdapterBase) {
                    ((AdapterBase) element).performDoubleClick();
                } else if (element instanceof ModelWrapper<?>) {
                    SessionManager.openViewForm((ModelWrapper<?>) element);
                }
            } else if (selection instanceof InfoTableSelection) {
                InfoTableSelection tableSelection = (InfoTableSelection) selection;
                if (tableSelection.getObject() instanceof ModelWrapper<?>) {
                    SessionManager
                        .openViewForm((ModelWrapper<?>) tableSelection
                            .getObject());
                }
            }
        }
    };

    public BiobankFormBase() {
        widgets = new HashMap<String, Control>();
        widgetCreator = new WidgetCreator(widgets);
    }

    protected void addWidget(String widgetName, Control widget) {
        widgets.put(widgetName, widget);
    }

    protected Control getWidget(String widgetName) {
        return widgets.get(widgetName);
    }

    @Override
    public void setFocus() {
        if (adapter.getId() != null) {
            SessionManager.setSelectedNode(adapter);
            // if selection fails, then the adapter needs to be matched at the
            // id level
            if (SessionManager.getSelectedNode() == null)
                SessionManager.setSelectedNode(SessionManager
                    .searchNode(adapter.getModelObject()));
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    /**
     * The initialisation method for the derived form.
     * 
     * @param adapter the corresponding model adapter the form is to edit /
     *            view.
     */
    protected abstract void init() throws Exception;

    @Override
    public void init(IEditorSite editorSite, IEditorInput input)
        throws PartInitException {
        if (!(input instanceof FormInput))
            throw new PartInitException("Invalid editor input");
        FormInput formInput = (FormInput) input;
        setSite(editorSite);
        setInput(input);
        adapter = formInput.getNode();
        Assert.isNotNull(adapter, "Bad editor input (null value)");
        appService = adapter.getAppService();
        if (formInput.hasPreviousForm()) {
            linkedForms = currentLinkedForms;
        } else {
            linkedForms = new ArrayList<BiobankFormBase>();
            currentLinkedForms = linkedForms;
        }
        linkedForms.add(this);
        try {
            init();
        } catch (final RemoteConnectFailureException exp) {
            BioBankPlugin.openRemoteConnectErrorMessage();
        } catch (Exception e) {
            logger.error("BioBankFormBase.createPartControl Error", e);
        }
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        mform = new ManagedForm(parent);
        toolkit = mform.getToolkit();
        widgetCreator.setToolkit(toolkit);
        form = mform.getForm();
        toolkit.decorateFormHeading(form.getForm());

        // start a new runnable so that database objects are populated in a
        // separate thread.
        BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
            public void run() {
                try {
                    createFormContent();
                    form.reflow(true);
                } catch (final RemoteConnectFailureException exp) {
                    BioBankPlugin.openRemoteConnectErrorMessage();
                } catch (Exception e) {
                    BioBankPlugin.openError(
                        "BioBankFormBase.createPartControl Error", e);
                }
            }
        });
    }

    /**
     * Called in a non-UI thread to create the widgets that make up the form.
     */
    protected abstract void createFormContent() throws Exception;

    protected Section createSection(String title) {
        Section section = toolkit.createSection(form.getBody(), Section.TWISTIE
            | Section.TITLE_BAR | Section.EXPANDED);
        if (title != null) {
            section.setText(title);
        }
        section.setLayout(new GridLayout(1, false));
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(false);
            }
        });
        return section;
    }

    protected Composite sectionAddClient(Section section) {
        Composite client = toolkit.createComposite(section);
        section.setClient(client);
        client.setLayout(new GridLayout(2, false));
        toolkit.paintBordersFor(client);
        return client;
    }

    protected Composite createSectionWithClient(String title) {
        return sectionAddClient(createSection(title));
    }

    protected void addSectionToolbar(Section section, String tooltip,
        SelectionListener listener) {
        ToolBar tbar = (ToolBar) section.getTextClient();
        if (tbar == null) {
            tbar = new ToolBar(section, SWT.FLAT | SWT.HORIZONTAL);
            section.setTextClient(tbar);
        }

        ToolItem titem = new ToolItem(tbar, SWT.NULL);
        titem.setImage(BioBankPlugin.getDefault().getImageRegistry().get(
            BioBankPlugin.IMG_ADD));
        titem.setToolTipText(tooltip);
        titem.addSelectionListener(listener);
    }

    public FormToolkit getToolkit() {
        return toolkit;
    }

    public AdapterBase getAdapter() {
        return adapter;
    }

    protected <T> ComboViewer createComboViewer(Composite parent,
        String fieldLabel, Collection<?> input, T selection) {
        return widgetCreator.createComboViewer(parent, fieldLabel, input,
            selection);
    }

    protected Control createWidget(Composite parent, Class<?> widgetClass,
        int widgetOptions, String fieldLabel, String value) {
        return widgetCreator.createWidget(parent, widgetClass, widgetOptions,
            fieldLabel, value);
    }

    protected Control createWidget(Composite parent, Class<?> widgetClass,
        int widgetOptions, String fieldLabel) {
        return createWidget(parent, widgetClass, widgetOptions, fieldLabel,
            null);
    }

    protected void createWidgetsFromMap(Map<String, FieldInfo> fieldsMap,
        Composite parent) {
        widgetCreator.createWidgetsFromMap(fieldsMap, parent);
    }

    protected Text createReadOnlyField(Composite parent, int widgetOptions,
        String fieldLabel, String value) {
        Text result = (Text) createWidget(parent, Text.class, SWT.READ_ONLY
            | widgetOptions, fieldLabel, value);
        return result;
    }

    protected Text createReadOnlyField(Composite parent, int widgetOptions,
        String fieldLabel) {
        return createReadOnlyField(parent, widgetOptions, fieldLabel, null);
    }

    public static void setTextValue(Text label, String value) {
        if (value != null && !label.isDisposed()) {
            label.setText(value);
        }
    }

    public static void setTextValue(Text label, Object value) {
        if (value != null) {
            setTextValue(label, value.toString());
        }
    }

    public static void setCheckBoxValue(Button button, Boolean value) {
        if (value != null) {
            button.setSelection(value.booleanValue());
        }
    }

    public void setBroughtToTop() {
        // linkedForms.remove(this);
        // linkedForms.add(this);
        currentLinkedForms = linkedForms;
    }

    public void setDeactivated() {
        linkedForms.remove(this);
    }

}
