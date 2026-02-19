package org.example;

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.UaNodeManager;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

public class Main {

    private static final String APP_URI = "urn:lsexp2:test:opcua:server";
    private static final String BIND_IP = "192.168.89.2";
    private static final String ENDPOINT_PATH = "/lsexp2-test";

    private static final String NAMESPACE_URI = "urn:lsexp2:test:namespace";

    public static void main(String[] args) throws Exception {
        OpcUaServer server = createServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));

        addDummyDataNodes(server);

        server.startup().get();

        System.out.println("OPC UA Test Server started.");
        System.out.println("Endpoint: opc.tcp://" + BIND_IP + ":8624" + ENDPOINT_PATH);
        System.out.println("SecurityPolicy: None / MessageSecurityMode: None / Auth: Anonymous");
        System.out.println("Namespace URI: " + NAMESPACE_URI);
        System.out.println("Dummy NodeIds:");
        System.out.println(" - ns=2;s=LS_EXP2/Heartbeat (Boolean)");
        System.out.println(" - ns=2;s=LS_EXP2/temp (UInt16)");
        System.out.println("Ctrl+C로 서버를 종료할 수 있습니다.");

        Thread.currentThread().join();
    }

    private static OpcUaServer createServer() {
        EndpointConfiguration endpoint = EndpointConfiguration.newBuilder()
                .setBindAddress(BIND_IP)
                .setHostname(BIND_IP)
                .setPath(ENDPOINT_PATH)
                .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .setSecurityPolicy(SecurityPolicy.None)
                .setSecurityMode(MessageSecurityMode.None)
                .build();

        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setEndpoints(Set.of(endpoint))
                .setIdentityValidator(new AnonymousIdentityValidator())
                .setBuildInfo(new BuildInfo(
                        APP_URI,
                        "openai",
                        "LS eXP2 OPC UA Test Server",
                        OpcUaServer.SDK_VERSION,
                        "2.1.0",
                        DateTime.now()
                ))
                .build();

        return new OpcUaServer(config);
    }

    private static void addDummyDataNodes(OpcUaServer server) {
        UShort nsIndex = server.getNamespaceTable().addUri(NAMESPACE_URI);

        UaNodeManager nodeManager = new UaNodeManager();
        server.getAddressSpaceManager().register(nodeManager);

        UaNodeContext nodeContext = new UaNodeContext() {
            @Override
            public OpcUaServer getServer() {
                return server;
            }

            @Override
            public org.eclipse.milo.opcua.sdk.server.api.NodeManager<UaNode> getNodeManager() {
                return nodeManager;
            }
        };

        UaFolderNode rootFolder = new UaFolderNode(
                nodeContext,
                new NodeId(nsIndex, "LS_EXP2"),
                new QualifiedName(nsIndex, "LS_EXP2"),
                LocalizedText.english("LS_EXP2")
        );
        nodeManager.addNode(rootFolder);

        nodeManager.addReference(new Reference(
                Identifiers.ObjectsFolder,
                Identifiers.Organizes,
                rootFolder.getNodeId().expanded(),
                true
        ));

        UaVariableNode heartbeatNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/Heartbeat"))
                .setBrowseName(new QualifiedName(nsIndex, "Heartbeat"))
                .setDisplayName(LocalizedText.english("Heartbeat"))
                .setDataType(Identifiers.Boolean)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        heartbeatNode.setValue(new DataValue(new Variant(false)));
        nodeManager.addNode(heartbeatNode);
        rootFolder.addOrganizes(heartbeatNode);

        UaVariableNode tempNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/temp"))
                .setBrowseName(new QualifiedName(nsIndex, "temp"))
                .setDisplayName(LocalizedText.english("temp"))
                .setDataType(Identifiers.UInt16)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        tempNode.setValue(new DataValue(new Variant(ushort(250))));
        nodeManager.addNode(tempNode);
        rootFolder.addOrganizes(tempNode);

        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            boolean heartbeat = Boolean.TRUE.equals(heartbeatNode.getValue().getValue().getValue());
            heartbeatNode.setValue(new DataValue(new Variant(!heartbeat)));

            int tempRaw = 200 + (int) (Math.random() * 120);
            tempNode.setValue(new DataValue(new Variant(ushort(tempRaw))));
        }, 1, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }
}
