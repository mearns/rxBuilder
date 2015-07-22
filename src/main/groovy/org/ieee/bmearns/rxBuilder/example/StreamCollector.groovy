package org.ieee.bmearns.rxBuilder.example

import rx.Observable

class StreamCollector<T, S> {

    T subject
    List<Observable<? extends S>> streams

    public StreamCollector(T subject) {
        this.subject = subject
        this.streams = new LinkedList<>()
    }

    public <R extends S> StreamCollector<S, T> addStream(Observable<R> stream) {
        streams.add(stream);
        return this;
    }

    public Observable<? extends S> mergeStreams() {
        if(this.streams.isEmpty()) {
            return Observable.<S>from([]);
        } else {
            Iterator<Observable<? extends S>> streamIter = streams.iterator()
            Observable<? extends S> mergedStream = streamIter.next()
            for(Observable<? extends S> stream : streamIter ) {
                mergedStream = mergedStream.mergeWith(stream)
            }
            return mergedStream
        }
    }

    public Observable<T> subjectStream() {
        mergeStreams()
            .last()
            .<T>map { this.subject }
    }



}
