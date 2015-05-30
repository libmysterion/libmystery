package com.mystery.libmystery.injection;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;
import static org.hamcrest.core.Is.is;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

public class InjectorTest {

    public InjectorTest() {
    }

    @Inject
    private TestInjectable testGuy;

    @Before
    public void before() {
        TestSingleton.instanceCount = 0;
    }

    @Test
    public void createInstance_shouldBuildAnObect() {
        Injector injector = new Injector();
        InjectorTest test = injector.create(InjectorTest.class);
        assertNotNull(test);
    }

    @Test
    public void createInstance_shouldInjectMembers() {
        Injector injector = new Injector();
        InjectorTest test = injector.create(InjectorTest.class);
        assertNotNull(test);
        assertNotNull(test.testGuy);
    }

    @Test
    public void createInstance_shouldInvokePostConstructAnnotatedMethods() {
        Injector injector = new Injector();
        InjectorTest test = injector.create(InjectorTest.class);
        assertTrue(test.testGuy.ran);
    }
    
    @Test
    public void createInstance_shouldInvokePostConstructAnnotatedMethodsOnSingletons() {
        Injector injector = new Injector();
        TestSingleton test = injector.create(TestSingleton.class);
        assertTrue(test.postConstructCalled);
    }
    
     @Test
    public void createInstance_shouldInvokePostConstructAnnotatedMethodsOnInjectedSingletons() {
        Injector injector = new Injector();
        TestSingletonMonsterChild test = injector.create(TestSingletonMonsterChild.class);
        assertTrue(test.postConstructCalled);
    }

    @Test
    public void createInstance_shouldInjectPropeties() {

        HashMap<String, String> properties = new HashMap<>();
        properties.put("injectMe", "JEEZUS");
        properties.put("injectedBy", "MICAHEL JACKSON");

        Injector injector = new Injector();
        injector.setPropertySource(properties::get);
        InjectorTest test = injector.create(InjectorTest.class);
        assertNotNull(test);
        assertThat(test.testGuy.injectedProperty, is("JEEZUS"));
        assertThat(test.testGuy.injectedBy, is("MICAHEL JACKSON"));

    }

    @Test
    public void createInstance_shouldInjectPropetiesFromProperties() {

        Properties properties = new Properties();
        properties.put("injectMe", "JEEZUS");
        properties.put("injectedBy", "MICAHEL JACKSON");

        Injector injector = new Injector();
        injector.setPropertySource(properties::getProperty);
        InjectorTest test = injector.create(InjectorTest.class);
        assertNotNull(test);
        assertThat(test.testGuy.injectedProperty, is("JEEZUS"));
        assertThat(test.testGuy.injectedBy, is("MICAHEL JACKSON"));

    }

    @Test
    public void createInstance_shouldInstantiateSingletonsOnce() {
        Injector injector = new Injector();
        injector.create(TestSingleton.class);
        injector.create(TestSingleton.class);
        injector.create(TestSingleton.class);
        TestSingleton test = injector.create(TestSingleton.class);
        assertNotNull(test);
        assertThat(test.instanceNumber, is(1)); //only 1 instance is created
    }

    @Test
    public void createInstance_shouldInstantiateNewNonSingletons() {
        Injector injector = new Injector();
        TestNonSingleton test = injector.create(TestNonSingleton.class);
        assertNotNull(test);
        assertThat(test.instanceNumber, is(1));

        TestNonSingleton test2 = injector.create(TestNonSingleton.class);
        assertNotNull(test2);
        assertThat(test2.instanceNumber, is(2)); //2 instances have been created
        assertTrue(test != test2);
    }

    @Test
    public void createInstance_shouldInjectChilds() {

        Injector injector = new Injector();
        TestMonster test = injector.create(TestMonster.class);
        assertNotNull(test);
        assertNotNull(test.injectable);
        assertNotNull(test.testSingleton);
        assertNotNull(test.monsterChild);
        assertNotNull(test.monsterChild.injectable);
        assertNotNull(test.monsterChild.testSingleton);
        assertNotNull(test.singletonMonsterChild);
        assertNotNull(test.singletonMonsterChild.injectable);
        assertNotNull(test.singletonMonsterChild.testSingleton);
        assertNotNull(test.singletonMonsterChild2);
        assertNotNull(test.singletonMonsterChild2.injectable);
        assertNotNull(test.singletonMonsterChild2.testSingleton);

        assertTrue(test.testSingleton == test.monsterChild.testSingleton);
        assertTrue(test.testSingleton == test.singletonMonsterChild.testSingleton);
        assertTrue(test.singletonMonsterChild2 == test.singletonMonsterChild);

    }

    @Test
    public void createInstance_shouldAllowUsToInjectMocks() {

        TestInjectable mockInjectable = Mockito.mock(TestInjectable.class);

        Function<Class<TestInjectable>, TestInjectable> mockTestInjectableInstanceFactory = Mockito.mock(Function.class);
        when(mockTestInjectableInstanceFactory.apply(TestInjectable.class)).thenReturn(mockInjectable);

        Injector injector = new Injector();
        injector.setInstanceFactory(TestInjectable.class, mockTestInjectableInstanceFactory);

        TestMonster test = injector.create(TestMonster.class);
        assertNotNull(test);
        assertTrue(test.injectable == mockInjectable);  // we injected our mock
        assertNotNull(test.monsterChild);               // and we got the real impl for everything else     
    }

    // or maybe not....
//    @Test
//    public void cirecular_Singletons_should_create_references_to_each_other() {
//        Injector injector = new Injector();
//        SingletonCircleLeft left = injector.create(SingletonCircleLeft.class);
//        SingletonCircleRight right = injector.create(SingletonCircleRight.class);
//        assertThat(left.right, is(right));
//        assertThat(right.left, is(left));
//    }
}

@Singleton
class SingletonCircleLeft {

    @Inject
    SingletonCircleRight right;
}

@Singleton
class SingletonCircleRight {

    @Inject
    SingletonCircleLeft left;
}

class TestInjectable {

    boolean ran = false;

    @Property(value = "injectMe")
    String injectedProperty;

    @Property
    String injectedBy;

    @PostConstruct
    private void runRunRun() {
        ran = true;
    }

}

class InstanceCounter {

    static int instanceCount = 0;
    int instanceNumber = ++instanceCount;
}

@Singleton
class TestSingleton extends InstanceCounter {

    boolean postConstructCalled = false;
    
    @PostConstruct
    private void go(){
        postConstructCalled = true;
    }
    
}

class TestNonSingleton extends InstanceCounter {

}

class TestMonster {

    @Inject
    TestSingleton testSingleton;

    @Inject
    TestInjectable injectable;

    @Inject
    TestSingletonMonsterChild singletonMonsterChild;

    @Inject
    TestSingletonMonsterChild singletonMonsterChild2;

    @Inject
    TestMonsterChild monsterChild;

}

@Singleton
class TestSingletonMonsterChild extends InstanceCounter {

    @Inject
    TestSingleton testSingleton;

    @Inject
    TestInjectable injectable;
    
    boolean postConstructCalled = false;
    
    @PostConstruct
    private void go(){
        postConstructCalled = true;
    }
    
}

class TestMonsterChild extends InstanceCounter {

    @Inject
    TestSingleton testSingleton;

    @Inject
    TestInjectable injectable;

}
