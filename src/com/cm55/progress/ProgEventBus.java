package com.cm55.progress;

import org.apache.commons.logging.*;

import com.cm55.eventBus.*;

public class ProgEventBus extends EventBus {
  protected static Log log = LogFactory.getLog(ProgEventBus.class);

  @Override
  protected Log log() {
    return log;
  }
}
