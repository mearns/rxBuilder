package org.ieee.bmearns.rxBuilder.example.remote

import rx.Observable

/**
 * Represents some kind of service that we are querying to get data from to build our objects.
 */
class ExampleRemoteService {

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
