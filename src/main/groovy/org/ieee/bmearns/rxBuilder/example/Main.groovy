package org.ieee.bmearns.rxBuilder.example

import org.ieee.bmearns.rxBuilder.example.domain.*
import org.ieee.bmearns.rxBuilder.example.remote.ExampleRemoteService

import rx.Observable
import rx.functions.Func1

class Main {

    public static void main(String[] args) {
        main2()
    }

    public static void main2() {

        UpdaterStreamFactory<Bar.BarBuilder, String> barUpdaterBazStreamFactory = new DefaultUpdaterStreamFactory<>(
                {Bar.BarBuilder subject, String bazName ->
                    subject.baz(new Baz.BazBuilder().name(bazName))
                },
                {Bar.BarBuilder subject ->
                    ExampleRemoteService.getBazNamesForBar(subject.name)
                }
        )

        UpdaterStreamFactory<Foo.FooBuilder, Bar.BarBuilder> fooUpdaterBarStreamFactory = new DefaultUpdaterStreamFactory<>(
                {Foo.FooBuilder subject, Bar.BarBuilder barBuilder ->
                    subject.bar(barBuilder)
                },
                {Foo.FooBuilder subject ->
                    ExampleRemoteService.getBarNamesForFoo(subject.name)
                        .map { String name ->
                            (new Bar.BarBuilder().name(name))
                        }
                        .map({ Bar.BarBuilder barBuilder ->
                            (new ReactiveUpdater<Bar.BarBuilder>(barBuilder))
                                    .addUpdater(barUpdaterBazStreamFactory)
                                    .waitFor()
                        })
                }
        )

        Observable<Foo.FooBuilder> fooBuilderStream = ExampleRemoteService.getFooNames()
                .map { String name ->
                    return (new Foo.FooBuilder()).name(name)
                }
                .map({ Foo.FooBuilder fooBuilder ->
                    (new ReactiveUpdater<Foo.FooBuilder>(fooBuilder))
                        .addUpdater(fooUpdaterBarStreamFactory)
                        .waitFor()
                } as Func1<Foo.FooBuilder, Foo.FooBuilder>)

        fooBuilderStream.subscribe(
                { println it.build() },
                { println "Error: " + it.getMessage(); it.printStackTrace() },
                { println "Done"}
        )
    }

    public static void main1() {
        Observable<Foo.FooBuilder> fooBuilderStream = ExampleRemoteService.getFooNames()
            .map { String name ->
                return (new Foo.FooBuilder()).name(name)
            }
            .map { Foo.FooBuilder builder ->
                return new StreamCollector<Foo.FooBuilder, Object>(builder)
            }
            .map { StreamCollector<Foo.FooBuilder, Object> fooStreams ->
                fooStreams.addStream(
                        ExampleRemoteService.getBarNamesForFoo(fooStreams.subject.name)
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
                                    ExampleRemoteService.getBazNamesForBar(barStreams.subject.name)
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
                        ExampleRemoteService.getTrotNamesForFoo(fooStreams.subject.name)
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


}
