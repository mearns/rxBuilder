package com.commercehub.rxbuilder.demo

import com.commercehub.rxbuilder.StreamingBuilder

import com.commercehub.rxbuilder.demo.domain.Bar
import com.commercehub.rxbuilder.demo.domain.Baz
import com.commercehub.rxbuilder.demo.domain.Foo
import com.commercehub.rxbuilder.demo.domain.Trot
import com.commercehub.rxbuilder.demo.remote.ExampleRemoteService

import rx.Observable

class Main {

    public static void main(String[] args) {

        Observable<Foo.FooBuilder> seedStream = ExampleRemoteService.getFooNames()
            .map {
                return new Foo.FooBuilder().name(it)
            }

        new StreamingBuilder<Foo.FooBuilder>(seedStream)

            //Update the bar properties.
            .updateStream { fooBuilder ->

                new StreamingBuilder<Bar.BarBuilder>(
                    ExampleRemoteService.getBarNamesForFoo(fooBuilder.name)
                        .map { barName ->
                            new Bar.BarBuilder().name(barName)
                        }
                )

                //Update the baz properties.
                .updateStream { barBuilder ->
                    ExampleRemoteService.getBazNamesForBar(barBuilder.name)
                }
                .apply { barBuilder, bazName ->
                    barBuilder.baz(new Baz.BazBuilder().name(bazName))
                }
                .stream()
            }
            .apply { fooBuilder, barBuilder ->
                fooBuilder.bar(barBuilder)
            }

            //Update the trot properties.
            .updateStream { fooBuilder ->
                ExampleRemoteService.getTrotNamesForFoo(fooBuilder.name)
            }
            .apply { fooBuilder, trotName ->
                fooBuilder.trot(new Trot.TrotBuilder().name(trotName))
            }

            .stream()
            .subscribe (
                { println it.build() },
                { it.printStackTrace() },
                { println "Done"}
            )
    }


}
