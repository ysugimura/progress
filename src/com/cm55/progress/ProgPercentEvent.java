package com.cm55.progress;



/**
 * 進捗状況イベント
 * @author ysugimura
 */
public class ProgPercentEvent extends ProgEvent {
  public final int percent;
  ProgPercentEvent(Progress progress, int percent) {
    super(progress);
    this.percent = percent;
  }
  @Override public String toString() {
    return "percent:" + percent;
  }
}
