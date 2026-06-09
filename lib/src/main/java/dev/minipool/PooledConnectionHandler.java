package dev.minipool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

final class PooledConnectionHandler implements InvocationHandler {

    private final Connection real;
    private final MiniPool pool;

    private PooledConnectionHandler(Connection real, MiniPool pool) {
        this.real = real;
        this.pool = pool;
    }

    /** Wrap a real Connection so close() returns it to the pool. */
    static Connection wrap(Connection real, MiniPool pool) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] { Connection.class },
                new PooledConnectionHandler(real, pool)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("close")) {
            pool.returnToPool(real);
            return null;
        } else {
            return method.invoke(real, args);
        }

        // Future in step 4: method.invoke() wraps thrown exceptions in
        // InvocationTargetException. When tests start calling real JDBC methods,
        // we'll need to catch that and rethrow getCause()
    }
}