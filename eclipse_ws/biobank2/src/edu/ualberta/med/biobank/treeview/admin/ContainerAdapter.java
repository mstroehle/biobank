package edu.ualberta.med.biobank.treeview.admin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.container.ContainerDeleteAction;
import edu.ualberta.med.biobank.common.action.container.ContainerGetChildrenAction;
import edu.ualberta.med.biobank.common.action.container.ContainerMoveAction;
import edu.ualberta.med.biobank.common.action.container.ContainerMoveSpecimensAction;
import edu.ualberta.med.biobank.common.permission.container.ContainerDeletePermission;
import edu.ualberta.med.biobank.common.permission.container.ContainerReadPermission;
import edu.ualberta.med.biobank.common.permission.container.ContainerUpdatePermission;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.dialogs.MoveContainerDialog;
import edu.ualberta.med.biobank.dialogs.MoveSpecimensToDialog;
import edu.ualberta.med.biobank.dialogs.select.SelectParentContainerDialog;
import edu.ualberta.med.biobank.forms.ContainerEntryForm;
import edu.ualberta.med.biobank.forms.ContainerViewForm;
import edu.ualberta.med.biobank.gui.common.BgcLogger;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ContainerAdapter extends AdapterBase {

    @SuppressWarnings("unused")
    private static BgcLogger LOGGER = BgcLogger
        .getLogger(ContainerAdapter.class.getName());

    private List<Container> childContainers = null;

    public ContainerAdapter(AdapterBase parent, ContainerWrapper container) {
        super(parent, container);
        // assume it has children for now and set it appropriately when user
        // double clicks on node
        if (container != null) {
            setHasChildren(true);
        }
    }

    @Override
    public void init() {
        try {
            ContainerWrapper container = (ContainerWrapper) getModelObject();
            Integer id = container.getId();

            this.isDeletable =
                SessionManager.getAppService()
                    .isAllowed(new ContainerDeletePermission(id));
            this.isReadable =
                SessionManager.getAppService()
                    .isAllowed(new ContainerReadPermission(container.getSite()
                        .getId()));
            this.isEditable =
                SessionManager.getAppService()
                    .isAllowed(new ContainerUpdatePermission(id));
        } catch (ApplicationException e) {
            BgcPlugin.openAsyncError("Permission Error",
                "Unable to retrieve user permissions");
        }
    }

    @Override
    public void executeDoubleClick() {
        performExpand();
        openViewForm();
    }

    @Override
    public void setModelObject(Object modelObject) {
        super.setModelObject(modelObject);
        // assume it has children for now and set it appropriately when user
        // double clicks on node
        setHasChildren(true);
    }

    private ContainerWrapper getContainer() {
        return (ContainerWrapper) getModelObject();
    }

    @Override
    protected String getLabelInternal() {
        ContainerWrapper container = getContainer();
        if (container.getContainerType() == null) {
            return container.getLabel();
        }
        return container.getLabel() + " ("
            + container.getContainerType().getNameShort() + ")";
    }

    @Override
    public String getTooltipTextInternal() {
        ContainerWrapper container = getContainer();
        if (container != null) {
            SiteWrapper site = container.getSite();
            if (site != null) {
                return site.getNameShort() + " - "
                    + getTooltipText("Container");
            }
        }
        return getTooltipText("Container");
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        addEditMenu(menu, "Container");
        addViewMenu(menu, "Container");

        Boolean topLevel = getContainer().getContainerType().getTopLevel();

        if (isEditable() && (topLevel == null || !topLevel)) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText(Messages.ContainerAdapter_move_label);
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    moveContainer(null);
                }
            });
        }

        if (isEditable() && getContainer().hasSpecimens()) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText("Move all specimens to");
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    moveSpecimens();
                }
            });
        }

        addDeleteMenu(menu, "Container");
    }

    public void moveSpecimens() {
        final MoveSpecimensToDialog msDlg =
            new MoveSpecimensToDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), getContainer());
        if (msDlg.open() == Dialog.OK) {
            try {
                final Integer toContainerId = msDlg.getNewContainer().getId();
                final ContainerWrapper newContainer = msDlg.getNewContainer();
                IRunnableContext context =
                    new ProgressMonitorDialog(Display.getDefault()
                        .getActiveShell());
                context.run(true,
                    false,
                    new IRunnableWithProgress() {
                        @Override
                        public void run(final IProgressMonitor monitor) {
                            monitor.beginTask(NLS
                                .bind(
                                    "Moving specimens from container {0} to {1}",
                                    getContainer().getFullInfoLabel(),
                                    newContainer.getFullInfoLabel()),
                                IProgressMonitor.UNKNOWN);
                            try {
                                SessionManager.getAppService()
                                    .doAction(new ContainerMoveSpecimensAction(
                                        getContainer().getWrappedObject(),
                                        newContainer.getWrappedObject()));
                                monitor.done();
                                BgcPlugin
                                    .openAsyncInformation("Specimens moved",
                                        NLS.bind(
                                            "{0} specimens are now in {1}.",
                                            newContainer.getSpecimens().size(),
                                            newContainer.getFullInfoLabel()));
                            } catch (Exception e) {
                                monitor.setCanceled(true);
                                BgcPlugin
                                    .openAsyncError("Move problem", e);
                            }
                        }
                    });
                ContainerAdapter newContainerAdapter =
                    (ContainerAdapter) SessionManager
                        .searchFirstNode(ContainerWrapper.class,
                            newContainer.getId());
                if (newContainerAdapter != null) {
                    getContainer().reload();
                    newContainerAdapter.performDoubleClick();
                }
                getContainer().reload();
                SessionManager.openViewForm(getContainer());
            } catch (Exception e) {
                BgcPlugin
                    .openError(
                        "Problem while moving specimens", e);
            }
        }
    }

    @Override
    protected String getConfirmDeleteMessage() {
        return "Are you sure you want to delete this container?";
    }

    public void moveContainer(ContainerWrapper destParentContainer) {
        final ContainerAdapter oldParent = (ContainerAdapter) getParent();
        final MoveContainerDialog mc =
            new MoveContainerDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), getContainer(),
                destParentContainer);
        if (mc.open() == Dialog.OK) {
            try {
                if (setNewPositionFromLabel(mc.getNewLabel())) {
                    // update new parent
                    ContainerWrapper newParentContainer =
                        getContainer().getParentContainer();
                    ContainerAdapter parentAdapter =
                        (ContainerAdapter) SessionManager
                            .searchFirstNode(ContainerWrapper.class,
                                newParentContainer.getId());
                    if (parentAdapter != null) {
                        parentAdapter.getContainer().reload();
                        parentAdapter.removeAll();
                        parentAdapter.performExpand();
                    }
                    // update old parent
                    oldParent.getContainer().reload();
                    oldParent.removeAll();
                    oldParent.performExpand();
                }
            } catch (Exception e) {
                BgcPlugin
                    .openError("Problem while moving container",
                        e);
            }
        }
    }

    /**
     * if address exists and if address is not full and if type is valid for
     * slot: modify this object's position, label and the label of children
     */
    public boolean setNewPositionFromLabel(final String newLabel)
        throws Exception {
        final ContainerWrapper container = getContainer();
        final String oldLabel = container.getLabel();
        List<ContainerWrapper> newParentContainers =
            container.getPossibleParents(newLabel);
        if (newParentContainers.size() == 0) {
            BgcPlugin
                .openError(
                    "Container Move Error",
                    MessageFormat
                        .format(
                            "A parent container with child \"{0}\" does not exist.",
                            newLabel));
            return false;
        }

        ContainerWrapper newParent;
        if (newParentContainers.size() > 1) {
            SelectParentContainerDialog dlg =
                new SelectParentContainerDialog(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), newParentContainers);
            if (dlg.open() != Dialog.OK) {
                return false;
            }
            newParent = dlg.getSelectedContainer();
        } else {
            newParent = newParentContainers.get(0);
        }

        SessionManager.getAppService().doAction(new ContainerMoveAction(
            getContainer().getWrappedObject(),
            newParent.getWrappedObject(), newLabel));
        return true;
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        List<AbstractAdapterBase> res = new ArrayList<AbstractAdapterBase>();
        if (ContainerWrapper.class.isAssignableFrom(searchedClass)) {
            // FIXME search might need to be different now
            // ContainerWrapper containerWrapper = (ContainerWrapper)
            // searchedObject;
            // List<ContainerWrapper> parents = new
            // ArrayList<ContainerWrapper>();
            // ContainerWrapper currentContainer = containerWrapper;
            // while (currentContainer.hasParentContainer()) {
            // currentContainer = currentContainer.getParentContainer();
            // parents.add(currentContainer);
            // }
            // res = searchChildContainers(searchedObject, objectId, this,
            // parents);
        }
        return res;
    }

    @Override
    protected AdapterBase createChildNode() {
        return new ContainerAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(Object child) {
        Assert.isTrue(child instanceof ContainerWrapper);
        return new ContainerAdapter(this, (ContainerWrapper) child);
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        childContainers =
            SessionManager.getAppService()
                .doAction(new ContainerGetChildrenAction(getId())).getList();
        return ModelWrapper.wrapModelCollection(SessionManager.getAppService(),
            childContainers,
            ContainerWrapper.class);
    }

    @Override
    public String getEntryFormId() {
        return ContainerEntryForm.ID;
    }

    @Override
    public String getViewFormId() {
        return ContainerViewForm.ID;
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        if (o instanceof ContainerAdapter) return internalCompareTo(o);
        return 0;
    }

    @Override
    protected void runDelete() throws Exception {
        SessionManager.getAppService().doAction(new ContainerDeleteAction(
            (Container) getModelObject().getWrappedObject()));
        SessionManager.updateAllSimilarNodes(getParent(), true);
    }
}
