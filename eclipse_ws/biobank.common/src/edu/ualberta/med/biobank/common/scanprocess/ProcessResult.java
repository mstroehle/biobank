package edu.ualberta.med.biobank.common.scanprocess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ProcessResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<String> logs = new ArrayList<String>();
    private CellStatus processStatus;

    public List<String> getLogs() {
        return logs;
    }

    public CellStatus getProcessStatus() {
        return processStatus;
    }

    protected void setProcessStatus(CellStatus processStatus) {
        this.processStatus = processStatus;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

}
