package org.apache.zookeeper.quorum;

import org.apache.zookeeper.common.X509Exception;
import org.apache.zookeeper.server.quorum.LearnerHandler;
import org.apache.zookeeper.server.quorum.ObserverMaster;
import org.apache.zookeeper.server.quorum.QuorumPacket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(value = Parameterized.class)
public class RemoveProposedPacketObserverMasterTest {

    private static final int LAST_PKT_ID = 50;
    private static final int FIRST_PKT_ID = 25;

    //packet to remove
    private static QuorumPacket remQp;

    //Observer class to test
    private ObserverMaster obsM;

    //arguments
    private long zxid;
    private boolean fillQueue;
    private boolean expResult;

    public RemoveProposedPacketObserverMasterTest(TestParameters input) {
        //the arguments of the constructor are not used in the method we are testing
        //we can leave them as null
        obsM = new ObserverMaster(null, null, 80);

        this.zxid = input.getZxid();
        this.fillQueue = input.isFillQueue();
        this.expResult = input.isExpResult();
    }



    @Parameterized.Parameters
    public static Collection<TestParameters> getTestParameters() throws NoSuchFieldException, X509Exception, IllegalAccessException, IOException {
        //function signature
        //QuorumPacket removeProposedPacket(long zxid)
        List<TestParameters> inputs = new ArrayList<>();

        inputs.add(new TestParameters(false, FIRST_PKT_ID-1, true));
        inputs.add(new TestParameters(false, FIRST_PKT_ID, false));

        inputs.add(new TestParameters(false, FIRST_PKT_ID+1, true));
        inputs.add(new TestParameters(true, FIRST_PKT_ID, true));

        return inputs;
    }

    public static class TestParameters {
        private long zxid;
        private boolean fillQueue;
        private boolean expResult;

        public TestParameters(boolean expResult, long zxid, boolean fillQueu){
            this.zxid = zxid;
            this.fillQueue = fillQueue;
            this.expResult = expResult;
        }

        public long getZxid() {
            return zxid;
        }

        public boolean isFillQueue() {
            return fillQueue;
        }

        public boolean isExpResult() {
            return expResult;
        }
    }


    @Before
    public void setUpQueue() {
        //add some dummy packets in the queue if necessary
        if (fillQueue) {
            remQp = new QuorumPacket(0, FIRST_PKT_ID, "removed".getBytes(), null);
            obsM.proposalReceived(remQp);
            for (int i = FIRST_PKT_ID+1; i < LAST_PKT_ID; i++) {
                QuorumPacket qp = new QuorumPacket(0, i, "dummy".getBytes(), null);
                obsM.proposalReceived(qp);
            }
        }

    }

    @Test
    public void startForwardingTest() {

        QuorumPacket result;

        try {
            result = obsM.removeProposedPacket(zxid);
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unexpected proposal packet on commit ack, expected zxid 0x"+(FIRST_PKT_ID+1)+" got zxid 0x" + FIRST_PKT_ID);
            return;
        }

        Assert.assertTrue((!expResult && result == null) || (expResult && Arrays.equals(result.getData(), remQp.getData())));

    }



}