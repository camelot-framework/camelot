<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
            http://www.springframework.org/schema/context
            http://www.springframework.org/schema/context/spring-context-3.0.xsd
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">


    <import resource="file:${originalAppContextFile}"/>

    <bean id="camelot-config" class="ru.yandex.qatools.camelot.spring.ListablePropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath*:camelot-default.properties</value>
                <value>classpath*:camelot-web.properties</value>
                <value>classpath*:camelot.properties</value>
                <value>file:${newConfigFile}</value>
                <#if additionalProperties?has_content>
                <value>${additionalProperties}</value>
                </#if>
            </list>
        </property>
        <property name="ignoreResourceNotFound" value="true"/>
        <property name="systemPropertiesModeName" value="SYSTEM_PROPERTIES_MODE_OVERRIDE"/>
    </bean>

    <bean id="camelot-loader-plugin" class="ru.yandex.qatools.camelot.maven.service.PluginPluginLoader">
        <constructor-arg name="srcResDir" value="${srcResDir}"/>
        <constructor-arg name="testSrcResDir" value="${testSrcResDir}"/>
    </bean>
</beans>
