package de.sub.goobi.helper;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BatchTest.class, FilesystemHelperTest.class, BatchProcessHelperTest.class, BatchStepHelperTest.class })
public class TestAll {

}