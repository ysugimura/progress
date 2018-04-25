package com.cm55.progress;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.junit.*;
import static org.junit.Assert.*;

public class EventBusTest {

  static class SomeEvent {}
  static class FooEvent  {}
  static class BarEvent extends FooEvent {}
  
  @Test
  public void addTest() {
    EventBus bus = new EventBus();
    
    boolean[]value = new boolean[1];
    Consumer<SomeEvent>toList1 = e-> { value[0] = true; };
    bus.addListener(SomeEvent.class, toList1);
    bus.dispatchEvent(new SomeEvent());
    assertTrue(value[0]);
  }
  
  @Test
  public void removeTest() {
    EventBus bus = new EventBus();
    
    boolean[]value = new boolean[1];
    Consumer<SomeEvent>toList1 = e-> { value[0] = true; };
    bus.addListener(SomeEvent.class, toList1);
    bus.removeListener(SomeEvent.class,  toList1);  
    bus.dispatchEvent(new SomeEvent());
    assertFalse(value[0]);
  }
  
  @Test
  public void addRemoveTest() {
    List<Object>list1 = new ArrayList<>();  
    List<Object>list2 = new ArrayList<>();  
    EventBus bus = new EventBus();
    
    Consumer<SomeEvent>toList1 = e-> list1.add(e);
    bus.addListener(SomeEvent.class, toList1);
    bus.addListener(FooEvent.class,  e-> list1.add(e));
    bus.addListener(SomeEvent.class,  e->list2.add(e));
    bus.addListener(BarEvent.class,  e-> list2.add(e));
    
    bus.dispatchEvent(new SomeEvent());
    bus.dispatchEvent(new FooEvent());
    bus.dispatchEvent(new BarEvent());
    
    validate(list1, SomeEvent.class, FooEvent.class, BarEvent.class);
    validate(list2, SomeEvent.class, BarEvent.class);
    
    bus.removeListener(SomeEvent.class,  toList1);    
    bus.dispatchEvent(new SomeEvent());
    
    validate(list1, SomeEvent.class, FooEvent.class, BarEvent.class);
    validate(list2, SomeEvent.class, BarEvent.class, SomeEvent.class);
  }

  void validate(List<Object>list, Class<?>...classes) {
    assertArrayEquals(classes, 
        list.stream().map(o->o.getClass()).collect(Collectors.toList()).toArray(new Class[0]));
  }
}
