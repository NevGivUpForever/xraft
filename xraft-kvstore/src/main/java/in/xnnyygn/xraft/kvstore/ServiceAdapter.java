package in.xnnyygn.xraft.kvstore;

import in.xnnyygn.xraft.core.log.CommandApplyListener;
import in.xnnyygn.xraft.core.node.Node2;
import in.xnnyygn.xraft.core.node.NodeId;
import in.xnnyygn.xraft.core.nodestate.NodeRole;
import in.xnnyygn.xraft.core.nodestate.NodeStateSnapshot;
import in.xnnyygn.xraft.kvstore.command.SetCommand;
import org.apache.thrift.TException;

import java.util.concurrent.CountDownLatch;

public class ServiceAdapter implements KVStore.Iface, CommandApplyListener {

    private final Node2 node;
    private final Service service;

    public ServiceAdapter(Node2 node, Service service) {
        this.node = node;
        this.service = service;
        this.node.setCommandApplyListener(this);
    }

    @Override
    public void Set(String key, String value) throws TException {
        checkLeadership();
        CountDownLatch latch = new CountDownLatch(1);
        this.node.appendLog(new SetCommand(key, value).toBytes(), (commandBytes) -> {
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public GetResult Get(String key) throws TException {
        checkLeadership();
        String value = this.service.get(key);

        GetResult result = new GetResult();
        result.setFound(value != null);
        result.setValue(value);
        return result;
    }

    @Override
    public void applyCommand(int index, byte[] commandBytes) {
        SetCommand command = SetCommand.fromBytes(commandBytes);
        this.service.set(command.getKey(), command.getValue());
    }

    private void checkLeadership() throws Redirect {
        NodeStateSnapshot state = this.node.getNodeState();
        if (state.getRole() == NodeRole.FOLLOWER) {
            NodeId leaderId = state.getLeaderId();
            throw new Redirect(leaderId != null ? leaderId.getValue() : null);
        }
        if (state.getRole() == NodeRole.CANDIDATE) {
            throw new Redirect((String) null);
        }
    }

}