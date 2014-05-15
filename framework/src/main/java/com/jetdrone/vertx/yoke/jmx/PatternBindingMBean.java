package com.jetdrone.vertx.yoke.jmx;

import javax.management.*;

public class PatternBindingMBean implements DynamicMBean {
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {}

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(
                this.getClass().getName(),
                "Pattern Binding Manager MBean",
                null,   // attributes
                null,   // constructors
                null,   // operations
                null);  // notifications

    }
}
