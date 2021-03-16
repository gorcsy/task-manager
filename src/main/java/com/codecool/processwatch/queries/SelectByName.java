package com.codecool.processwatch.queries;

        import com.codecool.processwatch.domain.Query;
        import com.codecool.processwatch.domain.Process;
        import java.util.stream.Stream;

/**
 * This is the identity query.  It selects everything from its source.
 */
public class SelectByName implements Query {

    private final String selectedName;
    public SelectByName(String selectedName) {
        this.selectedName = selectedName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Process> run(Stream<Process> input) {
        return input.filter(process -> process.getName().equals(selectedName));
    }
}