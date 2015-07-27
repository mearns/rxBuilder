package com.commercehub.rxbuilder.demo.domain


class Foo {

    final String name
    final Bar[] bars
    final Trot[] trots

    Foo(String name, Bar[] bars, Trot[] trots) {
        this.name = name
        this.bars = bars;
        this.trots = trots;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "name='" + name + '\'' +
                ", bars=" + Arrays.toString(bars) +
                ", trots=" + Arrays.toString(trots) +
                '}';
    }

    static class FooBuilder {

        String name
        List<Bar.BarBuilder> bars
        List<Trot.TrotBuilder> trots

        {
            bars = new LinkedList<>()
            trots = new LinkedList<>()
        }

        FooBuilder name(String name) {
            this.name = name;
            return this;
        }

        FooBuilder bar(Bar.BarBuilder bar) {
            this.bars.add(bar);
            return this;
        }

        FooBuilder trot(Trot.TrotBuilder trot) {
            if(trot.name == "ERROR") {
                throw new RuntimeException("Found a bad trot name: " + trot.name)
            }
            this.trots.add(trot);
            return this;
        }

        Foo build() {
            return new Foo(
                    name,
                    bars?.collect{ it?.build() }?.toArray(new Bar[0]),
                    trots?.collect{ it?.build() }?.toArray(new Trot[0])
            )
        }
    }
}
