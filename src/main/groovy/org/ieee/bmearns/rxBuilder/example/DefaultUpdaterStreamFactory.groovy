package org.ieee.bmearns.rxBuilder.example

import rx.Observable

class DefaultUpdaterStreamFactory<T, U> extends UpdaterStreamFactory<T, U> {

    final Closure doUpdate
    final Closure<Observable<U>> getUpdateStream

    DefaultUpdaterStreamFactory(Closure doUpdate, Closure<Observable<U>> getUpdateStream) {
        this.doUpdate = doUpdate
        this.getUpdateStream = getUpdateStream
    }

    @Override
    Observable<U> getUpdateStream(T subject) {
        return this.getUpdateStream.call(subject)
    }

    @Override
    public void doUpdate(T subject, U update) {
        this.doUpdate.call(subject, update)
    }
}
