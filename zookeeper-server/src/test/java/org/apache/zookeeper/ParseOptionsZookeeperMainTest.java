package org.apache.zookeeper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ParseOptionsZookeeperMainTest {

    private boolean expResult;
    private String[] args;

    public ParseOptionsZookeeperMainTest(TestParameters input) {
        this.expResult = input.isExpResult();
        this.args = input.getArgs();
    }

    @Parameterized.Parameters
    public static Collection<TestParameters> getTestParameters() {
        List<TestParameters> inputs = new ArrayList<>();

        //invalid -> empty
        inputs.add(new TestParameters(false, new String[1]));

        // invalid -> missing arguments
        inputs.add(new TestParameters(false, new String[]{"-server"}));
        inputs.add(new TestParameters(false, new String[]{"-client-configuration"}));

        //valid
        inputs.add(new TestParameters(true, new String[]{"-r", "cmd"}));
        inputs.add(new TestParameters(true, new String[]{"-r"}));
        inputs.add(new TestParameters(true, new String[]{"-server", "argument"}));
        inputs.add(new TestParameters(true, new String[]{"-timeout", "argument"}));
        inputs.add(new TestParameters(true, new String[]{"-client-configuration", "argument"}));
        inputs.add(new TestParameters(true , new String[]{"cmd", "arg", "-server", "argument"}));
        inputs.add(new TestParameters(true, new String[]{"-waitforconnection"}));

        return inputs;

    }

    public static class TestParameters {
        private boolean expResult;
        private String[] args;

        public TestParameters(boolean expResult, String[] args){
            this.expResult = expResult;
            this.args = args;
        }

        public boolean isExpResult() {
            return expResult;
        }

        public String[] getArgs() {
            return args;
        }
    }

    @Test
    public void parseOptionsTest(){

        ZooKeeperMain.MyCommandOptions optClass = new ZooKeeperMain.MyCommandOptions();

        boolean actResult;

        try {
            actResult = optClass.parseOptions(args);
        } catch (NullPointerException e) {
            Assert.assertFalse(expResult);
            return;
        }
        Assert.assertEquals(expResult, actResult);

    }
}
