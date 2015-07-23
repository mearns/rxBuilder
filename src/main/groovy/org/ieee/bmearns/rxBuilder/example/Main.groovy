package org.ieee.bmearns.rxBuilder.example

import org.ieee.bmearns.rxBuilder.example.domain.*
import org.ieee.bmearns.rxBuilder.example.remote.ExampleRemoteService

import rx.Observable
import rx.functions.Func1
import rx.functions.Func2

class Main {

    public static void main(String[] args) {

        Observable<Foo.FooBuilder> seedStream = ExampleRemoteService.getFooNames()
            .map {
                return new Foo.FooBuilder().name(it)
            }

        new StreamingBuilder<Foo.FooBuilder>(seedStream)

            //Update the bar properties.
            .updateStream({ Foo.FooBuilder fooBuilder ->
                ExampleRemoteService.getBarNamesForFoo(fooBuilder.name)
            }  as Func1<Foo.FooBuilder, Observable<String>>)
                .apply({ Foo.FooBuilder fooBuilder, String barName ->
                    fooBuilder.bar(new Bar.BarBuilder().name(barName))
                } as Func2<Foo.FooBuilder, String, ?>)

            //Update the trot properties.
            .updateStream({ Foo.FooBuilder fooBuilder ->
                ExampleRemoteService.getTrotNamesForFoo(fooBuilder.name)
            }  as Func1<Foo.FooBuilder, Observable<String>>)
                .apply({ Foo.FooBuilder fooBuilder, String trotName ->
                    fooBuilder.trot(new Trot.TrotBuilder().name(trotName))
                })

            .stream()
            .subscribe (
                { println it.build() },
                { it.printStackTrace() },
                { println "Done"}
            )
    }


}
