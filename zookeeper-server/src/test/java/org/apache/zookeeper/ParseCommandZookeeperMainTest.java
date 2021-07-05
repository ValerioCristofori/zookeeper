package org.apache.zookeeper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ParseCommandZookeeperMainTest {
    private boolean expResult;
    private String cmdstring;

    public ParseCommandZookeeperMainTest(TestParameters input) {
        this.expResult = input.isExpResult();
        this.cmdstring = input.getCmdstring();
    }

    @Parameterized.Parameters
    public static Collection<TestParameters> getTestParameters() {
        List<TestParameters> inputs = new ArrayList<>();

        inputs.add(new TestParameters(false, null));
        inputs.add(new TestParameters(false, ""));

        inputs.add(new TestParameters(true, "'' arguments"));
        inputs.add(new TestParameters(true, "\"\" arguments"));
        inputs.add(new TestParameters(true, "cmd ''"));

        inputs.add(new TestParameters(true, "cmd arg1 arg2"));
        inputs.add(new TestParameters(true, "cmd 'arg1' arg2"));
        inputs.add(new TestParameters(true, "'cmd' arg1 arg2"));
        inputs.add(new TestParameters(true, "\"cmd\" arg1 arg2"));
        return inputs;
    }

    public static class TestParameters {
        private boolean expResult;
        private String cmdstring;

        public TestParameters(boolean expResult, String cmdstring){
            this.expResult = expResult;
            this.cmdstring = cmdstring;
        }

        public boolean isExpResult() {
            return expResult;
        }

        public String getCmdstring() {
            return cmdstring;
        }
    }

    @Test
    public void parseCommandTest(){

        ZooKeeperMain.MyCommandOptions optClass = new ZooKeeperMain.MyCommandOptions();

        boolean actResult;

        try {
            actResult = optClass.parseCommand(cmdstring);
        } catch (NullPointerException e) {
            Assert.assertFalse(expResult);
            return;
        }
        Assert.assertEquals(expResult, actResult);

    }

}
