package org.apache.zookeeper.quorum;

import org.apache.zookeeper.data.Id;
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

    private ObserverMaster obsM;

    private boolean expResult;
    private QuorumPacket qp;



    public CacheCommittedPacketObserverMasterTest(TestParameters input) {
        obsM = new ObserverMaster(null, null, 80);
        this.expResult = input.isExpResult();
        this.qp = input.getQp();

    }



    @Parameterized.Parameters
    public static Collection<TestParameters> getTestParameters() {
        //auth info non rilevanti per il test
        Id dummyId = new Id("dummy", "dummy");
        List<Id> dummyAuthInfo = new ArrayList<>();
        dummyAuthInfo.add(dummyId);


        // 3 tipi di byte array
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
    public void setup() {
        // aggiungo alcuni pacchetti dummy nella coda
        // controllo poi se sono stati eliminati correttamente
        pktSizeLimit = Integer.getInteger("zookeeper.observerMaster.sizeLimit", 32 * 1024 * 1024);

        long pktSize = 0;
        // lascio 300 bytes di spazio libero, il resto lo fillo con pacchetti dummy
        while (pktSize < pktSizeLimit-300) {
            QuorumPacket qp = new QuorumPacket(0, 1234, "d".getBytes(), null);
            obsM.cacheCommittedPacket(qp);
            pktSize += LearnerHandler.packetSize(qp);
        }
    }

    @Test
    public void start() {

        try {
            obsM.cacheCommittedPacket(qp);
        } catch (NullPointerException e) {
            Assert.assertNull(qp);
            return;
        }

        // prendo la coda e controllo che il pacchetto sia stato inserito
        ConcurrentLinkedQueue<QuorumPacket> queue = obsM.getCommittedPkts();
        Assert.assertTrue(!expResult || queue.contains(qp));

        //controllo che la coda non eccede la lunghezza massima
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