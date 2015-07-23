package org.ieee.bmearns.rxBuilder.example

import rx.functions.Func1
import rx.Observable
import rx.functions.Func2

class StreamingBuilder<T> {

    private Observable<T> stream

    StreamingBuilder(Observable<T> subjectSource) {
        this.stream = subjectSource
    }

    public <S> UpdateStream<S> updateStream(Func1<T, Observable<S>> updateSource) {
        return new UpdateStream<S>(updateSource)
    }

    public Observable<T> stream() {
        return stream
    }

    public class UpdateStream<S> {
        final Func1<T, Observable<S>> updateSource

        UpdateStream(Func1<T, Observable<S>> updateSource) {
            this.updateSource = updateSource
        }

        public StreamingBuilder<T> apply(Func2<T, S, ?> func) {
            stream = stream.flatMap({ T subject ->
                updateSource.call(subject)
                    .map { S update ->
                        func(subject, update)
                    }
                    .last()
                    .map {
                        return subject
                    }
            } as Func1<T, Observable<T>>)

            return StreamingBuilder.this;
        }
    }
}
