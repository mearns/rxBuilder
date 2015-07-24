package org.ieee.bmearns.rxBuilder.example

import rx.functions.Func1
import rx.Observable
import rx.functions.Func2

/**
 * An object to create a stream that updates objects of type T, emitted from a source stream.
 *
 * Imagine you're building up an object by querying a REST API. The objects represent users so your first query fetches
 * a set of all users you're interested using some kind of searching or filtering query. The response looks like this:
 *
 * <pre> {@code
 * [1001, 1002, 1003]
 * } </pre>
 *
 * Unfortunately, this is a very lean API and doesn't provide much information. All it's given you is the IDs of the
 * three objects you asked about, it didn't give you any of the fields. To get those, you need to query the API again,
 * once for each object. And you get back responses like this:
 *
 * <pre> {@code
 * {
 *      "id": 1001,
 *      "username": "user1",
 *      "displayName": "User One",
 *      "profileId": 2001
 * }
 * } </pre>
 *
 * Hm, not much more information. We have a username and a displayName, but it looks like most of the information is
 * encapsulated in some kind of Profile object, and all we have is the ID for that object. To get that information,
 * we'll have to make another query.
 *
 * But let's just start with what we've got.
 *
 * The initial query produces what we call the <em>source stream</em>. It's a stream that emits all of the objects that
 * we want to update. At least, it will once you've turned it into an {@link Observable}). So let's do that:
 *
 * <pre> {@code
 * Future<Long[]> userIds = api.getUserIds();
 * Observable<Long> userIdStream = Observable.from(userIds);
 * Observable<User> userStream = userIdStream.map { Long id -> new User(id) }
 * } </pre>
 *
 * In the above code, {@code userStream} is our <em>source stream</em>, it will emit one {@code User} object for each
 * user that we care about, with the ID already set. Now that we have our source stream, we can create a {@link StreamingBuilder}
 * from it. This will allow us to easily manage updating our Users from additional streams.
 *
 * <pre> {@code
 * StreamingBuilder<User> userBuilder = new StreamingBuilder<>(userStream);
 * } </pre>
 *
 * Now for each of those Users, we want to run another query against the API to get the additional information about
 * the user, like "username" and "displayName". To do this, we're going to create an <em>update stream</em> for each user,
 * where each update stream will emit <em>update objects</em>, each of which will be used to update that user.
 *
 * Now, the {@code StreamingBuilder} doesn't know how to create this update stream, or how to actually update the {@code User}
 * objects with the update objects it emits. So you'll need to provide a closure to do each of those things.
 *
 * The first step is to provide a closure which will create an update stream for a given user, which is done using
 * the {@link StreamingBuilder#updateStream(rx.functions.Func1) updateStream({})} method:
 *
 * <pre> {@code
 * def updateStream = userBuilder.updateStream { User subject ->
 *     Future<Map<String, Object>> userDetails = api.getUserDetails(subject.getId());
 *     return Observable.from(userDetails);
 * }
 * }</pre>
 *
 * So now for each user emitted by the source stream, we know how to create another stream, called an update stream,
 * which will emit a {@code Map} with some additional user details. Now you need to provide the second part, a way
 * to actually update our {@code User} objects with those emitted details. You do this using the
 * {@link org.ieee.bmearns.rxBuilder.example.StreamingBuilder.UpdateStream#apply(rx.functions.Func2) apply({})} method:
 *
 * <pre> {@code
 * updateStream.apply { User subject, Map<String, Object> userDetails ->
 *      User.setUsername(Map.get("username"));
 *      User.setDisplayName(Map.get("displayName"));
 *      User.setProfileId(Map.get("profileId"));
 * }
 * } </pre>
 *
 * Simple enough.
 *
 * The {@link StreamingBuilder#updateStream(rx.functions.Func1) updateStream} method actually returns an {@link
 * org.ieee.bmearns.rxBuilder.example.StreamingBuilder.UpdateStream UpdateStream} object, but one that is tied to
 * the {@link StreamingBuilder} that created it ({@code userBuilder}, in this case). But you really don't need to
 * care about that, which is why we just showed a {@code def} for that variable above.
 *
 * The only thing that matters about the {@code BuildStream} is that it provides the
 * {@link org.ieee.bmearns.rxBuilder.example.StreamingBuilder.UpdateStream#apply(rx.functions.Func2) apply} method that
 * we used above. Conveniently, the {@code apply} method returns the {@code StreamingBuilder} itself, so we can chain
 * pairs of {@code updateStream} and {@code apply} calls together indefinitely. For starters, we'll rewrite the previous
 * three code blocks like this (along with some additional minor code clean up):
 *
 * <pre> {@code
 * StreamingBuilder<User> userBuilder = new StreamingBuilder<>(userStream)
 *      .updateStream { User subject ->
 *          return Observable.from(api.getUserDetails(subject.getId()));
 *      }
 *      .apply { User subject, Map<String, Object> userDetails ->
 *          User.setUsername(Map.get("username"));
 *          User.setDisplayName(Map.get("displayName"));
 *      }
 * } </pre>
 *
 * Great, so now you've provided a stream of Users, and some directions on how to update each one from a streaming
 * resource that provides some user details for each. To get the {@code User} objects themselves, use the
 * {@link StreamingBuilder#stream()}  stream()} method to get an {@link Observable} and subscribe to or transform it as
 * usual:
 *
 * <pre> {@code
 * userBuilder.stream().subscribe { User user ->
 *     println "User: " + user;
 * }
 * } </pre>
 *
 * Which might output something like this:
 *
 * <pre> {@code
 * User: User(id: 1001, username: "user1", displayName: "User One", profileId: 2001)
 * User: User(id: 1002, username: "user2", displayName: "User Two", profileId: 2002)
 * User: User(id: 1003, username: "user3", displayName: "User Three", profileId: 2003)
 * } </pre>
 *
 * Pretty exciting stuff.
 *
 * Now let's deal with that {@code profileId}.
 *
 * To do this, we're going to need another update stream, one which fetches the profile object from the API,
 * and uses it to update the the {@code User} object again. So we simply tack on another pair of {@code updateStream}
 * and {@code apply} calls:
 *
 * <pre> {@code
 * userBuilder
 *      .updateStream { User subject ->
 *          return Observable.from(api.getProfile(subject.getProfileId()));
 *      }
 *      . apply { User subject, Map<String, Object> profile ->
 *          subject.setRealName(profile.get("realName"))
 *          subject.setLocation(profile.get("location"))
 *          subject.setBiography(profile.get("biography"))
 *          // ... etc.
 *      }
 * } </pre>
 *
 * And that's all there is to that.
 *
 * All together, out code looks something like this:
 *
 * <pre> {@code
 * StreamingBuilder<User> userBuilder = new StreamingBuilder<>(userStream)
 *      //Update basic information
 *      .updateStream { User subject ->
 *          return Observable.from(api.getUserDetails(subject.getId()));
 *      }
 *      .apply { User subject, Map<String, Object> userDetails ->
 *          User.setUsername(Map.get("username"));
 *          User.setDisplayName(Map.get("displayName"));
 *      }
 *
 *      //Update profile information
 *      .updateStream { User subject ->
 *          return Observable.from(api.getProfile(subject.getProfileId()));
 *      }
 *      . apply { User subject, Map<String, Object> profile ->
 *          subject.setRealName(profile.get("realName"))
 *          subject.setLocation(profile.get("location"))
 *          subject.setBiography(profile.get("biography"))
 *          // ... etc.
 *      }
 *
 *      //And lastly, subscribe to the stream.
 *      .stream()
 *      .subscribe { User user ->
 *          println "User: " + user;
 *      }
 * } </pre>
 *
 * @param < T > The type of object built by the streaming builder.
 */
