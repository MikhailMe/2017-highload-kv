package ru.mail.polis.mikhail.Topology;

import java.util.Set;

public interface Topology {

    int getQuorum();

    int getSize();

    Set<String> getNodes();
}
