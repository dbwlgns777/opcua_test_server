package org.example;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.namespaces.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String APP_URI = "urn:lsexp2:test:opcua:server";
    private static final String BIND_IP = "192.168.89.2";
    private static final String ENDPOINT_PATH = "/lsexp2-test";
    private static final int ENDPOINT_PORT = 8624;

    public static void main(String[] args) throws Exception {
        OpcUaServer server = createServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        server.startup().get();

        System.out.println("OPC UA Test Server started.");
        System.out.println("Endpoint: opc.tcp://" + BIND_IP + ":" + ENDPOINT_PORT + ENDPOINT_PATH);
        System.out.println("Namespace URI: " + LsExp2Namespace.NAMESPACE_URI);
        System.out.println("Host IP(Detected): " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("Ctrl+C로 서버를 종료할 수 있습니다.");

        Thread.currentThread().join();
    }

    private static OpcUaServer createServer() throws Exception {
        EndpointConfiguration endpoint = EndpointConfiguration.newBuilder()
                .setBindAddress(BIND_IP)
                .setHostname(BIND_IP)
                .setPath(ENDPOINT_PATH)
                .setBindPort(ENDPOINT_PORT)
                .setSecurityPolicyUri(SecurityPolicy.None.getUri())
                .setSecurityMode(MessageSecurityMode.None)
                .addTokenPolicies(UserTokenPolicy.ANONYMOUS)
                .build();

        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setApplicationUri(APP_URI)
                .setApplicationName(LocalizedText.english("LS eXP2 OPC UA Test Server"))
                .setBuildInfo(new BuildInfo(
                        APP_URI,
                        "openai",
                        "LS eXP2 OPC UA Test Server",
                        OpcUaServer.SDK_VERSION,
                        "1.1.0",
                        DateTime.now()
                ))
                .setEndpoints(Set.of(endpoint))
                .setIdentityValidator(new AnonymousIdentityValidator(true))
                .build();

        OpcUaServer server = new OpcUaServer(config);
        server.getNamespaceManager().registerAndAdd(
                LsExp2Namespace.NAMESPACE_URI,
                idx -> new LsExp2Namespace(server)
        );

        return server;
    }

    public static class LsExp2Namespace extends ManagedNamespace {

        public static final String NAMESPACE_URI = "urn:lsexp2:test:namespace";

        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        public LsExp2Namespace(OpcUaServer server) {
            super(server, NAMESPACE_URI);
        }

        @Override
        protected void onStartup() {
            super.onStartup();

            UaFolderNode rootFolder = new UaFolderNode(
                    getNodeContext(),
                    newNodeId("LS_EXP2"),
                    newQualifiedName("LS_EXP2"),
                    LocalizedText.english("LS_EXP2")
            );
            getNodeManager().addNode(rootFolder);

            getServer().getUaNamespace().addReference(
                    org.eclipse.milo.opcua.stack.core.Identifiers.ObjectsFolder,
                    NodeClass.Object,
                    rootFolder.getNodeId().expanded(),
                    NodeClass.Object,
                    org.eclipse.milo.opcua.stack.core.Identifiers.Organizes,
                    true
            );

            UaVariableNode currentTemperatureNode = UaVariableNode.builder(getNodeContext())
                    .setNodeId(newNodeId("LS_EXP2/CurrentTemperature"))
                    .setBrowseName(newQualifiedName("CurrentTemperature"))
                    .setDisplayName(LocalizedText.english("CurrentTemperature"))
                    .setDataType(org.eclipse.milo.opcua.stack.core.Identifiers.Double)
                    .setTypeDefinition(org.eclipse.milo.opcua.stack.core.Identifiers.BaseDataVariableType)
                    .build();
            currentTemperatureNode.setValue(new DataValue(new Variant(23.5d)));
            getNodeManager().addNode(currentTemperatureNode);
            rootFolder.addOrganizes(currentTemperatureNode);

            UaVariableNode heartbeatNode = UaVariableNode.builder(getNodeContext())
                    .setNodeId(newNodeId("LS_EXP2/Heartbeat"))
                    .setBrowseName(newQualifiedName("Heartbeat"))
                    .setDisplayName(LocalizedText.english("Heartbeat"))
                    .setDataType(org.eclipse.milo.opcua.stack.core.Identifiers.Boolean)
                    .setTypeDefinition(org.eclipse.milo.opcua.stack.core.Identifiers.BaseDataVariableType)
                    .build();
            heartbeatNode.setValue(new DataValue(new Variant(false)));
            getNodeManager().addNode(heartbeatNode);
            rootFolder.addOrganizes(heartbeatNode);

            UaVariableNode serverTimeNode = UaVariableNode.builder(getNodeContext())
                    .setNodeId(newNodeId("LS_EXP2/ServerTime"))
                    .setBrowseName(newQualifiedName("ServerTime"))
                    .setDisplayName(LocalizedText.english("ServerTime"))
                    .setDataType(org.eclipse.milo.opcua.stack.core.Identifiers.DateTime)
                    .setTypeDefinition(org.eclipse.milo.opcua.stack.core.Identifiers.BaseDataVariableType)
                    .build();
            serverTimeNode.setValue(new DataValue(new Variant(DateTime.now())));
            getNodeManager().addNode(serverTimeNode);
            rootFolder.addOrganizes(serverTimeNode);

            scheduler.scheduleAtFixedRate(() -> {
                double changingTemperature = 20.0d + Math.random() * 10.0d;
                currentTemperatureNode.setValue(new DataValue(
                        new Variant(changingTemperature),
                        StatusCode.GOOD,
                        DateTime.now(),
                        DateTime.now()
                ));

                boolean heartbeat = Boolean.TRUE.equals(heartbeatNode.getValue().getValue().getValue());
                heartbeatNode.setValue(new DataValue(
                        new Variant(!heartbeat),
                        StatusCode.GOOD,
                        DateTime.now(),
                        DateTime.now()
                ));

                serverTimeNode.setValue(new DataValue(new Variant(DateTime.of(
                        LocalDateTime.now().toInstant(ZoneOffset.UTC)
                ))));
            }, 1, 1, TimeUnit.SECONDS);
        }

        @Override
        protected void onShutdown() {
            super.onShutdown();
            scheduler.shutdownNow();
        }
    }
}
