package edu.ualberta.med.biobank.test.model;

import org.junit.Test;

import edu.ualberta.med.biobank.test.DbTest;
import edu.ualberta.med.biobank.test.model.util.HasXHelper;

public class TestAliquotedSpecimen extends DbTest {
    @Test
    public void expectedActivityStatusIds() {
        HasXHelper.checkExpectedActivityStatusIds(session,
            factory.createAliquotedSpecimen());
    }
}
