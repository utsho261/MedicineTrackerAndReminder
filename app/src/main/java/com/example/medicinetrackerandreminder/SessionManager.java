package com.example.medicinetrackerandreminder;

public class SessionManager {
    private static long userId = -1;
    public static void set(long id) { userId = id; }
    public static long get() { return userId; }
}