package com.cm55.progress;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import com.cm55.eventBus.*;

public class Progress  {

  protected EventBus es = new ProgEventBus();
  
  /** タイトル */
  protected String title;
  
  /** 親進捗オブジェクト。トップレベル進捗オブジェクトの場合はnull */
  protected final Progress parent;
  
  /** この進捗オブジェクトの分割数 */
  protected final int divisionCount;
  
  /**　上記分割数のうちのの完了数 */
  protected int divisionIndex;
  
  /** 現在実行中の子進捗情報 */
  protected Progress currentChild;
  
  /** 完了フラグ */
  private boolean done;
  
  /** パーセント */
  private int currentPercent = 0;
  
  /** タイトル無しのトップレベル進捗オブジェクトを作成する。分割数は１ */
  public Progress() {
    this(null, 1, null);
  }

  /** 
   * タイトル無しのトップレベル進捗オブジェクトを作成する。分割数を指定。
   * 分割数０以下は無意味なので禁止する。 
   */
  public Progress(int divisionCount) {
    this(null, divisionCount, null);
    // 分割数０以下は不可
    if (divisionCount <= 0) throw new IllegalArgumentException();
  }

  /** 
   * タイトル・分割数を指定したトップレベル進捗オブジェクトを作成する
   * 分割数０以下は無意味なので禁止する
   * @param title
   * @param divisionCount
   */
  public Progress(String title, int divisionCount) {
    this(title, divisionCount, null);
    // 分割数０以下は不可
    if (divisionCount <= 0) throw new IllegalArgumentException();
  }

  /** 
   * 内部使用
   * 
   * @param title タイトル
   * @param divisionCount 分割数。ここでは0の分割数も認められる
   * @param parent 親進捗オブジェクト
   */
  Progress(String title, int divisionCount, Progress parent) {
    this.parent = parent;
    
    // ※分割数は０も可であることに注意
    if (divisionCount < 0) throw new IllegalArgumentException();    
    this.title = title;
    this.divisionCount = divisionCount;
  }
  
  /** 階層タイトル */
  public String hierarchyTitle() {
    String separator = "＞";
    ArrayList<String>concat = new ArrayList<String>();
    concat.add(title);
    if (currentChild != null) 
      concat.add(currentChild.hierarchyTitle());    
    return
      concat.stream().filter(s->s != null && s.length() > 0)
      .collect(Collectors.joining(separator));
  }
  
  /** 
   * 自オブジェクトの「１」の進捗を分割するために子進捗オブジェクトを作成する。その分割数を指定する。
   * <p>
   * 例えば、子オブジェクトの分割数を10とした場合、親オブジェクトの「1」の進捗を得るのに、子では10の進捗動作を行うことを意味する。
   * </p>
   * <p>
   * ただし、このオブジェクトに既に進行中の子進捗オブジェクトが存在する場合には、新たな子進捗オブジェクトを作成することはできない。
   * あくまでも、親の進捗は1ずつ進むので、前の子進捗オブジェクトが完了していなければ、強制的に完了とする
   * </p>
   * <p>
   * なお、子オブジェクトの分割数は0も許される。これは、計算の結果「何もすることが無い」場合に対応している。
   * </p>
   * @param title この進捗のタイトル、あるいはnull
   * @param divisionCount 親オブジェクトの「1」の進捗に対する子による分割数。0も許される
   * @return 子進捗オブジェクト
   */
  public Progress createChild(String title, int divisionCount) {
    
    // 既に子進捗オブジェクトが存在する場合、それは未完了であることを示す。
    // 強制的に完了させる
    if (currentChild != null) {
      currentChild = null;
      incProgress();
    }

    // 子進捗オブジェクトを作成する
    Progress child = new Progress(title, divisionCount, this);
    
    // 自身が既に完了の場合はこれ以上何もしない。子進捗オブジェクトを返す。
    if (this.done) return child;

    // 自身は完了していない。子の分割数が0の場合
    if (child.divisionCount == 0) {
      incProgress();
      return child;
    }

    // この子オブジェクトに進捗報告をまかせる
    currentChild = child;
    currentChild.listen(ProgEvent.class, this::processChildEvent);
    if (title != null) {
      currentChild.es.dispatchEvent(new ProgTitleEvent(currentChild));     
    }   
    
    return child;
  }  

  
  public void setTitle(String title) {
    if (title == null) return;
    this.title = title;
    es.dispatchEvent(new ProgTitleEvent(this));
  }
  
