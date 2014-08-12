package ru.yandex.qatools.camelot.test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.runners.model.InitializationError;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import ru.yandex.qatools.camelot.test.core.CamelotTestListener;
import ru.yandex.qatools.camelot.util.MapUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.camel.test.spring.CamelSpringTestHelper.setTestClass;
import static org.springframework.test.annotation.DirtiesContext.ClassMode;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.RandomUtil.randomInt;
import static ru.yandex.qatools.camelot.util.ReflectUtil.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CamelotTestRunner extends CamelSpringJUnit4ClassRunner {
    public static final String REAL_TEST_CLASS_ATTR = "CamelotRealTestClassAttr";

    public CamelotTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected TestContextManager createTestContextManager(Class<?> clazz) {
        try {
            //pool creation
            ClassPool pool = ClassPool.getDefault();
            //extracting the class
            CtClass cc = pool.makeClass(clazz.getName() + "$CamelotTest" + randomInt());
            // create the annotation
            ClassFile ccFile = cc.getClassFile();
            final ConstPool constPool = ccFile.getConstPool();
            new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            boolean useCustomContext = getAnnotation(clazz, UseCustomContext.class) != null;
            List<String> contextLocations = new ArrayList<>();
            if (useCustomContext) {
                contextLocations.add("classpath*:camelot-test-empty-context.xml");
                contextLocations.addAll(asList((String[]) getAnnotationValue(clazz, UseCustomContext.class, "value")));
            } else {
                contextLocations.add("classpath*:camelot-test-context.xml");
            }

            AnnotationsAttribute annotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            annotations.addAnnotation(
                    newAnnotation(constPool, ContextConfiguration.class,
                            map("locations", stringArrayMemberValue(constPool, contextLocations.toArray(new String[contextLocations.size()]))))
            );
            DirtiesContext dirtiesContext;
            if ((dirtiesContext = (DirtiesContext) getAnnotation(clazz, DirtiesContext.class)) != null) {
                annotations.addAnnotation(
                        newAnnotation(constPool, DirtiesContext.class,
                                map("classMode", enumMemberValue(constPool, ClassMode.class, dirtiesContext.classMode()))
                        )
                );
            }
            annotations.addAnnotation(
                    newAnnotation(constPool, MockEndpoints.class, MapUtil.<String, MemberValue>map("value", new StringMemberValue("*", constPool)))
            );
            annotations.addAnnotation(
                    newAnnotation(constPool, TestExecutionListeners.class, map("value", classArrayMemberValue(constPool, CamelotTestListener.class)))
            );
            ccFile.addAttribute(annotations);

            // transform the ctClass to java class
            Class enhancedClass = cc.toClass();
            CamelTestContextManager res = new CamelTestContextManager(enhancedClass, getDefaultContextLoaderClassName(clazz));
            // FIXME: hack - we're overriding some vital properties of TestContext!
            for (Method m : getMethodsInClassHierarchy(res.getClass())) {
                if (m.getName().equals("getTestContext")) {
                    setTestClass(enhancedClass);
                    m.setAccessible(true);
                    TestContext context = (TestContext) m.invoke(res);
                    context.setAttribute(REAL_TEST_CLASS_ATTR, clazz);
                    break;
                }
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instrument the test code: ", e);
        }
    }

//    @Override
//    protected Object createTest() throws Exception {
//        Object testInstance = new TestClass(enhancedClass).getOnlyConstructor().newInstance();
//        getTestContextManager().prepareTestInstance(testInstance);
//        return testInstance;
//    }

    private Annotation newAnnotation(ConstPool constPool, Class<? extends java.lang.annotation.Annotation> clazz,
                                     Map<String, MemberValue> attrs) {
        Annotation contextConfigAttr = new Annotation(clazz.getName(), constPool);
        for (Map.Entry<String, MemberValue> attribute : attrs.entrySet()) {
            contextConfigAttr.addMemberValue(attribute.getKey(), attribute.getValue());
        }
        return contextConfigAttr;
    }

    private <T extends Enum> MemberValue enumMemberValue(ConstPool constPool, Class<T> enumClass, T value) {
        final EnumMemberValue mValue = new EnumMemberValue(constPool);
        mValue.setType(enumClass.getName());
        mValue.setValue(value.name());
        return mValue;
    }

    private MemberValue classArrayMemberValue(ConstPool constPool, Class... values) {
        final ArrayMemberValue mValue = new ArrayMemberValue(constPool);
        List<ClassMemberValue> memberValues = new ArrayList<>();
        for (Class value : values) {
            memberValues.add(new ClassMemberValue(value.getName(), constPool));
        }
        mValue.setValue(memberValues.toArray(new ClassMemberValue[memberValues.size()]));
        return mValue;
    }

    private MemberValue stringArrayMemberValue(ConstPool constPool, String... values) {
        final ArrayMemberValue mValue = new ArrayMemberValue(constPool);
        List<StringMemberValue> memberValues = new ArrayList<StringMemberValue>();
        for (String value : values) {
            memberValues.add(new StringMemberValue(value, constPool));
        }
        mValue.setValue(memberValues.toArray(new StringMemberValue[memberValues.size()]));
        return mValue;
    }
}
