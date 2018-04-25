package com.cm55.progress;

/** 
 * 完了イベント
 * ※{@link ProgPercentEvet}のパーセント指定は目安に過ぎない。
 * 実際に作業が終了したかどうかは、このイベントによって判断すること。
 * @author ysugimura
 */
public class ProgDoneEvent extends ProgEvent {
  ProgDoneEvent(Progress progress) {      
    super(progress);
  }
  @Override public String toString() {
    return "done";
  }
}