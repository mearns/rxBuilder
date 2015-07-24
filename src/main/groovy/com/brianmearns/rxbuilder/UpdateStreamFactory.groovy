package com.brianmearns.rxbuilder

interface UpdateStreamFactory<T, S> {

    public rx.Observable<S> getUpdateStream(T source);

}
