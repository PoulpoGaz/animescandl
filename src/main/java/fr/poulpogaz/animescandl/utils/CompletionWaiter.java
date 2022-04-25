package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.website.WebsiteException;

public class CompletionWaiter<T> {

    private T value = null;
    private volatile boolean completed = false;

    public T waitUntilCompletion(long timeout) throws WebsiteException, InterruptedException {
        if (timeout > 0) {
            long start = System.currentTimeMillis();

            while (start + timeout > System.currentTimeMillis() && !completed) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                } else {
                    Thread.onSpinWait();
                }
            }

            if (!completed) {
                throw new WebsiteException("Timeout");
            }

        } else {
            while (!completed) {
                Thread.onSpinWait();
            }
        }

        return value;
    }

    public void complete(T value) {
        if (!completed) {
            this.value = value;
            complete();
        }
    }

    public void complete() {
        completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }
}
