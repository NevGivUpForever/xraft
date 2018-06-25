package in.xnnyygn.xraft.kvstore;

import in.xnnyygn.xraft.core.node.NodeId;
import in.xnnyygn.xraft.core.service.ServerRouter;

public class ClientLauncher {

    public static void main(String[] args) throws Exception {
        ServerRouter serverRouter = new ServerRouter();
        serverRouter.add(new NodeId("A"), new SocketChannel("127.0.0.1", 3333));
        serverRouter.add(new NodeId("B"), new SocketChannel("127.0.0.1", 3334));
        serverRouter.add(new NodeId("C"), new SocketChannel("127.0.0.1", 3335));
        Client client = new Client(serverRouter);
        System.out.println(client.get("x"));
        client.set("x", "2");
        System.out.println(client.get("x"));
    }
}