class StreamingBuilder<T> {

    private Observable<Subject<T>> stream

    StreamingBuilder(Observable<T> subjectSource) {
        this.stream = subjectSource.map { T source ->
            new Subject<T>(source)
        }
    }

    public <S> UpdateStream<S> updateStream(Func1<T, Observable<S>> updateSource) {
        return new UpdateStream<S>(updateSource)
    }

    public Observable<T> stream() {
        return stream.flatMap { Subject<T> subject ->
            subject.mergedStream.toList().map{ subject.subject }
        }
    }

    public class UpdateStream<S> {
        final Func1<T, Observable<S>> updateSource

        UpdateStream(Func1<T, Observable<S>> updateSource) {
            this.updateSource = updateSource
        }

        //XXX: Use an Updater interface for this.
        public StreamingBuilder<T> apply(Func2<T, S, ?> updateFunc) {
            stream = stream.map({ Subject<T> subject ->
                subject.addStream(
                    updateSource.call(subject.subject)
                        .map { S update ->
                            updateFunc.call(subject.subject, update)
                            return null
                        }
                )
            } as Func1<Subject<T>, Subject<T>>)

            return StreamingBuilder.this;
        }
    }

    private static class Subject<T> {
        final T subject
        Observable mergedStream

        public Subject(T subject) {
            this.subject = subject
            this.mergedStream = null
        }

        public Subject<T> addStream(Observable<Void> stream) {
            stream = stream.ignoreElements()
            if (this.mergedStream == null) {
                this.mergedStream = stream
            }
            else {
                this.mergedStream = this.mergedStream.mergeWith(stream)
            }
            return this;
        }
    }
}
