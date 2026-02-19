package org.example;

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.NodeManager;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
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

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String APP_URI = "urn:lsexp2:test:opcua:server";
    private static final String BIND_IP = "192.168.89.2";
    private static final String BIND_ADDRESS = "0.0.0.0";
    private static final int ENDPOINT_PORT = 8624;
    private static final String ENDPOINT_PATH = "/lsexp2-test";
    private static final String ROOT_ENDPOINT_PATH = "/";

    public static void main(String[] args) throws Exception {
        OpcUaServer server = createServer();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        server.startup().get();

        UShort nsIndex = addDummyDataNodes(server);

        System.out.println("OPC UA Test Server started.");
        System.out.println("Discovery Endpoint: opc.tcp://" + BIND_IP + ":" + ENDPOINT_PORT + ROOT_ENDPOINT_PATH);
        System.out.println("Service Endpoint: opc.tcp://" + BIND_IP + ":" + ENDPOINT_PORT + ENDPOINT_PATH);
        System.out.println("SecurityPolicy: None / MessageSecurityMode: None / Auth: Anonymous");
        System.out.println("Namespace Index used for dummy nodes: ns=" + nsIndex.intValue());
        System.out.println("Dummy NodeIds:");
        System.out.println(" - ns=" + nsIndex.intValue() + ";s=LS_EXP2/Heartbeat (Boolean)");
        System.out.println(" - ns=" + nsIndex.intValue() + ";s=LS_EXP2/temp (Int16)");
        System.out.println(" - ns=" + nsIndex.intValue() + ";s=LS_EXP2/HmiText (String, Read/Write)");
        System.out.println("Ctrl+C로 서버를 종료할 수 있습니다.");

        Thread.currentThread().join();
    }

    private static OpcUaServer createServer() {
        EndpointConfiguration serviceEndpoint = buildEndpoint(ENDPOINT_PATH);
        EndpointConfiguration discoveryEndpoint = buildEndpoint(ROOT_ENDPOINT_PATH);

        var configBuilder = OpcUaServerConfig.builder()
                .setEndpoints(Set.of(discoveryEndpoint, serviceEndpoint))
                .setIdentityValidator(new AnonymousIdentityValidator())
                .setBuildInfo(new BuildInfo(
                        APP_URI,
                        "openai",
                        "LS eXP2 OPC UA Test Server",
                        OpcUaServer.SDK_VERSION,
                        "2.1.3",
                        DateTime.now()
                ));

        invokeIfPresent(configBuilder, "setBindPort", ENDPOINT_PORT);

        OpcUaServerConfig config = configBuilder.build();

        return new OpcUaServer(config);
    }

    private static EndpointConfiguration buildEndpoint(String path) {
        EndpointConfiguration.Builder endpointBuilder = EndpointConfiguration.newBuilder()
                .setBindAddress(BIND_ADDRESS)
                .setHostname(BIND_IP)
                .setPath(path)
                .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .setSecurityPolicy(SecurityPolicy.None)
                .setSecurityMode(MessageSecurityMode.None);

        invokeIfPresent(endpointBuilder, "setBindPort", ENDPOINT_PORT);
        return endpointBuilder.build();
    }

    private static void invokeIfPresent(Object target, String methodName, int value) {
        try {
            Method m = target.getClass().getMethod(methodName, int.class);
            m.invoke(target, value);
        } catch (Exception ignored) {
            // Milo 버전별 API 차이를 허용하기 위한 no-op
        }
    }

    private static UShort addDummyDataNodes(OpcUaServer server) {
        UaNode objectsFolder = server.getAddressSpaceManager()
                .getManagedNode(Identifiers.ObjectsFolder)
                .orElseThrow(() -> new IllegalStateException("ObjectsFolder not found"));

        UShort nsIndex = objectsFolder.getNodeId().getNamespaceIndex();

        UaNodeContext nodeContext = objectsFolder.getNodeContext();
        @SuppressWarnings("unchecked")
        NodeManager<UaNode> nodeManager = (NodeManager<UaNode>) objectsFolder.getNodeManager();

        UaFolderNode rootFolder = new UaFolderNode(
                nodeContext,
                new NodeId(nsIndex, "LS_EXP2"),
                new QualifiedName(nsIndex, "LS_EXP2"),
                LocalizedText.english("LS_EXP2")
        );
        nodeManager.addNode(rootFolder);
        nodeManager.addReferences(new Reference(
                Identifiers.ObjectsFolder,
                Identifiers.Organizes,
                rootFolder.getNodeId().expanded(),
                true
        ), server.getNamespaceTable());

        UaVariableNode heartbeatNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/Heartbeat"))
                .setBrowseName(new QualifiedName(nsIndex, "Heartbeat"))
                .setDisplayName(LocalizedText.english("Heartbeat"))
                .setDataType(Identifiers.Boolean)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        heartbeatNode.setValue(new DataValue(new Variant(false)));
        nodeManager.addNode(heartbeatNode);
        nodeManager.addReferences(new Reference(
                rootFolder.getNodeId(),
                Identifiers.Organizes,
                heartbeatNode.getNodeId().expanded(),
                true
        ), server.getNamespaceTable());

        UaVariableNode tempNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/temp"))
                .setBrowseName(new QualifiedName(nsIndex, "temp"))
                .setDisplayName(LocalizedText.english("temp"))
                .setDataType(Identifiers.Int16)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        tempNode.setValue(new DataValue(new Variant((short) 250)));
        nodeManager.addNode(tempNode);
        nodeManager.addReferences(new Reference(
                rootFolder.getNodeId(),
                Identifiers.Organizes,
                tempNode.getNodeId().expanded(),
                true
        ), server.getNamespaceTable());

        UaVariableNode hmiTextNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/HmiText"))
                .setBrowseName(new QualifiedName(nsIndex, "HmiText"))
                .setDisplayName(LocalizedText.english("HmiText"))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        hmiTextNode.setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        hmiTextNode.setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        hmiTextNode.setValue(new DataValue(new Variant("초기값: 서버 UTF-8 테스트")));
        nodeManager.addNode(hmiTextNode);
        nodeManager.addReferences(new Reference(
                rootFolder.getNodeId(),
                Identifiers.Organizes,
                hmiTextNode.getNodeId().expanded(),
                true
        ), server.getNamespaceTable());

        var scheduler = Executors.newSingleThreadScheduledExecutor();
        final String[] lastClientText = {String.valueOf(hmiTextNode.getValue().getValue().getValue())};
        scheduler.scheduleAtFixedRate(() -> {
            boolean heartbeat = Boolean.TRUE.equals(heartbeatNode.getValue().getValue().getValue());
            heartbeatNode.setValue(new DataValue(new Variant(!heartbeat)));

            short tempRaw = (short) (200 + (int) (Math.random() * 120));
            tempNode.setValue(new DataValue(new Variant(tempRaw)));

            Object currentText = hmiTextNode.getValue().getValue().getValue();
            String currentTextValue = currentText == null ? "" : currentText.toString();
            if (!currentTextValue.equals(lastClientText[0])) {
                lastClientText[0] = currentTextValue;
                System.out.println("[CLIENT->SERVER] HmiText updated: " + currentTextValue);
            }
        }, 1, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));

        return nsIndex;
    }
}
