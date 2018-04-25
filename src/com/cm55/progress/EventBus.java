package com.cm55.progress;

import java.util.*;
import java.util.function.*;

/**
 * 任意のイベントクラスと、それを受け入れる{@link Consumer}を登録しておき、イベントが発生したら登録されたすべての{@link Consumer}に
 * 通知する。
 * <p>
 * 例えば
 * </p>
 * <pre>
 * static class SomeEvent {
 * }
 * EventBus bus = new EventBus();
 * bus.addListener(SomeEvent.class, e-> { ... });
 * </pre>
 * <p>
 * としておき、bus.dispatchEvent(new SomeEvent());としてイベントを発生させる。
 * </p>
 * <p>
 * なお、上位のイベントクラスを登録することにより、一度に複数のイベントに対応することができる。
 * </p>
 * <pre>
 * static class SomeEvent {}
 * static class FooEvent extends SomeEvent {}
 * static class BarEvent extends SomeEvent {}
 * ...
 * bus.addListener(SomeEvent.class, e-> { ... });
 * </pre>
 * <p>
 * とすることで、例えば、bus.dispatchEvent(new FooEvent());でもリスナーが呼び出される。
 * </p>
 * @author ysugimura
 */
public class EventBus {

  /** イベントタイプごとのリスナーリスト */
  private Map<Class<?>, EventLink<?>> eventLinks;

  /**
   * 指定タイプのイベントのリスナーを登録する
   * @param eventType イベントタイプクラス
   * @param consumer リスナー
   */
  @SuppressWarnings("unchecked")
  public <T> void addListener(Class<T> eventType, Consumer<T> consumer) {
    if (eventLinks == null) eventLinks = new HashMap<Class<?>, EventLink<?>>();
    EventLink<?>rec = eventLinks.get(eventType);
    if (rec == null) {
      rec = new EventLink<T>();
      eventLinks.put(eventType, rec);
    }
    ((EventLink<T>)rec).addConsumer(consumer);
  }

  /**
   * 指定タイプのイベントのリスナーを解除する
   * @param eventType イベントタイプクラス
   * @param consumer リスナー
   */
  @SuppressWarnings("unchecked")
  public <T> void removeListener(Class<T> eventType, Consumer<T> consumer) {
    if (eventLinks == null) return;
    EventLink<?>rec = eventLinks.get(eventType);
    if (rec == null) return;    
    ((EventLink<T>)rec).removeConsumer(consumer);
  }
  
  /** 
   * イベントをディスパッチする。
   * <p>
   * このとき、このオブジェクトのクラスと同一のイベントタイプのリスナーだけではなく、その上位クラスのイベントタイプのリスナーが登録されていれば
   * そのリスナーにも通知される。
   * </p>
   * <pre>
   * static class SomeEvent {}
   * static class FooEvent extends SomeEvent {}
   * </pre>
   * <p>
   * の場合にSomeEventのリスナーはFooEventのディスパッチ対象となる。
   * </p>
   * @param event ディスパッチするイベントオブジェクト
   */
  @SuppressWarnings("unchecked")
  public <T>void dispatchEvent(final T event) {
    if (eventLinks == null) return;

    // ディスパッチ途中でマップが変更されることがある
    for (Map.Entry<Class<?>, EventLink<?>>e: eventLinks.entrySet().toArray(new Map.Entry[0])) {
      if (!e.getKey().isInstance(event)) continue;
      ((EventLink<T>)e.getValue()).dispatchEvent(event);
    }
  }

  /** イベントの型ごとにターゲット・メソッドを格納するオブジェクト */
  static class EventLink<T> {
    ArrayList<Consumer<T>>consumers = new ArrayList<>();    
    
    void addConsumer(Consumer<T>consumer) {
      consumers.add(consumer);
    }
    
    void removeConsumer(Consumer<T>consumer) {
      consumers.remove(consumer);
    }
    
    @SuppressWarnings("unchecked")
    void dispatchEvent(T event) {       
      // ディスパッチ途中でリストが変更されることがある
      for (Consumer<T> consumer: consumers.toArray(new Consumer[0])) {
        consumer.accept(event);
      }
    }
  }
}
