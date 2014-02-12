/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetdrone.vertx.yoke.extras.engine;

import com.jetdrone.vertx.yoke.engine.AbstractEngine;
import com.jetdrone.vertx.yoke.core.YokeAsyncResult;
import com.jetdrone.vertx.yoke.core.Context;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.ReaderTemplateLoader;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import java.io.StringReader;
import java.util.Map;

public class Jade4JEngine extends AbstractEngine<JadeTemplate> {

    private JadeConfiguration config = new JadeConfiguration();

    @Override
    public void render(final String filename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        read(filename, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    next.handle(new YokeAsyncResult<Buffer>(asyncResult.cause()));
                } else {
                    try {
                        JadeTemplate template = getTemplateFromCache(filename);
                        if(template == null) {
                            config.setTemplateLoader(new ReaderTemplateLoader(new StringReader(asyncResult.result()), filename));
                            template = config.getTemplate(filename);
                            putTemplateToCache(filename, template);
                        }
                        next.handle(new YokeAsyncResult<>(new Buffer(config.renderTemplate(template, context != null ? ((Context)context).getHashMap() : null))));
                    } catch (Exception ex) {
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
                                        	// we now have all the ingredients, so use compileWithLayout                                        	
                                        	JadeTemplate template = compileWithLayout(filename,layoutFilename, asyncResult2.result(), layoutTemplateText);
                                        	next.handle(new YokeAsyncResult<>(new Buffer(config.renderTemplate(template,  context != null ? ((Context)context).getHashMap() : null))));
                                            
                                        } catch (IOException | ClassNotFoundException ex) {
                                            next.handle(new YokeAsyncResult<Buffer>(ex));
                                        }
                                    }
                                }
                            });                    		
                    	
                        
                    } catch (Exception ex) {
                        next.handle(new YokeAsyncResult<Buffer>(ex));
                    }
                }
            }
        });       
    	
    }  
    
    private JadeTemplate compileWithLayout(String filename, String layoutFilename, String templateText, String layoutTemplateText) throws IOException, ClassNotFoundException {
        
    	// template + layout cache (cache-wise) will look similar to:
    	// sales.html-WithLayout-ecommerce.html
    	final StringBuilder compositeTemplateName = new StringBuilder();
    	compositeTemplateName.append(filename).append(KEY_TAG_FOR_TEMPLATE_NAME_WITH_LAYOUT).append(layoutFilename);
    	
    	final String compositeTemplateNameString = compositeTemplateName.toString();
    	    	
    	JadeTemplate compositeTemplate = getTemplateFromCache(compositeTemplateNameString);

        if (compositeTemplate == null) {
        	
        	// We need to find something like #{TemplateBody} inside the 
        	// layoutTemplateText, and replace it with the entire templateText
        	// this way we will create a single, unified template, comprising both layout and the main body        	
        	String fullTemplateText = layoutTemplateText.replace(PLACEHOLDER_TEMPLATE_BODY, templateText);
        	
            config.setTemplateLoader(new ReaderTemplateLoader(new StringReader(fullTemplateText), compositeTemplateNameString));
            compositeTemplate = config.getTemplate(compositeTemplateNameString);
            
            putLayoutTemplateToCache(compositeTemplateNameString, fullTemplateText, compositeTemplate);
        }

        return compositeTemplate;
    }      

}
