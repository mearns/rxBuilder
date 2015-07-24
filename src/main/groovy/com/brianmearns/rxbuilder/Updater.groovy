
package com.brianmearns.rxbuilder

interface Updater<T, U> {

    public void update(T subject, U update);

}
