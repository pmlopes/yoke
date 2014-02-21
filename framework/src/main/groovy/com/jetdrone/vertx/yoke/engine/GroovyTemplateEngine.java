/**
 * Copyright 2011-2014 the original author or authors.
 */
package com.jetdrone.vertx.yoke.engine;

import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import groovy.lang.MissingPropertyException;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilationFailedException;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GroovyTemplateEngine extends AbstractEngine<Template> {

    private static final String placeholderPrefix = "${";
    private static final String placeholderSuffix = "}";
    
    private static final String PLACEHOLDER_TEMPLATE_BODY = placeholderPrefix + KEY_FOR_TEMPLATE_BODY_INSIDE_LAYOUT + placeholderSuffix;

    private final TemplateEngine engine = new SimpleTemplateEngine();

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        read(filename, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<Buffer>(asyncResult.cause()));
                } else {
                    try {
                        Buffer result = internalRender(compile(filename, asyncResult.result()), context);
                        next.handle(new YokeAsyncResult<>(result));
                    } catch (CompilationFailedException | ClassNotFoundException | MissingPropertyException | IOException ex) {
                        next.handle(new YokeAsyncResult<Buffer>(ex));
                    }
                }
            }
        });
    }
    
@Override
    public void render(final String filename, final String layoutFilename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        
    	// get the layout template first
        read(layoutFilename, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<Buffer>(asyncResult.cause()));
                } else {
                    try {
                    		
                    		// we got the layout content based on the filename, let's save it and move further
                    		// and obtain the main template's body
                    		final String layoutTemplateText = asyncResult.result();
                    		
                            read(filename, new AsyncResultHandler<String>() {
                                @Override
                                public void handle(AsyncResult<String> asyncResult2) {
                                    if (asyncResult2.failed()) {
                                        next.handle(new YokeAsyncResult<Buffer>(asyncResult2.cause()));
                                    } else {
                                        try {
                                        	// we now have all the ingredients, so use compileWithLayout instead of compile
                                        	
                                            Buffer result = internalRender(compileWithLayout(filename, layoutFilename, asyncResult2.result(), layoutTemplateText ), context);
                                            next.handle(new YokeAsyncResult<>(result));
                                        } catch (CompilationFailedException | ClassNotFoundException | MissingPropertyException | IOException ex) {
                                            next.handle(new YokeAsyncResult<Buffer>(ex));
                                        }
                                    }
                                }
                            });                    		
                    	
                        
                    } catch (CompilationFailedException | MissingPropertyException ex) {
                        next.handle(new YokeAsyncResult<Buffer>(ex));
                    }
                }
            }
        });    	
    }    

    private Template compile(String filename, String templateText) throws IOException, ClassNotFoundException {
        Template template = getTemplateFromCache(filename);

        if (template == null) {
            // real compile
            template = engine.createTemplate(templateText);
            putTemplateToCache(filename, template);
        }

        return template;
    }
    
    private Template compileWithLayout(String filename, String layoutFilename, String templateText, String layoutTemplateText) throws IOException, ClassNotFoundException {
        
    	// template + layout cache (cache-wise) will look similar to:
    	// sales.html-WithLayout-ecommerce.html
    	final StringBuilder compositeTemplateName = new StringBuilder();
    	compositeTemplateName.append(filename).append(KEY_TAG_FOR_TEMPLATE_NAME_WITH_LAYOUT).append(layoutFilename);
    	
    	final String compositeTemplateNameString = compositeTemplateName.toString();
    	    	
    	Template compositeTemplate = getTemplateFromCache(compositeTemplateNameString);

        if (compositeTemplate == null) {
        	
        	// We need to find something like ${TemplateBody} or $TemplateBody inside the 
        	// layoutTemplateText, and replace it with the entire templateText
        	// this way we will create a single, unified template, comprising both layout and the main body        	
        	String fullTemplateText = layoutTemplateText.replace(PLACEHOLDER_TEMPLATE_BODY, templateText);
        	
            compositeTemplate = engine.createTemplate(fullTemplateText);
            putLayoutTemplateToCache(compositeTemplateNameString, fullTemplateText, compositeTemplate);
        }

        return compositeTemplate;
    }     

    private static Buffer internalRender(Template template, final Map<String, Object> context) throws IOException {
        final StringBuilder buffer = new StringBuilder();

        template.make(context).writeTo(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                buffer.append(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {
                // noop
            }

            @Override
            public void close() throws IOException {
                // noop
            }
        });

        return new Buffer(buffer.toString());
    }
}
