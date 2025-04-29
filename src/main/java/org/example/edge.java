package org.example;

import java.util.Objects;

public class edge {
    public String from;
    public String to;
    public edge(String source,String dst) {
        this.from = source;
        this.to = dst;
    }

    public edge() {
        this.from = null;
        this.to = null;
    }

    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        edge ed = (edge) o;
        return Objects.equals(from, ed.from) && Objects.equals(to,ed.to);

    }
    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

}
