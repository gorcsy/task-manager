package com.codecool.processwatch.queries;

import com.codecool.processwatch.domain.Query;
import com.codecool.processwatch.domain.Process;
import com.codecool.processwatch.gui.FakeMain;
import com.codecool.processwatch.gui.FxMain;
import javafx.application.Application;

import java.util.stream.Stream;

/**
 * This is the identity query.  It selects everything from its source.
 */
public class SelectByParentPID implements Query{

    private long selectedParentPID;

    public SelectByParentPID(long selectedParentPID) {
        this.selectedParentPID = selectedParentPID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Process> run(Stream<Process> input) {
        return input.filter(process -> process.getParentPid() == selectedParentPID);
    }
}