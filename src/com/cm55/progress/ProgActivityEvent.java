package com.cm55.progress;

/**
 * 進捗を具体的な数値で表すことができない場合に、何かしらの作業を行っていることを示す。
 * @author ysugimura
 */
public class ProgActivityEvent extends ProgEvent {
  
  ProgActivityEvent(Progress progress) {
    super(progress);
  }
  @Override public String toString() {
    return "activity";
  }
}