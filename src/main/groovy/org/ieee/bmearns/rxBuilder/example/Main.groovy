package org.ieee.bmearns.rxBuilder.example

import org.ieee.bmearns.rxBuilder.example.domain.*

import rx.Observable
import rx.observables.BlockingObservable

class Main {

    public static void main(String[] args) {

        Observable<Foo.FooBuilder> fooBuilderStream = getFooNames()
            .map { String name ->
                return (new Foo.FooBuilder()).name(name)
            }
            .map { Foo.FooBuilder builder ->
                return new StreamCollector<Foo.FooBuilder, Object>(builder)
            }
            .map { StreamCollector<Foo.FooBuilder, Object> fooStreams ->
                fooStreams.addStream(
                    getBarNamesForFoo(fooStreams.subject.name)
                        //Create the BarBuilder from the name.
                        .map { String barName ->
                            return (new Bar.BarBuilder()).name(barName)
                        }
                        //Add the Bar to the Foo.
                        .map { Bar.BarBuilder barBuilder ->
                            fooStreams.subject.bar(barBuilder)
                            return barBuilder
                        }
                        //Create a stream collector for this Bar.
                        .map { Bar.BarBuilder barBuilder ->
                            return new StreamCollector<Bar.BarBuilder, Object>(barBuilder)
                        }
                        //Query the Baz objects for this Bar.
                        .map { StreamCollector<Bar.BarBuilder, Object> barStreams ->
                            barStreams.addStream(
                                getBazNamesForBar(barStreams.subject.name)
                                    //Create the BazBuilder
                                    .map { String bazName ->
                                        return (new Baz.BazBuilder()).name(bazName)
                                    }
                                    //Add it to the Bar
                                    .map { Baz.BazBuilder bazBuilder ->
                                        return barStreams.subject.baz(bazBuilder)
                                    }
                            )
                        }
                        .flatMap { StreamCollector<Bar.BarBuilder, Object> barStreams ->
                            barStreams.subjectStream()
                        }
                )
            }
            .map { StreamCollector<Foo.FooBuilder, Object> fooStreams ->
                fooStreams.addStream(
                    getTrotNamesForFoo(fooStreams.subject.name)
                        .map { String trotName ->
                            return (new Trot.TrotBuilder()).name(trotName)
                        }
                        .map { Trot.TrotBuilder trotBuilder ->
                            fooStreams.subject.trot(trotBuilder)
                            return trotBuilder
                        }
                )
            }
            .flatMap { StreamCollector<Foo.FooBuilder, Object> fooStreams -> fooStreams.subjectStream() }

        fooBuilderStream.subscribe(
                { println it.build() },
                { println "Error: " + it.getMessage(); it.printStackTrace() },
                { println "Done"}
        )


    }

    public static Observable<String> getFooNames() {
        return Observable.<String>just("foo1", "foo2", "foo3")
    }

    public static Observable<String> getBarNamesForFoo(String fooName) {
        switch(fooName) {
            case "foo1":
                return Observable.<String>just("bar1-1", "bar1-2")
            case "foo2":
                return Observable.<String>just("bar2-1", "bar2-2", "bar2-3")
            case "foo3":
                return Observable.<String>just("bar3-1", "bar3-2", "bar3-3", "bar3-4", "bar3-5")
            default:
                return Observable.<String>from([])
        }
    }

    public static Observable<String> getTrotNamesForFoo(String fooName) {
        switch(fooName) {
            case "foo1":
                return Observable.<String>just("trot1-1", "trot1-2")
            case "foo2":
                return Observable.<String>just("trot2-1", "trot2-2", "trot2-3")
            case "foo3":
                return Observable.<String>just("trot3-1", "trot3-2", "trot3-3", "trot3-4", "trot3-5")
            default:
                return Observable.<String>from([])
        }
    }


    public static Observable<String> getBazNamesForBar(String barName) {
        switch(barName) {
            case "bar1-1":
                return Observable.<String>just("baz-A", "baz-B")
            case "bar2-1":
                return Observable.<String>just("baz-C")
            case "bar2-2":
                return Observable.<String>from([])
            default:
                return Observable.<String>just("baz-N")
        }
    }
}
