package com.cm55.progress;

import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.*;


@RunWith(Suite.class)
@SuiteClasses( { 
  ProgressTest.class
  
})
public class AllTest {
  public static void main(String[] args) {
    JUnitCore.main(AllTest.class.getName());
  }
}
