package org.ieee.bmearns.rxBuilder.example

import rx.Observable
import rx.functions.Func1

/**
 * @author bmearns
 */
abstract class UpdaterStreamFactory<T, U> {

    /**
     * An update stream is a stream that emits update objects of type {@code U}
     * for a particular update subject, of type {@code T}.
     *
     * Concrete classes must implement this method in order to return a stream of updates
     * for a given object.
     *
     * @see #buildUpdaterStream(T)
     * @see #doUpdate(T, U)
     *
     * @param subject The object for which updates will be emitted by the returned stream.
     * @return A stream that emit update objects of type {@code U}, for the given {@code subject}.
     */
    public abstract Observable<U> getUpdateStream(T subject);

    /**
     * Called by the {@linkplain #buildUpdaterStream(T) updater stream} to apply the given {@code update} object
     * to the given {@code subject}.
     *
     * @param subject The object to be updated by the {@code update} object.
     * @param update An object representing the update to be applied to the {@code subject}.
     *
     * @see #update
     */
    public abstract void doUpdate(T subject, U update);

    /**
     * A helper method that uses {@link #doUpdate(T, U)} to update the given {@code subject} with the given
     * {@code update} object, and return an {@link EmittedUpdaterItem#Update} {@link EmittedUpdaterItem} to encapsulate
     * the update.
     *
     * This is used by {@link #buildUpdaterStream(T)} to map the {@linkplain #getUpdateStream(T) <em>update</em> stream}
     * into an {@linkplain #buildUpdaterStream(T) <em>updater</em> stream}.
     *
     * @param subject The object to be updated by the {@code update} object.
     * @param update An object representing the update to be applied to the {@code subject}.
     *
     * @return An {@link EmittedUpdaterItem#Update} {@link EmittedUpdaterItem} which encapsulates the update applied.
     */
    protected EmittedUpdaterItem<T, U> update(T subject, U update) {
        this.doUpdate(subject, update);
        return EmittedUpdaterItem.<T, U>Update(subject, update)
    }

    /**
     * Creates and returns an <em>updater stream</em> to {@linkplain #doUpdate(T, U) update} the given source.
     *
     * An <em>updater</em> stream transforms an {@linkplain #getUpdateStream(T) <em>update</em> stream} into a stream which
     * calls {@link #update(T, U)} on the given {@code subject} for each update object emitted by the
     * <em>update</em> stream, and emits the resulting {@link EmittedUpdaterItem} objects.
     *
     * Additionally, the returned <em>updater</em> stream will emit a {@link EmittedUpdaterItem#Subject(java.lang.Object) Subject}
     * {@link EmittedUpdaterItem} as the last emitted item. This guarantees that even if there are no updates, the <em>updater</em>
     * stream will emit at least one item.
     *
     * @param subject The subject object to get an updater stream for.
     *
     * @return The updater stream for the given {@code subject}.
     */
    public Observable<EmittedUpdaterItem<T,U>> buildUpdaterStream(T subject) {
        getUpdateStream(subject)
            .<EmittedUpdaterItem<T,U>>map({ U update ->
                this.update(subject, update)
            })
            .concatWith(Observable.just(EmittedUpdaterItem.<T,U>Subject(subject)))
    }

    /**
     * A {@code EmittedUpdaterItem} is an item emitted by an {@linkplain #buildUpdaterStream(T) updater stream}.
     * it encapsulates either an <em>update</em> object, of type {@code U}, or the final <em>subject</em> object
     * (the object which is updated with the update objects), of type {@code T}.
     *
     * @param < T > The type of the subject.
     * @param < U > The type of the update objects.
     */
    public static class EmittedUpdaterItem<T, U> {

        /**
         * The subject is the object which is updated with the update objects.
         * This should always be set, even if the item is an {@linkplain #isUpdate update}.
         *
         * @see #isUpdate
         */
        final T subject

        /**
         * The update object, which is used to update the subject. This is {@code null} if the item represent a {@link #subject}
         * object.
         *
         * @see #isUpdate
         */
        final U update

        /**
         * Indicates whether this item represents an update object, in which case the {@link #update} property
         * is set. Otherwise the item represents a subject, in which case the {@link #update} property is {@code null}.
         */
        final boolean isUpdate

        /**
         * @param subject The {@link #subject} of the updates.
         * @param update The {@link #update} object, if this item is an {@linkplain #isUpdate update}. Otherwise {@code null}.
         * @param isUpdate Whether ({@code true}) or not ({@code false}) the item represents an update. This should be
         *  {@code true} if <em>and only if</em> {@code update} is {@code null}.
         */
        protected EmittedUpdaterItem(T subject, U update, boolean isUpdate) {
            this.subject = subject
            this.update = update
            this.isUpdate = isUpdate
        }

        /**
         * Factory method to create an item representing an update.
         *
         * @param subject The object which is updated with the {@code update} object.
         * @param update The update object, which updates the {@code subject}.
         *
         * @return A new {@link EmittedUpdaterItem<T, U>}, with the given {@link #subject}
         *   and {@link #update}, and the {@link #isUpdate} set to {@code true}.
         */
        static <T, U> EmittedUpdaterItem Update(T subject, U update) {
            return new EmittedUpdaterItem<T, U>(subject, update, true)
        }

        /**
         * Factory method to create an item representing just the subject, without an update.
         *
         * @param subject The object which is updated by the previously emitted update objects.
         *
         * @return A new {@link EmittedUpdaterItem<T, U>}, with the given {@link #subject},
         *   the {@link #update} set to {@code null}, and the {@link #isUpdate} set to {@code false}.
         */
        static <T, U> EmittedUpdaterItem Subject(T subject) {
            return new EmittedUpdaterItem<T, U>(subject, null, false)
        }
    }
}
