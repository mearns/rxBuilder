package com.commercehub.rxbuilder

import rx.Observable

interface UpdateStreamFactory<T, S> {

    public Observable<S> getUpdateStream(T source);

}
