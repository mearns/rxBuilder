package com.commercehub.rxbuilder.demo.domain

class Bar {
    final String name
    final Baz[] bazzes

    Bar(String name, Baz[] bazzes) {
        this.name = name
        this.bazzes = bazzes
    }

    @Override
    public String toString() {
        return "Bar{" +
                "name='" + name + '\'' +
                ", bazzes=" + Arrays.toString(bazzes) +
                '}';
    }

    static class BarBuilder {
        String name
        List<Baz.BazBuilder> bazzes

        {
            bazzes = new LinkedList<>()
        }

        BarBuilder name(String name) {
            this.name = name;
            return this;
        }

        BarBuilder baz(Baz.BazBuilder baz) {
            this.bazzes.add(baz)
            return this;
        }

        Bar build() {
            return new Bar(name, bazzes?.collect{ it?.build() }?.toArray(new Baz[0]));
        }
    }
}
