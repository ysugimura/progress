package com.cm55.progress;

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.junit.*;

public class ProgressTest {

  @Test
  public void DoneのみのテストTest() {
    List<ProgEvent>events = new ArrayList<>();
    Progress p = new Progress();
    p.listen(ProgEvent.class, e-> events.add(e));
    p.done();
    this.validate(events, ProgDoneEvent.class);
  }
  
  @Test
  public void DoneのみのテストTest1() {
    List<ProgEvent>events = new ArrayList<>();
    Progress p = new Progress();
    p.listen(ProgEvent.class, e-> events.add(e));
    p.incProgress();
    this.validate(events, ProgDoneEvent.class);
  }
  
  @Test  
  public void doneTest1() {
    List<ProgEvent>events = new ArrayList<>();
    Progress p = new Progress();
    p.listen(ProgDoneEvent.class, e-> events.add(e));
    assertEquals(0, events.size());    
    p.incProgress();
    assertEquals(ProgDoneEvent.class, events.get(0).getClass());
  }
  
  @Test  
  public void doneTest2() {
    List<ProgEvent>events = new ArrayList<>();
    Progress p = new Progress();
    p.listen(ProgDoneEvent.class, e-> events.add(e));
    assertEquals(0, events.size());    
    p.createChild("test", 0);
    assertEquals(1, events.size()); 
    assertEquals(ProgDoneEvent.class, events.get(0).getClass());
  }
  
  @Test  
  public void doneTest3() {
    List<ProgEvent>events = new ArrayList<>();
    Progress p = new Progress();    
    p.listen(ProgTitleEvent.class, e->events.add(e));
    p.listen(ProgDoneEvent.class, e-> events.add(e));
    assertEquals(0, events.size());      
    Progress c = p.createChild("Child", 2);    
    assertEquals(1, events.size()); 
    assertEquals(ProgTitleEvent.class, events.get(0).getClass());
    c.incProgress();
    assertEquals(1, events.size()); 
    c.incProgress();

    assertEquals(3, events.size());
    assertEquals(ProgDoneEvent.class, events.get(2).getClass());
  }
  
  @Test  
  public void 連続的な子進捗オブジェクトの作成() {
    List<ProgEvent>events = new ArrayList<>();
    Progress p = new Progress();    
    p.listen(ProgTitleEvent.class, e->events.add(e));
    p.listen(ProgDoneEvent.class, e-> events.add(e));
    assertEquals(0, events.size());      
    Progress child = p.createChild(null, 2);    
    
    child.createChild(null,  1);
    Progress c2 = child.createChild(null,  1);
    assertEquals(0, events.size());
    c2.incProgress();
    assertEquals(1, events.size());
    assertEquals(ProgDoneEvent.class, events.get(0).getClass());
  }

  @Test
  public void ProgressEventのテスト_分割数が100以上でもイベントはパーセント単位のみ() {
    Progress p = new Progress(1000); 
    List<ProgEvent>events = new ArrayList<>();
    p.listen(ProgPercentEvent.class, e->events.add(e));
    for (int i = 0; i < 1000; i++) p.incProgress();    
    assertEquals(100, events.size());
    for (int i = 0; i < 100; i++) {
      assertEquals(i + 1, ((ProgPercentEvent)events.get(i)).percent);
    }
  }

  @Test
  public void ProgressEventのテスト_子分割の場合() {    
    Progress p = new Progress(2); 
    List<ProgEvent>events = new ArrayList<>();
    p.listen(ProgPercentEvent.class, e->events.add(e));
    
    Progress c = p.createChild(null,  5);
    for (int i = 0; i < 5; i++)  c.incProgress();
    
    for (ProgEvent e: events) System.out.println("" + e);
  }
  
  @Test
  public void test() {
    List<String>s = new ArrayList<String>() {{
      add("test");
      add("test");
    }};
    System.out.println(s.stream().collect(Collectors.joining(">")));    
  }

  <T>void show(List<T>list) {
    for (T o: list) {
      System.out.println("" + o.getClass());
    }
  }
  
  <T>void validate(List<T>list, Class<?>...classes) {
    assertArrayEquals(classes, 
        list.stream().map(o->o.getClass()).collect(Collectors.toList()).toArray(new Class[0]));
  }
  
}
