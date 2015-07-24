package org.ieee.bmearns.rxBuilder.example

import rx.Observable
import rx.functions.Func1

class DefaultUpdateStreamFactory<T,S> implements UpdateStreamFactory<T,S> {

    final Func1<T, Observable<S>> factoryFunc

    DefaultUpdateStreamFactory(Func1<T, Observable<S>> factoryFunc) {
        this.factoryFunc = factoryFunc
    }

    DefaultUpdateStreamFactory(Closure<Observable<S>> factoryFunc) {
        this(factoryFunc as Func1<T, Observable<S>>)
    }

    @Override
    Observable<S> getUpdateStream(T source) {
        return factoryFunc.call(source);
    }
}
