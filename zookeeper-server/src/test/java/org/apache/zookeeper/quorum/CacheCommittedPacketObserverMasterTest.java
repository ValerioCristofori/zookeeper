package org.apache.zookeeper.quorum;

import org.apache.zookeeper.ParseCommandZookeeperMainTest;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.quorum.Learner;
import org.apache.zookeeper.server.quorum.LearnerHandler;
import org.apache.zookeeper.server.quorum.ObserverMaster;
import org.apache.zookeeper.server.quorum.QuorumPacket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(value = Parameterized.class)
public class CacheCommittedPacketObserverMasterTest {

    private static int pktSizeLimit;

    //class to test
    private ObserverMaster obsM;

    //arguments
    private boolean expResult;
    private QuorumPacket qp;



    public CacheCommittedPacketObserverMasterTest(TestParameters input) {
        //the arguments of the contructor are not used in the method we are testing
        //we can leave them as null
        obsM = new ObserverMaster(null, null, 80);
        //System.setProperty("zookeeper.observerMaster.sizeLimit", "2000");
        this.expResult = input.isExpResult();
        this.qp = input.getQp();

    }



    @Parameterized.Parameters
    public static Collection<TestParameters> getTestParameters() {
        //function signature
        //void cacheCommittedPacket(final QuorumPacket pkt)

        //some auth info - not relevant to the test
        Id dummyId = new Id("dummy", "dummy");
        List<Id> dummyAuthInfo = new ArrayList<>();
        dummyAuthInfo.add(dummyId);


        //filling the byte arrays with dummy data
        byte[] almostOverSizedArray = new byte[299-28];
        byte[] overSizedArray = new byte[350];
        byte[] overOverSizedArray = new byte[6713523];

        QuorumPacket undersizedPkt = new QuorumPacket(0, 1234, "dummyy".getBytes(), dummyAuthInfo);
        QuorumPacket almostOverSizedPkt = new QuorumPacket(0, 1234, almostOverSizedArray, dummyAuthInfo);
        QuorumPacket overSizedPkt = new QuorumPacket(0, 1234, overSizedArray, dummyAuthInfo);
        QuorumPacket overOverSizedPkt = new QuorumPacket(0, 1234, overOverSizedArray, dummyAuthInfo);

        List<TestParameters> inputs = new ArrayList<>();

        inputs.add(new TestParameters(false, null));
        inputs.add(new TestParameters(true, undersizedPkt));

        inputs.add(new TestParameters(true, almostOverSizedPkt));
        inputs.add(new TestParameters(true, overSizedPkt));
        inputs.add(new TestParameters(true, overOverSizedPkt));

        return inputs;


    }

    public static class TestParameters {
        private boolean expResult;
        private QuorumPacket qp;

        public TestParameters(boolean expResult, QuorumPacket qp){
            this.expResult = expResult;
            this.qp = qp;
        }

        public boolean isExpResult() {
            return expResult;
        }

        public QuorumPacket getQp() {
            return qp;
        }
    }

    @Before
    public void setUpQueue() {
        //add some dummy packets in the queue to check if they are removed correctly
        pktSizeLimit = Integer.getInteger("zookeeper.observerMaster.sizeLimit", 32 * 1024 * 1024);

        //System.out.println("askd: " + pktSizeLimit + " " + pktNumber);
        long pktSize = 0;
        while (pktSize < pktSizeLimit-300) {
            QuorumPacket qp = new QuorumPacket(0, 1234, "d".getBytes(), null);
            obsM.cacheCommittedPacket(qp);
            pktSize += LearnerHandler.packetSize(qp);
        }

        //System.out.println("askd: " + (pktSize - pktSizeLimit));

    }

    @Test
    public void cacheCommittedPacketTest() {


        //call the test method
        try {
            obsM.cacheCommittedPacket(qp);
        } catch (NullPointerException e) {
            Assert.assertNull(qp);
            return;
        }

        //get the committed packet queue
        ConcurrentLinkedQueue<QuorumPacket> queue = obsM.getCommittedPkts();

        //check that our packet has been added
        Assert.assertTrue(!expResult || queue.contains(qp));

        //check that queue has not exceeded maximum size
        int queueSize = 0;
        QuorumPacket pkt;
        while (true) {
            pkt = queue.poll();
            if (pkt == null) {
                break;
            }
            queueSize += LearnerHandler.packetSize(pkt);
        }

        Assert.assertTrue(queueSize < pktSizeLimit);



    }

}