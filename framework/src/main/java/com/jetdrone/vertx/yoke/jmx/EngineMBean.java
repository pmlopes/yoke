package com.jetdrone.vertx.yoke.jmx;

import com.jetdrone.vertx.yoke.Engine;
import com.jetdrone.vertx.yoke.core.MountedMiddleware;

import javax.management.*;
import java.util.*;

/**
 * Created by paulo on 4/22/14.
 */
public class EngineMBean implements DynamicMBean {

    private final List<MountedMiddleware> middlewares;

    public EngineMBean(List<MountedMiddleware> middlewares) {
        this.middlewares = middlewares;
    }

    public EngineMBean(Map<String, Engine> engineMap) {
        middlewares = null;
    }

    public synchronized String getAttribute(String name) throws AttributeNotFoundException {
        for (MountedMiddleware m : middlewares) {
            if (m.mount.equals(name)) {
                return m.middleware.getClass().getName();
            }
        }

        throw new AttributeNotFoundException("No such property: " + name);
    }

    public synchronized void setAttribute(Attribute attribute) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException {
//        String name = attribute.getName();
//        if (properties.getProperty(name) == null)
//            throw new AttributeNotFoundException(name);
//        Object value = attribute.getValue();
//        if (!(value instanceof String)) {
//            throw new InvalidAttributeValueException(
//                    "Attribute value not a string: " + value);
//        }
//        properties.setProperty(name, (String) value);
//        try {
//            save();
//        } catch (IOException e) {
//            throw new MBeanException(e);
//        }
    }

    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (MountedMiddleware m : middlewares) {
            list.add(new Attribute(m.mount, m.middleware.getClass().getName()));
        }
        return list;
    }

    public synchronized AttributeList setAttributes(AttributeList list) {
//        Attribute[] attrs = (Attribute[]) list.toArray(new Attribute[0]);
//        AttributeList retlist = new AttributeList();
//        for (Attribute attr : attrs) {
//            String name = attr.getName();
//            Object value = attr.getValue();
//            if (properties.getProperty(name) != null && value instanceof String) {
//                properties.setProperty(name, (String) value);
//                retlist.add(new Attribute(name, value));
//            }
//        }
//        try {
//            save();
//        } catch (IOException e) {
//            return new AttributeList();
//        }
//        return retlist;
        return null;
    }

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

    public synchronized MBeanInfo getMBeanInfo() {
        SortedSet<String> names = new TreeSet<>();

        for (MountedMiddleware m : middlewares) {
            names.add(m.mount);
        }

        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[names.size()];
        Iterator<String> it = names.iterator();
        for (int i = 0; i < attrs.length; i++) {
            String name = it.next();
            attrs[i] = new MBeanAttributeInfo(
                    name,
                    "java.lang.String",
                    "Middleware " + name,
                    true,   // isReadable
                    false,   // isWritable
                    false); // isIs
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
