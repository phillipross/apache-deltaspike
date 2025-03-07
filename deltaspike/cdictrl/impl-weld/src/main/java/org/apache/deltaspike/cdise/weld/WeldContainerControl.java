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
package org.apache.deltaspike.cdise.weld;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.jboss.weld.Container;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.util.reflection.Formats;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;

/**
 * Weld specific implementation of {@link org.apache.deltaspike.cdise.api.CdiContainer}.
 */
@SuppressWarnings("UnusedDeclaration")
public class WeldContainerControl implements CdiContainer
{
    private static final Logger LOG = Logger.getLogger(WeldContainerControl.class.getName());

    private Weld weld;
    private WeldContainer weldContainer;

    private Bean<ContextControl> ctxCtrlBean = null;
    private CreationalContext<ContextControl> ctxCtrlCreationalContext = null;
    private ContextControl ctxCtrl = null;


    @Override
    public BeanManager getBeanManager()
    {
        if (weldContainer == null)
        {
            return null;
        }

        return weldContainer.getBeanManager();
    }


    @Override
    public synchronized void boot()
    {
        weld = new Weld();
        weldContainer = weld.initialize();
    }

    @Override
    public void boot(Map<?, ?> properties)
    {
        weld = new Weld();

        // setProperties only exists from Weld-2.x onwards
        setProperties(weld, convertProperties(properties));

        weldContainer = weld.initialize();
    }

    private void setProperties(Weld weld, Map<String, Object> properties)
    {
        if (properties == null || properties.isEmpty())
        {
            return;
        }

        Method setPropertiesMethod = extractMethod(Weld.class, "setProperties", Map.class);
        if (setPropertiesMethod != null)
        {
            if (!setPropertiesMethod.isAccessible())
            {
                setPropertiesMethod.setAccessible(true);
            }

            try
            {
                setPropertiesMethod.invoke(weld, properties);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            LOG.warning("No setProperties method available on this version of Weld - ignoring passed in properties!");
        }
    }

    /**
     * Extract a method with the given name and parameterTypes.
     * Return {@code null} if no such visible method exists on the given class.
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private static Method extractMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)
    {
        try
        {
            return clazz != null ? clazz.getMethod(methodName, parameterTypes) : null;
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }


    @Override
    public synchronized  void shutdown()
    {
        if (ctxCtrl != null)
        {
            try
            {
                // stops all built-in contexts except for ApplicationScoped as that one is handled by Weld
                ctxCtrl.stopContext(ConversationScoped.class);
                ctxCtrl.stopContext(RequestScoped.class);
                ctxCtrl.stopContext(SessionScoped.class);
                ctxCtrlBean.destroy(ctxCtrl, ctxCtrlCreationalContext);
            }
            catch (Exception e)
            {
                // contexts likely already stopped
            }
        }
        try
        {
            weld.shutdown();
        }
        catch (Exception e)
        {
            // something caused weld to shutdown already.
        }
        weld = null;
        ctxCtrl = null;
        ctxCtrlBean = null;
        ctxCtrlCreationalContext = null;

    }

    @Override
    public synchronized ContextControl getContextControl()
    {
        if (ctxCtrl == null)
        {
            BeanManager beanManager = getBeanManager();

            if (beanManager == null)
            {
                LOG.warning("If the CDI-container was started by the environment, you can't use this helper." +
                        "Instead you can resolve ContextControl manually " +
                        "(e.g. via BeanProvider.getContextualReference(ContextControl.class) ). " +
                        "If the container wasn't started already, you have to use CdiContainer#boot before.");

                return null;
            }
            Set<Bean<?>> beans = beanManager.getBeans(ContextControl.class);
            ctxCtrlBean = (Bean<ContextControl>) beanManager.resolve(beans);

            ctxCtrlCreationalContext = getBeanManager().createCreationalContext(ctxCtrlBean);

            ctxCtrl = (ContextControl)
                    getBeanManager().getReference(ctxCtrlBean, ContextControl.class, ctxCtrlCreationalContext);
        }
        return ctxCtrl;
    }


    @Override
    public String toString()
    {
        return "WeldContainerControl [Weld " + Formats.version(Container.class.getPackage()) + ']';
    }
    
    private static Map<String, Object> convertProperties(final Map<?, ?> map)
    {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

}
