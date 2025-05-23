package org.icann.rdapconformance.validator;

import java.util.ArrayList;
import java.util.List;

public class StatusCodes {
    private static final StatusCodes Instance = new StatusCodes();
    private final List<Integer> statusCodes;

    private StatusCodes() {
        this.statusCodes = new ArrayList<>();
    }

    public static StatusCodes getInstance() {
        return Instance;
    }

    // Static method to add a status code
    public static void add(int statusCode) {
        Instance.addStatusCode(statusCode);
    }

    // Static method to get all status codes
    public static List<Integer> getAll() {
        return Instance.getAllStatusCodes();
    }

    // Static method to clear all status codes
    public static void clearAll() {
        Instance.clear();
    }

    // Instance method to add a status code
    private synchronized void addStatusCode(int statusCode) {
        statusCodes.add(statusCode);
    }

    private synchronized List<Integer> getAllStatusCodes() {
        return new ArrayList<>(statusCodes);
    }

    private synchronized void clear() {
        statusCodes.clear();
    }
}