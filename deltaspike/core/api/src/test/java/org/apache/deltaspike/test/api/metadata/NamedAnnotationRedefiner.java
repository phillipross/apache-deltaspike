/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.deltaspike.test.api.metadata;

import org.apache.deltaspike.core.api.literal.AlternativeLiteral;
import org.apache.deltaspike.core.api.literal.ApplicationScopedLiteral;
import org.apache.deltaspike.core.api.literal.NamedLiteral;
import org.apache.deltaspike.core.api.metadata.AnnotationRedefiner;
import org.apache.deltaspike.core.api.metadata.RedefinitionContext;
import org.apache.deltaspike.core.api.metadata.builder.AnnotationBuilder;

import javax.inject.Named;

/**
 *
 */
public class NamedAnnotationRedefiner implements AnnotationRedefiner
{
    @Override
    public void redefine(RedefinitionContext ctx)
    {
        Named named = ctx.getAnnotatedElement().getAnnotation(Named.class);
        if ("cat".equals(named.value()))
        {
            AnnotationBuilder builder = ctx.getAnnotationBuilder();
            // add two annotations
            builder.add(new AlternativeLiteral());
            builder.add(new ApplicationScopedLiteral());
            // change the value of @Named
            builder.remove(Named.class);
            builder.add(new NamedLiteral("tomcat"));
        }
    }
}