  /** 
   * 分割した一つを強制完了する。
   * もし、全分割が完了したなら自身を完了させる。
   * そうでない場合は進捗イベントを発生する。 */
  public void incProgress() {
    incProgress(1);
  }
  
  public void incProgress(int advance) {
    setProgress(divisionIndex + advance);
  }
  
  public void setProgress(int value) {

    if (done) return;
    
    value = Math.max(0, value);
    value = Math.min(value, divisionCount);
    
    if (divisionIndex == value) return;
    divisionIndex = value;
        
    // 子登録がある場合は消去
    if (currentChild != null) {
      currentChild = null;
    }
    
    if (divisionIndex >= divisionCount) {
      // 自身を完了させる。
      done();
      return;
    }
        
    // 自身の進捗イベント
    mayFireProgressEvent(100.0F * divisionIndex / divisionCount);    
  }
  
  /** 
   * アクティビティ通知を行う。
   * <p>
   * 進捗状況を表示するには、その動作を行う以前にどの位の作業量であるかがわかっていなくてはならない。
   * 例えば、「100の仕事があって、今現在50の進捗である」などである。
   * が、あらかじめ作業量がわかっていないものがある。そのような時に何も通知しなければ、進捗表示ダイアログは経過秒数でさえ表示することができない。
   * このメソッドは、何かしらを行っていることを定期的に通知するものである。
   * </p>
   */
  public void activity() {
    es.dispatchEvent(new ProgActivityEvent(this));
  }
  
  /** 
   * 強制的に完了させる。完了イベントを送出する。
   * 既に完了済の場合は何もしない。
   */
  public void done() {
    if (done) return;
    done = true;
    es.dispatchEvent(new ProgDoneEvent(this));
  }

  /** 完了済みか */
  public boolean isDone() {
    return done;
  }
  
  /** 
   * 現在進捗報告中の子からのイベントを処理する。
   * @param e イベント
   */
  void processChildEvent(ProgEvent e) {
    
    // 既に完了済みなら無視
    if (this.done) return;
    
    // 現在の子以外からのイベントは無視
    if (e.progress != currentChild) return;

    // 子のタイトル変更イベント
    if (e instanceof ProgTitleEvent) {

      es.dispatchEvent(new ProgTitleEvent(this));
      return;
    } 

    // 子の進捗イベント
    if (e instanceof ProgPercentEvent) {
      float childProgress = ((ProgPercentEvent)e).percent;
      mayFireProgressEvent(
          100.0F * divisionIndex / divisionCount +
          childProgress / divisionCount
      );
      return;
    } 

    // 子のアクティビティイベント
    if (e instanceof ProgActivityEvent) {
      es.dispatchEvent(new ProgActivityEvent(this));
      return;
    }

    // 子の完了イベント
    if (e instanceof ProgDoneEvent) {      
      if (currentChild.hierarchyTitle().length() > 0) {
        es.dispatchEvent(new ProgTitleEvent(this));
      }
      
      // カウンタを進める
      incProgress();
      return;
    }
  }
  
  /**
   * floatのパーセントを指定して{@link ProgPercentEvent}を送出する。
   * ただし、以前のパーセンテージと同じ場合は何もしない
   * @param percent
   */
  void mayFireProgressEvent(float percent) {
    int newPercent = Math.round(percent);
    newPercent = Math.max(0, newPercent);
    newPercent = Math.min(100, newPercent);
    if (currentPercent != newPercent) {
      es.dispatchEvent(new ProgPercentEvent(this, currentPercent = newPercent));
    }
  }

  /** リスナー登録 */
  public <T> Unlistener<T> listen(Class<T> eventType, Consumer<T> consumer) {
    return es.listen(eventType, consumer);
  }
}
