package com.jetdrone.vertx.yoke.jmx;

import com.jetdrone.vertx.yoke.IMiddleware;
import com.jetdrone.vertx.yoke.Middleware;

import javax.management.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RouteMBean implements DynamicMBean {

    private final List<IMiddleware> middleware;

    private final Pattern middlewarePattern = Pattern.compile("middleware\\[(\\d+)\\]");

    public RouteMBean(List<IMiddleware> middleware) {
        this.middleware = middleware;
    }

    @Override
    public synchronized Object getAttribute(String name) throws AttributeNotFoundException {
        Matcher m = middlewarePattern.matcher(name);
        if (m.matches()) {
            return middleware.get(Integer.parseInt(m.group(1))).getClass().getName();
        }

        throw new AttributeNotFoundException("No such property: " + name);
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new MBeanException(new UnsupportedOperationException());
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            }catch (AttributeNotFoundException e) {
                // ignore
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return new AttributeList();
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        throw new MBeanException(new UnsupportedOperationException());
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(
                this.getClass().getName(),
                "Pattern Binding Manager MBean",
                getAttributes(),    // attributes
                null,               // constructors
                null,               // operations
                null);              // notifications

    }

    private MBeanAttributeInfo[] getAttributes() {

        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[middleware.size()];

        for (int i = 0; i < middleware.size(); i++) {
            attrs[i] = new MBeanAttributeInfo(
                    "middleware[" + i + "]",
                    "java.lang.String",
                    "Middleware mounted in chain at position " + i,
                    true,
                    false,
                    false);
        }

        return attrs;
    }
}
