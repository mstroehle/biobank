package edu.ualberta.med.biobank.dialogs.scanmanually;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This wizard This class extends WizardDialog because we always want the default button to be the
 * wizard's "Next" button.
 * 
 * When the WizardDialog receives an "Enter" key from the keyboard, the default button is activated.
 * WizardDialog makes the "Finish" button the default button if it is enabled. In this
 * implementation we always want the "Next" button to be the default even though the "Finish" button
 * is enabled.
 * 
 * @author Nelson Loyola
 * 
 */
public class ScanTubesManuallyWizardDialog extends WizardDialog {

    /**
     * This constructor is private. Use {@link #getInventoryIds} to create this wizard.
     */
    ScanTubesManuallyWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    @Override
    public void updateButtons() {
        super.updateButtons();
        getShell().setDefaultButton(getButton(IDialogConstants.NEXT_ID));
    }
}
