//
// $Id$

package com.jetdrone.vertx.yoke.mustache;

/**
 * An exception thrown if we encounter an error while parsing a template.
 */
public class MustacheParseException extends MustacheException
{
    public MustacheParseException (String message) {
        super(message);
    }

    public MustacheParseException (String message, int lineNo) {
        super(message + " @ line " + lineNo);
    }
}
