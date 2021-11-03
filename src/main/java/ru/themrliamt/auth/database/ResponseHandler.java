package ru.themrliamt.auth.database;

public interface ResponseHandler<H, R> {
    R handleResponse(H handle) throws Exception;
}
