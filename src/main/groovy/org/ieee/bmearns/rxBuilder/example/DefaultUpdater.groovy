
package org.ieee.bmearns.rxBuilder.example

import rx.functions.Func2

class DefaultUpdater<T, U> implements Updater<T, U> {

    Func2<T, U, ?> updateFunc;

    DefaultUpdater(Func2<T, U, ?> updateFunc) {
        this.updateFunc = updateFunc;
    }

    DefaultUpdater(Closure<?> updateFunc) {
        this.updateFunc = (updateFunc as Func2<T, U, ?>);
    }

    public void update(T subject, U update) {
        updateFunc(subject, update);
    }
}
