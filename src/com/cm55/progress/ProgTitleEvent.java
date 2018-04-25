package com.cm55.progress;


/** 
 * タイトル変更イベント
 * 進捗タイトルが変更されたときに発生する。
 * @author ysugimura
 */
public class ProgTitleEvent extends ProgEvent {
  
  ProgTitleEvent(Progress progress) {
    super(progress);
  }
  
  /** タイトルを取得する。nullにはならない */
  public String getTitle() {
    return progress.hierarchyTitle();
  }
}