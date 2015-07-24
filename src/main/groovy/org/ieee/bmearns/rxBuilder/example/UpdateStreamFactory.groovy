package org.ieee.bmearns.rxBuilder.example

interface UpdateStreamFactory<T, S> {

    public rx.Observable<S> getUpdateStream(T source);

}
