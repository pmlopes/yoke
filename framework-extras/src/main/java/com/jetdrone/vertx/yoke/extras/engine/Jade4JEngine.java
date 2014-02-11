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
                        next.handle(new YokeAsyncResult<>(new Buffer(config.renderTemplate(template, context))));
                    } catch (Exception ex) {
                        next.handle(new YokeAsyncResult<Buffer>(ex));
                    }
                }
            }
        });
    }
    
    @Override
    public void render(final String filename, final String layoutFilename, final Map<String, Object> context, final Handler<AsyncResult<Buffer>> next) {
        
    	// todo: implement proper layout support like in Groovy Template Engine   
    	
    }     

}
