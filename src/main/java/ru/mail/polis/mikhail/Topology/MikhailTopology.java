package ru.mail.polis.mikhail;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MikhailTopology implements Topology {

    @NotNull
    private final Set<String> nodes;

    public MikhailTopology(@NotNull final Set<String> nodes) {
        this.nodes = nodes;
    }

    @Override
    public int getQuorum() {
        return this.nodes.size() / 2 + 1;
    }

    @Override
    public int getSize() {
        return this.nodes.size();
    }

    @Override
    public Set<String> getNodes() {
        return this.nodes;
    }
}
