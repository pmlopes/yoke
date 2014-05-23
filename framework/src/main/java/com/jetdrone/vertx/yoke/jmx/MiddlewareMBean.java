package com.jetdrone.vertx.yoke.jmx;

import com.jetdrone.vertx.yoke.core.MountedMiddleware;

import javax.management.*;

public final class MiddlewareMBean implements DynamicMBean {

    private final MountedMiddleware middleware;

    public MiddlewareMBean(MountedMiddleware middleware) {
        this.middleware = middleware;
    }

    @Override
    public synchronized Object getAttribute(String name) throws AttributeNotFoundException {
        switch (name) {
            case "mount":
                return middleware.mount;
            case "type":
                return middleware.middleware.getClass().getName();
            case "enabled":
                return middleware.enabled;
            default:
                throw new AttributeNotFoundException("No such property: " + name);
        }
    }

    @Override
    public synchronized void setAttribute(Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
        try {
            switch (attribute.getName()) {
                case "enabled":
                    middleware.enabled = (Boolean) attribute.getValue();
                    break;
                default:
                    throw new AttributeNotFoundException("No such property: " + attribute.getName());
            }
        } catch (RuntimeException e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (String name : names) {
            try {
                switch (name) {
                    case "mount":
                        list.add(new Attribute("mount", getAttribute(name)));
                        break;
                    case "type":
                        list.add(new Attribute("type", getAttribute(name)));
                        break;
                    case "enabled":
                        list.add(new Attribute("enabled", getAttribute(name)));
                        break;
                }
            }catch (AttributeNotFoundException e) {
                // ignore
            }
        }
        return list;
    }

    @Override
    public synchronized AttributeList setAttributes(AttributeList list) {
        return new AttributeList();
    }

    @Override
    public Object invoke(String name, Object[] args, String[] sig) throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attrs = {
                new MBeanAttributeInfo(
                        "mount",
                        "java.lang.String",
                        "Middleware Mount",
                        true,   // isReadable
                        false,   // isWritable
                        false), // isIs
                new MBeanAttributeInfo(
                        "type",
                        "java.lang.String",
                        "Middleware Type",
                        true,   // isReadable
                        false,   // isWritable
                        false), // isIs
                new MBeanAttributeInfo(
                        "enabled",
                        "java.lang.Boolean",
                        "Middleware Enabled",
                        true,   // isReadable
                        true,   // isWritable
                        true), // isIs
        };

        return new MBeanInfo(
                this.getClass().getName(),
                "Middleware Manager MBean",
                attrs,
                null,   // constructors
                null,   // operations
                null);  // notifications
    }
}
