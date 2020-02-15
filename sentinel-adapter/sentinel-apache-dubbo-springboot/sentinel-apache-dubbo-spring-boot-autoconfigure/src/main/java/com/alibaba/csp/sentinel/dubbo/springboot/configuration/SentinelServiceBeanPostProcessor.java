/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dubbo.springboot.configuration;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.dubbo.springboot.annotation.DegradeRuleDefine;
import com.alibaba.csp.sentinel.dubbo.springboot.annotation.FallbackHandler;
import com.alibaba.csp.sentinel.dubbo.springboot.annotation.FlowRuleDefine;
import com.alibaba.csp.sentinel.dubbo.springboot.annotation.RateLimitFlowRuleDefine;
import com.alibaba.csp.sentinel.dubbo.springboot.annotation.WarmUpFlowRuleDefine;
import com.alibaba.csp.sentinel.dubbo.springboot.utils.ConstStrings;
import com.alibaba.csp.sentinel.dubbo.springboot.utils.ResourceUtils;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.INTERFACE_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;


/**
 * @author zhengzechao
 * @date 2020-02-15
 *
 * {@link BeanDefinitionRegistryPostProcessor Bean Definition Registry Post Processor}
 * The role of this SentinelServiceBeanPostProcessor is to process all Class annotated with
 * {@link com.alibaba.csp.sentinel.dubbo.springboot.annotation.Rule @Rule}
 * (i.e., a class annotated with
 * {@link com.alibaba.csp.sentinel.dubbo.springboot.annotation.DegradeRuleDefine @DegradeRuleDefine},
 * {@link com.alibaba.csp.sentinel.dubbo.springboot.annotation.FlowRuleDefine @FlowRuleDefine},
 * {@link com.alibaba.csp.sentinel.dubbo.springboot.annotation.RateLimitFlowRuleDefine @RateLimitFlowRuleDefine}
 * {@link com.alibaba.csp.sentinel.dubbo.springboot.annotation.WarmUpFlowRuleDefine @WarmUpFlowRuleDefine}, etc.)
 * and convert them to the corresponding rules of Sentinel Rule API. And also handle the annotation
 * {@link com.alibaba.csp.sentinel.dubbo.springboot.annotation.FallbackHandler @FallbackHandler} and register to
 * {@link FallbackManager}
 *
 */

public class SentinelServiceBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {


    private static final Logger LOGGER = LoggerFactory.getLogger(SentinelServiceBeanPostProcessor.class);


    // @VisibleForTesting
    FallbackManager providerFallbackManager;
    // @VisibleForTesting
    FallbackManager consumerFallbackManager;
    // @VisibleForTesting
    List<FlowRule> flowRules = new ArrayList<FlowRule>();
    // @VisibleForTesting
    List<DegradeRule> degradeRules = new ArrayList<DegradeRule>();

    private Environment environment;
    private EvaluationContext context;
    private SpelExpressionParser parser = new SpelExpressionParser();


    public SentinelServiceBeanPostProcessor(FallbackManager providerFallbackManager, FallbackManager consumerFallbackManager) {
        this.providerFallbackManager = providerFallbackManager;
        this.consumerFallbackManager = consumerFallbackManager;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // NO OP
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        processService(beanFactory);
        processReference(beanFactory);
        // register rules and set fallback factory
        finishSentinelInitialize();
    }


