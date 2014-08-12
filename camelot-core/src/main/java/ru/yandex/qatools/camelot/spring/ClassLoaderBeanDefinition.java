package ru.yandex.qatools.camelot.spring;

import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ClassLoaderBeanDefinition extends RootBeanDefinition {

    private final ClassLoader defaultLoader;

    public ClassLoaderBeanDefinition(Class beanClass, ClassLoader defaultLoader) {
        super(beanClass);
        this.defaultLoader = defaultLoader;
    }

    @Override
    public Class resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
        return super.resolveBeanClass(defaultLoader);
    }


}
