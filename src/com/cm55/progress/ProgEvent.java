package com.cm55.progress;

/**
 * {@link Progress}が発生するすべてのイベントの抽象クラス
 * @author ysugimura
 */
public abstract class ProgEvent {
  public final Progress progress;
  protected ProgEvent(Progress progress) {
    this.progress = progress;
  }
}
