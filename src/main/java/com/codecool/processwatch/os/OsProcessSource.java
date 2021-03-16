package com.codecool.processwatch.os;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codecool.processwatch.domain.Process;
import com.codecool.processwatch.domain.ProcessSource;
import com.codecool.processwatch.domain.User;

/**
 * A process source using the Java {@code ProcessHandle} API to retrieve information
 * about the current processes.
 */
public class OsProcessSource implements ProcessSource {
    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Process> getProcesses() {
        Stream<ProcessHandle> processStream = ProcessHandle.allProcesses();
        List<Process> process = new ArrayList<>();
        processStream.forEach(processes -> process.add(mapObjectFromProcess(processes)));
        return process.stream();
    }

    public Set<String> getActualUserNames() {
        Set<String> nameList = new LinkedHashSet<>();
        getProcesses().forEach(process -> nameList.add(process.getUserName()));
        return nameList;
    }

    public Set<Long> getActualParentPid() {
        Set<Long> parentPidList = new LinkedHashSet<>();
        getProcesses().forEach(process -> parentPidList.add(process.getParentPid()));
        return parentPidList;
    }

    public Set<String> getActualProcessNames() {
        Set<String> nameList = new LinkedHashSet<>();
        getProcesses().forEach(process -> nameList.add(process.getName()));
        return nameList;
    }

    private Process mapObjectFromProcess(ProcessHandle processHandle){
        long pid;
        long parentPid = 0;
        User user;
        String name = "";
        String[] args = {""};

        pid = processHandle.pid();

        if(processHandle.parent().isPresent()) {
            parentPid = processHandle.parent().get().pid();
        }
        if(processHandle.info().user().isPresent()) {
            user = new User(processHandle.info().user().get());
        } else {
            user = new User("");
        }

        if(processHandle.info().command().isPresent()){
            name = processHandle.info().command().get();
        }

        if(processHandle.info().arguments().isPresent()){
            args = processHandle.info().arguments().get();
        }

        return new Process(pid, parentPid, user, name, args);
    }
}
