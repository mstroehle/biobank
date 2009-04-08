package edu.ualberta.med.biobank.treeview;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.forms.ClinicEntryForm;
import edu.ualberta.med.biobank.forms.ClinicViewForm;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.model.Clinic;

public class ClinicAdapter extends Node {
    
	private Clinic clinic;
	
	public ClinicAdapter(Node parent, Clinic clinic) {
		super(parent);
		this.clinic = clinic;
	}

	public void setClinic(Clinic clinic) {
		this.clinic = clinic;
		this.clinic.setName("");
	}

	public Clinic getClinic() {
		return clinic;
	}
	
	public void addChild(Node child) {
		Assert.isTrue(false, "Cannot add children to this adapter");
	}

	@Override
	public int getId() {
		Assert.isNotNull(clinic, "Clinic is null");
		Object o = (Object) clinic.getId();
		if (o == null) return 0;
		return clinic.getId();
	}

	@Override
	public String getName() {
		Assert.isNotNull(clinic, "Clinic is null");
		Object o = (Object) clinic.getName();
		if (o == null) return null;
		return clinic.getName();
	}
    
    public void performDoubleClick() {
        openForm(new FormInput(this), ClinicViewForm.ID);
    }
    
    public void popupMenu(TreeViewer tv, Tree tree,  Menu menu) {
        MenuItem mi = new MenuItem (menu, SWT.PUSH);
        mi.setText ("Edit Clinic");
        mi.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                openForm(new FormInput(ClinicAdapter.this), ClinicEntryForm.ID);
            }

            public void widgetDefaultSelected(SelectionEvent e) {                    
            }
        });

        mi = new MenuItem (menu, SWT.PUSH);
        mi.setText ("View Clinic");
        mi.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                openForm(new FormInput(ClinicAdapter.this), ClinicViewForm.ID);
            }

            public void widgetDefaultSelected(SelectionEvent e) {                    
            }
        }); 
    }
}
