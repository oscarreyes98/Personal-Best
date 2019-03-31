package edu.personalbest.ucsd.personalbest;

/**
 * Helper class for firebase return values (as opposed to the observer approach)
 * @param <T> the data to return
 */
interface Consumer<T> {

    /**
     * Accepts result of process (e.g. Firebase requesting chats for specific friend)
     * @param arg data
     */
    void accept(T arg);

    /**
     * Indicates that the requested information cannot be acquired
     */
    void reject();
}
