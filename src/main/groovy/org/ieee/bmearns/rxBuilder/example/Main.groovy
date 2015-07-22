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
            .map { StreamCollector<Foo.FooBuilder, Object> streams ->
                streams.addStream(
                        getBarNamesForFoo(streams.subject.name)
                                .map { String barName ->
                                    return (new Bar.BarBuilder()).name(barName)
                                }
                                .map { Bar.BarBuilder barBuilder ->
                                    return streams.subject.bar(barBuilder)
                                }
                )
            }
            .map { StreamCollector<Foo.FooBuilder, Object> streams ->
                streams.addStream(
                        getTrotNamesForFoo(streams.subject.name)
                                .map { String trotName ->
                                    return (new Trot.TrotBuilder()).name(trotName)
                                }
                                .map { Trot.TrotBuilder trotBuilder ->
                                    return streams.subject.trot(trotBuilder)
                                }
                )
            }
            .flatMap { StreamCollector<Trot.TrotBuilder, Object> streams -> streams.subjectStream() }

        fooBuilderStream.subscribe(
                { println it.build() },
                { println "Error: " + it.getMessage()},
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
                return Observable.<String>just("trot3-1", "trot3-2", "ERROR", "trot3-4", "trot3-5")
            default:
                return Observable.<String>from([])
        }
    }
}
