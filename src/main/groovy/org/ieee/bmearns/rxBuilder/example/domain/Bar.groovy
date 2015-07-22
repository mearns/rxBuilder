package org.ieee.bmearns.rxBuilder.example.domain


class Bar {
    final String name

    Bar(String name) {
        this.name = name
    }

    @Override
    public String toString() {
        return "Bar{" +
                "name='" + name + '\'' +
                '}';
    }

    static class BarBuilder {
        String name

        BarBuilder name(String name) {
            this.name = name;
            return this;
        }

        Bar build() {
            return new Bar(name);
        }
    }
}
