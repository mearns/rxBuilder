package org.ieee.bmearns.rxBuilder.example.domain

class Baz {
    final String name

    Baz(String name) {
        this.name = name
    }

    @Override
    public String toString() {
        return "Baz{" +
                "name='" + name + '\'' +
                '}';
    }

    static class BazBuilder {
        String name

        BazBuilder name(String name) {
            this.name = name;
            return this;
        }

        Baz build() {
            return new Baz(name);
        }
    }
}
