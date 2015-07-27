
package com.commercehub.rxbuilder

interface Updater<T, U> {

    public void update(T subject, U update);

}
