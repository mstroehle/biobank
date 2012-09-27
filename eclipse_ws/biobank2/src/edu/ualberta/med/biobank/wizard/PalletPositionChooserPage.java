package edu.ualberta.med.biobank.wizard;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;

import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.model.ContainerCell;
import edu.ualberta.med.biobank.model.ContainerStatus;

public class PalletPositionChooserPage extends AbstractContainerChooserPage {

    public static final String NAME = "HOTEL_CONTAINER";
    private ContainerTypeWrapper containerType;

    private ComboViewer comboViewer;
    private Combo combo;
    private ContainerCell selectedPosition;

    protected PalletPositionChooserPage() {
        super(NAME);
        setDescription("Choose position in container");
        gridWidth = 60;
        defaultDim1 = 19;
        defaultDim2 = 1;
    }

    @Override
    protected void initComponent() {
        super.initComponent();
        pageContainer.layout(true, true);
        containerWidget.setLegendOnSide(true);
        // containerWidget.setFirstColSign(null);
        // containerWidget.setFirstRowSign(1);
        // containerWidget.setShowNullStatusAsEmpty(true);

        Label label = new Label(pageContainer, SWT.NONE);
        label.setText("Choose container type:");
        combo = new Combo(pageContainer, SWT.NONE);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        combo.setLayoutData(gd);
        comboViewer = new ComboViewer(combo);
        comboViewer.setContentProvider(new ArrayContentProvider());
        comboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                ContainerTypeWrapper st = (ContainerTypeWrapper) element;
                return st.getName();
            }
        });

        comboViewer
            .addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    // type has been chosen = page complete if position
                    // choosen
                    if (!textPosition.getText().isEmpty()) {
                        setPageComplete(true);
                    }
                    containerType = (ContainerTypeWrapper) ((IStructuredSelection) comboViewer
                        .getSelection()).getFirstElement();
                }
            });
    }

    @Override
    public void setCurrentContainer(ContainerWrapper container) {
        super.setCurrentContainer(container);
        setTitle("Container " + container.getLabel());
        updateFreezerGrid();
        textPosition.setText("");
        selectedPosition = null;
        List<ContainerTypeWrapper> types = getCurrentContainer()
            .getContainerType().getChildContainerTypeCollection();
        // do not include type not active
        comboViewer.setInput(types);
        if (types.size() == 1) {
            comboViewer
                .setSelection(new StructuredSelection(types.toArray()[0]));
        }
        setPageComplete(false);
    }

    @Override
    protected ContainerCell positionSelection(MouseEvent e) {
        boolean complete = false;
        ContainerCell cell = (ContainerCell) containerWidget
            .getObjectAtCoordinates(e.x, e.y);
        if (cell.getStatus() == ContainerStatus.NOT_INITIALIZED) {
            this.selectedPosition = cell;
            int positionText = selectedPosition.getRow() + 1;
            textPosition.setText(String.valueOf(positionText));
            complete = true;
        } else {
            textPosition.setText("");
            complete = false;
        }
        if (complete) {
            if (comboViewer.getSelection() == null
                || ((IStructuredSelection) comboViewer.getSelection())
                    .isEmpty()) {
                setPageComplete(false);
            }
        }
        setPageComplete(complete);
        return cell;
    }

    public ContainerCell getSelectedPosition() {
        return selectedPosition;
    }

    public ContainerTypeWrapper getContainerType() {
        return containerType;
    }

    @Override
    protected void setStatus(ContainerCell cell) {
        if (cell.getContainer() == null) {
            cell.setStatus(ContainerStatus.NOT_INITIALIZED);
        } else {
            cell.setStatus(ContainerStatus.INITIALIZED);
        }
    }

    @Override
    protected void initEmptyCells(ContainerCell[][] cells) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] == null) {
                    ContainerCell cell = new ContainerCell(i, j);
                    cell.setStatus(ContainerStatus.NOT_INITIALIZED);
                    cells[i][j] = cell;
                }
            }
        }
    }

}