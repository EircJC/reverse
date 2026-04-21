package com.yulink.texas.server.common.utils;

import java.util.Map;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类，用于在非Spring管理的类中获取Bean
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 设置Spring上下文
     * @param applicationContext Spring上下文
     * @throws IllegalStateException 如果上下文已被设置
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws IllegalStateException {
        if (SpringUtil.applicationContext != null) {
            return; // 避免重复设置
        }
        SpringUtil.applicationContext = applicationContext;
    }

    /**
     * 获取Spring上下文
     * @return Spring上下文
     * @throws IllegalStateException 如果上下文未初始化
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 获取指定名称的Bean
     * @param name Bean名称
     * @param <T> Bean类型
     * @return Bean实例
     * @throws IllegalStateException 如果上下文未初始化
     * @throws NoSuchBeanDefinitionException 如果没有找到指定名称的Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws NoSuchBeanDefinitionException {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 获取指定类型的Bean
     * @param clazz Bean类型
     * @param <T> Bean类型
     * @return Bean实例
     * @throws IllegalStateException 如果上下文未初始化
     * @throws NoSuchBeanDefinitionException 如果没有找到指定类型的Bean
     */
    public static <T> T getBean(Class<T> clazz) throws NoSuchBeanDefinitionException {
        checkApplicationContext();
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取指定名称和类型的Bean
     * @param name Bean名称
     * @param clazz Bean类型
     * @param <T> Bean类型
     * @return Bean实例
     * @throws IllegalStateException 如果上下文未初始化
     * @throws NoSuchBeanDefinitionException 如果没有找到指定名称和类型的Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) throws NoSuchBeanDefinitionException {
        checkApplicationContext();
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取指定类型的所有Bean
     * @param clazz Bean类型
     * @param <T> Bean类型
     * @return 所有匹配的Bean映射，键为Bean名称，值为Bean实例
     * @throws IllegalStateException 如果上下文未初始化
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBeansOfType(clazz);
    }

    /**
     * 检查Spring上下文是否已初始化
     * @throws IllegalStateException 如果上下文未初始化
     */
    private static void checkApplicationContext() {
        if (applicationContext == null) {
            // 尝试直接从Spring的BeanFactory获取
            try {
                // 这种方式仅在Spring容器已经初始化的情况下有效
                ApplicationContext context = ApplicationContextHolder.getContext();
                if (context != null) {
                    applicationContext = context;
                    return;
                }
            } catch (Exception e) {
                // 忽略异常，继续抛出原始异常
            }
            
            throw new IllegalStateException("Spring ApplicationContext is not initialized yet! " +
                    "Please check if SpringUtil is properly registered as a Spring Bean.");
        }
    }

    /**
     * 内部类，用于保存ApplicationContext实例
     */
    private static class ApplicationContextHolder {
        private static ApplicationContext context;
        
        public static ApplicationContext getContext() {
            return context;
        }
        
        public static void setContext(ApplicationContext ctx) {
            context = ctx;
        }
    }

    /**
     * 供外部手动设置ApplicationContext
     * @param context Spring上下文
     */
    public static void setContext(ApplicationContext context) {
        ApplicationContextHolder.setContext(context);
        applicationContext = context;
    }
}