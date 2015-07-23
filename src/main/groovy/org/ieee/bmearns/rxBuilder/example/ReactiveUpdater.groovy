package org.ieee.bmearns.rxBuilder.example

import rx.Observable
import rx.observables.BlockingObservable


class ReactiveUpdater<T> {
    final T subject
    Collection<Observable> updaters;

    public ReactiveUpdater(T subject) {
        this.subject = subject
        this.updaters = new HashSet<>()
    }

    public addUpdater(UpdaterStreamFactory<T, ?>... updaters) {
        updaters.each {
            this.updaters.add(
                it.buildUpdaterStream(subject)
            )
        }
    }

    public void waitFor() {
        Observable merged
        if(this.updaters.isEmpty()) {
            merged = Observable.from([]);
        } else {
            Iterator<Observable> streamIter = updaters.iterator()
            Observable merged = streamIter.next()
            for(Observable stream : streamIter ) {
                merged = merged.mergeWith(stream)
            }
        }
        BlockingObservable.from(merged.toList()).last()
    }
}
