// Name : pooja bandari
// Course: open - source intelligent device (ITMD- 555)
// Project : FitSnacks
package com.example.fitsnacks.util;

/**
 * Simple callback used to avoid java.util.function types which may require desugaring.
 */
public interface Callback<T> {
    void onComplete(T value);
}

