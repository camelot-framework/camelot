<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:amq="http://activemq.apache.org/schema/core"
       xsi:schemaLocation="
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
            http://www.springframework.org/schema/context
            http://www.springframework.org/schema/context/spring-context-3.0.xsd
            http://activemq.apache.org/schema/core
            http://activemq.apache.org/schema/core/activemq-core.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">


    <import resource="file:${originalAppContextFile}"/>
    <import resource="${hazelcastContextConfigPath}"/>

    <#if useEmbeddedActivemq>
    <amq:broker useJmx="false" persistent="false">
        <amq:transportConnectors>
            <amq:transportConnector uri="tcp://localhost:${activemqPort}"/>
        </amq:transportConnectors>
        <#if networkConnectors?has_content>
        <amq:networkConnectors>
        <#list networkConnectors as connector>
            <amq:networkConnector uri="static:(${connector})" name="${connector.host + "_" + connector.port}"/>
        </#list>
        </amq:networkConnectors>
        </#if>
    </amq:broker>
    <amq:connectionFactory id="jmsFactory" brokerURL="vm://localhost:${activemqPort}"/>
    <bean id="jmsConfig" class="org.apache.camel.component.jms.JmsConfiguration">
        <property name="connectionFactory" ref="jmsFactory"/>
        <property name="transacted" value="false"/>
    </bean>
    <bean id="activemq" class="org.apache.activemq.camel.component.ActiveMQComponent">
        <property name="configuration" ref="jmsConfig"/>
    </bean>
    </#if>

    <bean id="configurationProperties" class="ru.yandex.qatools.camelot.spring.ListablePropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
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

    <bean id="LOADER-PLUGIN" class="ru.yandex.qatools.camelot.maven.service.PluginPluginLoader">
        <constructor-arg name="srcResDir" value="${srcResDir}"/>
        <constructor-arg name="testSrcResDir" value="${testSrcResDir}"/>
    </bean>
</beans>
