package com.kittyapplication.rest;

/**
 * Created by Scorpion on 03-08-2015.
 */
public class FinalWrapper<T> {
    public final T value;

    public FinalWrapper(T value) {
        this.value = value;
    }
}
