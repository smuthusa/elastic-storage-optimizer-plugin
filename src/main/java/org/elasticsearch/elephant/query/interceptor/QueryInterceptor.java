package org.elasticsearch.elephant.query.interceptor;

public interface QueryInterceptor<T> {

    void interceptQuery(T request);
}
