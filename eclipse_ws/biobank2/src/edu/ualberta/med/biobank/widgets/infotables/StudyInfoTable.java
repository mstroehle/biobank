package edu.ualberta.med.biobank.widgets.infotables;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.common.formatters.NumberFormatter;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.gui.common.widgets.BgcLabelProvider;

public class StudyInfoTable extends InfoTableWidget {

    protected static class TableRowData {
        StudyWrapper study;
        String name;
        String nameShort;
        String status;
        Long patientCount;
        Long visitCount;

        @Override
        public String toString() {
            return StringUtils.join(new String[] { name, nameShort, status,
                (patientCount != null) ? patientCount.toString() : "", //$NON-NLS-1$
                (visitCount != null) ? visitCount.toString() : "" }, "\t"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static final String[] HEADINGS = new String[] {
        Messages.StudyInfoTable_name_label,
        Messages.StudyInfoTable_nameshort_label,
        Messages.StudyInfoTable_status_label,
        Messages.StudyInfoTable_patients_label,
        Messages.StudyInfoTable_visits_label };

    public StudyInfoTable(Composite parent, List<StudyWrapper> collection) {
        super(parent, collection, HEADINGS, 10, StudyWrapper.class);
    }

    @Override
    protected BgcLabelProvider getLabelProvider() {
        return new BgcLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                TableRowData info = (TableRowData) ((BiobankCollectionModel) element).o;
                if (info == null) {
                    if (columnIndex == 0) {
                        return Messages.StudyInfoTable_loading;
                    }
                    return ""; //$NON-NLS-1$
                }
                switch (columnIndex) {
                case 0:
                    return info.name;
                case 1:
                    return info.nameShort;
                case 2:
                    return (info.status != null) ? info.status : ""; //$NON-NLS-1$
                case 3:
                    return NumberFormatter.format(info.patientCount);
                case 4:
                    return NumberFormatter.format(info.visitCount);
                default:
                    return ""; //$NON-NLS-1$
                }
            }
        };
    }

    // @Override
    // protected BgcTableSorter getTableSorter() {
    // return new BgcTableSorter() {
    //
    // @Override
    // public int compare(Viewer viewer, Object e1, Object e2) {
    // TableRowData row1 = (TableRowData) e1;
    // TableRowData row2 = (TableRowData) e2;
    // int rc = 0;
    //
    // switch (propertyIndex) {
    // case 0:
    // rc = row1.name.compareTo(row2.name);
    // break;
    // case 1:
    // rc = row1.nameShort.compareTo(row2.nameShort);
    // break;
    // }
    // return rc;
    // }
    //
    // };
    // }

    @Override
    public Object getCollectionModelObject(Object study) throws Exception {
        TableRowData info = new TableRowData();
        info.study = (StudyWrapper) study;
        info.name = info.study.getName();
        info.nameShort = info.study.getNameShort();
        info.status = info.study.getActivityStatus().getName();
        if (info.status == null) {
            info.status = ""; //$NON-NLS-1$
        }
        info.patientCount = info.study.getPatientCount(true);
        info.visitCount = info.study.getCollectionEventCount(true);
        info.study.reload();
        return info;
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null)
            return null;
        return ((TableRowData) o).toString();
    }

    @Override
    public StudyWrapper getSelection() {
        BiobankCollectionModel item = getSelectionInternal();
        if (item == null)
            return null;
        TableRowData row = (TableRowData) item.o;
        Assert.isNotNull(row);
        return row.study;
    }

    @Override
    protected BiobankTableSorter getComparator() {
        return null;
    }

}
