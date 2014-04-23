package com.jetdrone.vertx.yoke.jmx;

import javax.management.*;
import java.util.*;

public class ContextMBean implements DynamicMBean {

    private final Map<String, Object> defaultContext;

    public ContextMBean(Map<String, Object> defaultContext) {
        this.defaultContext = defaultContext;
    }

    @Override
    public synchronized Object getAttribute(String name) throws AttributeNotFoundException {
        if (defaultContext.containsKey(name)) {
            return defaultContext.get(name);
        }

        throw new AttributeNotFoundException("No such property: " + name);
    }

    @Override
    public synchronized void setAttribute(Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
        String name = attribute.getName();

        if (defaultContext.containsKey(name)) {
            defaultContext.put(name, attribute.getValue());
        } else {
            throw new AttributeNotFoundException(name);
        }
    }

    @Override
    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (String name : names) {
            if (defaultContext.containsKey(name)) {
                list.add(new Attribute(name, defaultContext.get(name)));
            }

        }
        return list;
    }

    @Override
    public synchronized AttributeList setAttributes(AttributeList list) {
        for (Object attr : list) {
            String name = ((Attribute) attr).getName();
            Object value = ((Attribute) attr).getValue();

            if (defaultContext.containsKey(name)) {
                defaultContext.put(name, value);
            }
        }

        return list;
    }

    @Override
    public Object invoke(String name, Object[] args, String[] sig) throws MBeanException, ReflectionException {
//        if (name.equals("reload") &&
//                (args == null || args.length == 0) &&
//                (sig == null || sig.length == 0)) {
//            try {
//                load();
//                return null;
//            } catch (IOException e) {
//                throw new MBeanException(e);
//            }
//        }
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {

        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[defaultContext.size()];
        Iterator<String> keys = defaultContext.keySet().iterator();

        for (int i = 0; i < attrs.length; i++) {
            String name = keys.next();
            attrs[i] = new MBeanAttributeInfo(
                    name,
                    defaultContext.get(name).getClass().getName(),
                    "Default Context Property " + name,
                    true,
                    true,
                    defaultContext.get(name) instanceof Boolean);
        }

//        MBeanOperationInfo[] opers = {
//                new MBeanOperationInfo(
//                        "reload",
//                        "Reload properties from file",
//                        null,   // no parameters
//                        "void",
//                        MBeanOperationInfo.ACTION)
//        };
        return new MBeanInfo(
                this.getClass().getName(),
                "Property Manager MBean",
                attrs,
                null,  // constructors
                null,  // operations
                null); // notifications
    }
}