    /**
     * process all @Service class
     *
     * @param beanFactory
     */
    public void processService(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNamesForType = beanFactory.getBeanNamesForType(ServiceBean.class);
        for (String beanName : beanNamesForType) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();

            PropertyValue ref = propertyValues.getPropertyValue("ref");

            // "ref" beanName is linking to the acutual Service bean in BeanFactory,
            // and the "interface" PropertyValue is linking to the Service Interface class name
            String serviceImplBdName = ((RuntimeBeanReference) ref.getValue()).getBeanName();
            BeanDefinition serviceImplBd = beanFactory.getBeanDefinition(serviceImplBdName);
            PropertyValue interfacePv = propertyValues.getPropertyValue("interface");
            String interfaceClassName = (String) interfacePv.getValue();
            Class interfaceClazz = null;

            try {
                // resolve interface class by BeanClassLoader
                ((ScannedGenericBeanDefinition) serviceImplBd).resolveBeanClass(beanFactory.getBeanClassLoader());
                interfaceClazz = ClassUtils.resolveClassName(interfaceClassName, beanFactory.getBeanClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostProcessBeanFactory failed because can't resolve class by ClassLoader: "
                        + beanFactory.getBeanClassLoader(), e);
            }

            Class<?> serviceImplBeanClass = ((ScannedGenericBeanDefinition) serviceImplBd).getBeanClass();

            Service serviceAnno = AnnotationUtils.findAnnotation(serviceImplBeanClass, Service.class);
            beanDefinition.getPropertyValues().getPropertyValue("interface");

            if (serviceAnno != null) {
                String group = serviceAnno.group();
                String version = serviceAnno.version();
                FallbackHandler fallbackHandler = serviceImplBeanClass.getAnnotation(FallbackHandler.class);
                processFallback(beanFactory, fallbackHandler, interfaceClazz, group, version, providerFallbackManager);
                assembleRules(serviceImplBeanClass, interfaceClazz, group, version);
            }

        }
    }

    /**
     * Initially, we will support only Rule in interface level. In the future, we may add support for Rule in method level.
     */
    void assembleRules(Object obj, Class interfaceClass, String group, String version) {
        URL url = new URL(null, null, 0).addParameters(INTERFACE_KEY, interfaceClass.getName(), GROUP_KEY, group, VERSION_KEY, version);
        String serviceInterface = url.getServiceInterface();
        boolean foundFlowRule = false;
        FlowRuleDefine flowDef = getAnnotation(obj, FlowRuleDefine.class);
        if (flowDef != null) {
            loadFlowRule(flowDef, ResourceUtils.getInterfaceResourceName(url), serviceInterface, "");
            foundFlowRule = true;
        }

        WarmUpFlowRuleDefine warmDef = getAnnotation(obj, WarmUpFlowRuleDefine.class);
        if (warmDef != null) {
            loadWarmupFlowRule(warmDef, ResourceUtils.getInterfaceResourceName(url), serviceInterface, "");
            foundFlowRule = true;
        }

        RateLimitFlowRuleDefine rateDef = getAnnotation(obj, RateLimitFlowRuleDefine.class);
        if (rateDef != null) {
            loadRateLimitFlowRule(rateDef, ResourceUtils.getInterfaceResourceName(url), serviceInterface, "");
            foundFlowRule = true;
        }

        if (!foundFlowRule) {
            LOGGER.warn("SENTINEL   FLOW  ==> not found @FlowRuleDefine at [{}.{}], remove `blockHandler` or add @FlowRuleDefine", serviceInterface, "");
        }

        DegradeRuleDefine degradeDef = getAnnotation(obj, DegradeRuleDefine.class);
        if (degradeDef != null) {
            loadDegradeRule(degradeDef, ResourceUtils.getInterfaceResourceName(url), url.getServiceInterface(), "");
        } else {
            LOGGER.warn("SENTINEL DEGRADE ==> not found @DegradeRuleDefine at [{}.{}], remove `fallback` or add @DegradeRuleDefine", serviceInterface, "");
        }
    }


    <T extends Annotation> T getAnnotation(Object obj, Class<T> annotationClass) {
        if (obj instanceof Field) {
            Annotation declaredAnnotation = ((Field) obj).getDeclaredAnnotation(annotationClass);
            return (T) declaredAnnotation;
        }
        return (T) ((Class) obj).getDeclaredAnnotation(annotationClass);

    }

    void processReference(ConfigurableListableBeanFactory beanFactory) {

        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            Class targetClass = null;

            // The target class has not been resolved, try to resolve it
            if (!((AbstractBeanDefinition) beanDefinition).hasBeanClass()) {
                // Maybe it's a factory bean with @Bean annotation
                if (beanClassName == null) {
                    if (beanDefinition instanceof AnnotatedBeanDefinition) {
                        beanClassName = ((AnnotatedBeanDefinition) beanDefinition).getFactoryMethodMetadata().getReturnTypeName();
                    }
                }
                if (beanClassName != null) {
                    try {
                        targetClass = ClassUtils.resolveClassName(beanClassName, beanFactory.getBeanClassLoader());
                    } catch (Throwable e) {
                        LOGGER.info("Can't resolve class by ClassLoader: " + beanFactory.getBeanClassLoader(), e);
                    }
                }
            } else {
                targetClass = ((AbstractBeanDefinition) beanDefinition).getBeanClass();
            }

            // If there is still no way to resolve, discard it
            if (targetClass == null) {
                continue;
            }

            ReflectionUtils.doWithFields(targetClass
                    , field -> processReferenceField(beanFactory, field)
                    , field -> field.getDeclaredAnnotation(Reference.class) != null);
        }

    }

    public void processReferenceField(ConfigurableListableBeanFactory beanFactory, Field field) {
        Class<?> referenceInterfaceClass = field.getType();
        FallbackHandler fallbackHandler = field.getDeclaredAnnotation(FallbackHandler.class);
        Reference reference = field.getDeclaredAnnotation(Reference.class);
        // generic reference should be ignored
        if (reference.generic() && GenericService.class.equals(referenceInterfaceClass)) {
            return;
        }
        processFallback(beanFactory, fallbackHandler, referenceInterfaceClass, reference.group(), reference.version(), consumerFallbackManager);
        assembleRules(field, field.getType(), reference.group(), reference.version());
    }


    public void processFallback(BeanFactory beanFactory,
                                FallbackHandler fallbackHandler,
                                Class interfaceClazz, String group,
                                String version, FallbackManager consumerFallbackManager) {
        if (fallbackHandler != null) {
            Class<?> clazz = fallbackHandler.fallbackClass();

            // the fallback class must inherit the interfaceClass
            if (!clazz.isAssignableFrom(interfaceClazz)) {
            }
            URL url = new URL(null, null, 0).addParameters(INTERFACE_KEY, interfaceClazz.getName(), GROUP_KEY, group, VERSION_KEY, version);

            String key = ResourceUtils.getInterfaceResourceName(url);
            // fallback bean should be lazy-init by Supplier after all BeanPostProcessors have initialized
            consumerFallbackManager.setFallbackImpl(key, new FallbackManager.FallbackImplSupplier(() -> beanFactory.getBean(clazz)));

            // ignore superclass's methods and non-public methods
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .forEach(method -> {
                        String resourceName = ResourceUtils.getResourceName(url, method.getName(), method.getParameterTypes(),
                                DubboConfig.getDubboInterfaceGroupAndVersionEnabled(), DubboConfig.getDubboProviderPrefix());
                        consumerFallbackManager.setFallbackMethod(resourceName, method);
                    });
        }
    }


    private void loadRateLimitFlowRule(RateLimitFlowRuleDefine flowDef, String resource, String className, String
            methodName) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setCount(getDouble(flowDef.count()));
        rule.setGrade(flowDef.grade());
        rule.setLimitApp(flowDef.app());
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        rule.setMaxQueueingTimeMs(getInt(flowDef.rateLimit()));

        flowRules.add(rule);

        LOGGER.info("SENTINEL RateLimit FLOW  ==> resource \"{}\" on METHOD: ({}.{}), rule: <grade:{}, count:{}, maxQueueTimeMS:{}, behavior:{}>",
                resource, className, methodName,
                ConstStrings.flowGradeString(rule.getGrade()), rule.getCount(), rule.getMaxQueueingTimeMs(),
                ConstStrings.behaviorGradeString(rule.getControlBehavior()));
    }

    private void loadWarmupFlowRule(WarmUpFlowRuleDefine flowDef, String resource, String className, String
            methodName) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setCount(getDouble(flowDef.count()));
        rule.setGrade(flowDef.grade());
        rule.setWarmUpPeriodSec(getInt(flowDef.warmUpPeriodSec()));
        rule.setLimitApp(flowDef.app());
        if (!flowDef.rateLimit().isEmpty()) {
            rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER);
            rule.setMaxQueueingTimeMs(getInt(flowDef.rateLimit()));
            LOGGER.info("SENTINEL WarmUp FLOW  ==> resource \"{}\" on METHOD: ({}.{}), rule: <grade:{}, count:{}, WarmUpPeriodSec:{}, maxQueueTimeMS:{} behavior:{}>",
                    resource, className, methodName,
                    ConstStrings.flowGradeString(flowDef.grade()), rule.getCount(), rule.getWarmUpPeriodSec(), rule.getMaxQueueingTimeMs(),
                    ConstStrings.behaviorGradeString(rule.getControlBehavior()));

        } else {
            rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
            LOGGER.info("SENTINEL WarmUp FLOW  ==> resource \"{}\" on METHOD: ({}.{}), rule: <grade:{}, count:{}, WarmUpPeriodSec:{}, behavior:{}>",
                    resource, className, methodName,
                    ConstStrings.flowGradeString(flowDef.grade()), rule.getCount(), rule.getWarmUpPeriodSec(),
                    ConstStrings.behaviorGradeString(rule.getControlBehavior()));
        }

        flowRules.add(rule);

    }

    private void loadFlowRule(FlowRuleDefine flowDef, String resource, String className, String methodName) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setCount(getDouble(flowDef.count()));
        rule.setGrade(flowDef.grade());
        rule.setControlBehavior(flowDef.behavior());
        rule.setLimitApp(flowDef.app());
        flowRules.add(rule);
        LOGGER.info("SENTINEL   FLOW  ==> resource \"{}\" on METHOD: ({}.{}), rule: <grade:{}, count:{}, behavior:{}>",
                resource, className, methodName,
                ConstStrings.flowGradeString(flowDef.grade()), rule.getCount(),
                ConstStrings.behaviorGradeString(flowDef.behavior()));
    }

    private void loadDegradeRule(DegradeRuleDefine degradeDef, String resource, String className, String methodName) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setCount(getDouble(degradeDef.count()));
        rule.setGrade(degradeDef.grade());
        rule.setTimeWindow(getInt(degradeDef.timeWindow()));
        rule.setLimitApp(degradeDef.app());
        degradeRules.add(rule);
        LOGGER.info("SENTINEL DEGRADE ==> resource \"{}\" on METHOD: ({}.{}), rule: <grade:{}, count:{}, timeWindow:{}(s)>",
                resource, className, methodName,
                ConstStrings.degradeGradeString(degradeDef.grade()),
                rule.getCount(), rule.getTimeWindow());
    }

    public void finishSentinelInitialize() {
        LOGGER.info("SENTINEL         ==> load {} flow rules", flowRules.size());
        FlowRuleManager.loadRules(flowRules);
        LOGGER.info("SENTINEL         ==> load {} degrade rules", degradeRules.size());
        DegradeRuleManager.loadRules(degradeRules);

        DubboFallbackRegistry.setProviderFallback(providerFallbackManager);
        DubboFallbackRegistry.setConsumerFallback(consumerFallbackManager);

    }

    public String parserSPEL(String expr) {
        String key;
        if (expr.charAt(1) == '{') {
            // ${xxx} format
            key = expr.substring(2, expr.length() - 1);
        } else {
            // $xxx format
            key = expr.substring(1);
        }

        return parser.parseExpression(key).getValue(context, String.class);
    }

    public String readProperty(String expr) {
        String key;
        if (expr.charAt(1) == '{') {
            // ${xxx} format
            key = expr.substring(2, expr.length() - 1);
        } else {
            // $xxx format
            key = expr.substring(1);
        }

        return environment.getProperty(key);
    }

    String getProperty(String expr) {
        expr = expr.trim();
        if (expr.isEmpty()) {
            return expr;
        }

        switch (expr.charAt(0)) {
            case '#':
                return parserSPEL(expr);
            case '$':
                return readProperty(expr);
            default:
                return expr;
        }
    }

    double getDouble(String expr) {
        return Double.parseDouble(getProperty(expr));
    }

    int getInt(String expr) {
        return Integer.parseInt(getProperty(expr));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        environment = applicationContext.getEnvironment();
        context = new StandardEvaluationContext(environment);
    }
}
