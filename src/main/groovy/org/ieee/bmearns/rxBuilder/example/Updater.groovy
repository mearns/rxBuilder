
package org.ieee.bmearns.rxBuilder.example

interface Updater<T, U> {

    public void update(T subject, U update);

}
