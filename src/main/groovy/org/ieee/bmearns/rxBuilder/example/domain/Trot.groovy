package org.ieee.bmearns.rxBuilder.example.domain


class Trot {
    final String name

    Trot(String name) {
        this.name = name
    }

    @Override
    public String toString() {
        return "Trot{" +
                "name='" + name + '\'' +
                '}';
    }

    static class TrotBuilder {
        String name

        TrotBuilder name(String name) {
            this.name = name;
            return this;
        }

        Trot build() {
            return new Trot(name);
        }
    }
}
