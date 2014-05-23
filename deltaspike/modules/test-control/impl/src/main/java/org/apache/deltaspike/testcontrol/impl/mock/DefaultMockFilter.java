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
package org.apache.deltaspike.testcontrol.impl.mock;

import org.apache.deltaspike.testcontrol.spi.mock.MockFilter;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Set;
import java.util.logging.Logger;

public class DefaultMockFilter implements MockFilter
{
    private static final Logger LOG = Logger.getLogger(DefaultMockFilter.class.getName());

    private static final String DS_BASE_PACKAGE = "org.apache.deltaspike.";
    private static final String JAVA_BASE_PACKAGE = "java.";
    private static final String JAVAX_BASE_PACKAGE = "javax.";
    private static final String OWB_BASE_PACKAGE = "org.apache.webbeans.";
    private static final String WELD_BASE_PACKAGE = "org.jboss.weld.";

    @Override
    public boolean isMockedImplementationSupported(BeanManager beanManager, Annotated annotated)
    {
        Class origin = null;
        if (annotated instanceof AnnotatedType)
        {

            origin = ((AnnotatedType)annotated).getJavaClass();
            if (isAnnotatedTypeWithInterceptorAnnotation(beanManager, annotated.getAnnotations(), origin.getName()))
            {
                return false;
            }
        }
        else if (annotated instanceof AnnotatedMember)
        {
            Member member = ((AnnotatedMember)annotated).getJavaMember();
            origin = member.getDeclaringClass();
            if (isAnnotatedTypeWithInterceptorAnnotation(beanManager, annotated.getAnnotations(), member.toString()))
            {
                return false;
            }
        }

        return origin != null && !isInternalPackage(origin.getPackage().getName());
    }

    protected boolean isAnnotatedTypeWithInterceptorAnnotation(BeanManager beanManager,
                                                               Set<Annotation> annotations,
                                                               String origin)
    {
        for (Annotation annotation : annotations)
        {
            if (isStandardAnnotation(annotation))
            {
                continue;
            }

            if (beanManager.isInterceptorBinding(annotation.annotationType()) ||
                    (beanManager.isStereotype(annotation.annotationType()) &&
                            isStereotypeWithInterceptor(annotation, beanManager)))
            {
                LOG.warning("Skip mocking intercepted bean " + origin);

                return true;
            }
        }
        return false;
    }

    protected boolean isStereotypeWithInterceptor(Annotation stereotypeAnnotation, BeanManager beanManager)
    {
        for (Annotation annotation : stereotypeAnnotation.annotationType().getAnnotations())
        {
            if (isStandardAnnotation(annotation))
            {
                continue;
            }

            if (beanManager.isInterceptorBinding(annotation.annotationType()) ||
                    isStereotypeWithInterceptor(annotation, beanManager))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isStandardAnnotation(Annotation annotation)
    {
        return annotation.annotationType().getName().startsWith(JAVA_BASE_PACKAGE) ||
                annotation.annotationType().getName().startsWith(JAVAX_BASE_PACKAGE);
    }

    protected boolean isInternalPackage(String packageName)
    {
        return packageName.startsWith(OWB_BASE_PACKAGE) || packageName.startsWith(WELD_BASE_PACKAGE) ||
            isDeltaSpikePackage(packageName);
    }

    protected boolean isDeltaSpikePackage(String packageName)
    {
        return packageName.startsWith(DS_BASE_PACKAGE);
    }
}
