package org.example;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.api.config.UserTokenPolicy;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.namespaces.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.server.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.server.security.ServerCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedHttpsCertificateBuilder;

import java.net.InetAddress;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String APP_URI = "urn:lsexp2:test:opcua:server";
    private static final String ENDPOINT_PATH = "/lsexp2-test";
    private static final int ENDPOINT_PORT = 12686;

    public static void main(String[] args) throws Exception {
        OpcUaServer server = createServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        server.startup().get();
        System.out.println("OPC UA Test Server started.");
        System.out.println("Endpoint: opc.tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + ENDPOINT_PORT + ENDPOINT_PATH);
        System.out.println("Namespace URI: " + LsExp2Namespace.NAMESPACE_URI);
        System.out.println("Ctrl+C로 서버를 종료할 수 있습니다.");

        Thread.currentThread().join();
    }

    private static OpcUaServer createServer() throws Exception {
        Path securityDir = Path.of("security");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X509Certificate certificate = new SelfSignedHttpsCertificateBuilder(keyPair)
                .setCommonName("LS-eXP2 OPC UA Test Server")
                .setOrganization("Local Test")
                .setOrganizationalUnit("Automation")
                .setLocalityName("Seoul")
                .setStateName("Seoul")
                .setCountryCode("KR")
                .setApplicationUri(APP_URI)
                .addDnsName("localhost")
                .addIpAddress("127.0.0.1")
                .build();

        DefaultCertificateManager certificateManager = new DefaultCertificateManager(keyPair, certificate);
        DefaultTrustListManager trustListManager = new DefaultTrustListManager(securityDir);
        ServerCertificateValidator certificateValidator = new ServerCertificateValidator(trustListManager);

        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setApplicationUri(APP_URI)
                .setApplicationName(LocalizedText.english("LS eXP2 OPC UA Test Server"))
                .setBindAddresses(Set.of("0.0.0.0"))
                .setBindPort(ENDPOINT_PORT)
                .setEndpoints(Set.of(
                        new OpcUaServerConfig.OpcUaEndpointConfigBuilder()
                                .setPath(ENDPOINT_PATH)
                                .setHostname("0.0.0.0")
                                .setSecurityPolicy(SecurityPolicy.None)
                                .setSecurityMode(MessageSecurityMode.None)
                                .setTransportProfile(org.eclipse.milo.opcua.stack.core.Stack.UA_TCP_BINARY_TRANSPORT_URI)
                                .setUserTokenPolicies(Set.of(UserTokenPolicy.ANONYMOUS))
                                .build()
                ))
                .setBuildInfo(new BuildInfo(
                        APP_URI,
                        "openai",
                        "LS eXP2 OPC UA Test Server",
                        OpcUaServer.SDK_VERSION,
                        "1.0.0",
                        DateTime.now()
                ))
                .setCertificateManager(certificateManager)
                .setTrustListManager(trustListManager)
                .setCertificateValidator(certificateValidator)
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
        private UaVariableNode currentTemperatureNode;
        private UaVariableNode heartbeatNode;

        public LsExp2Namespace(OpcUaServer server) {
            super(server, NAMESPACE_URI);
            onStartup();
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
                    org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass.Object,
                    rootFolder.getNodeId().expanded(),
                    org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass.Object,
                    org.eclipse.milo.opcua.stack.core.Identifiers.Organizes,
                    true
            );

            currentTemperatureNode = UaVariableNode.builder(getNodeContext())
                    .setNodeId(newNodeId("LS_EXP2/CurrentTemperature"))
                    .setBrowseName(newQualifiedName("CurrentTemperature"))
                    .setDisplayName(LocalizedText.english("CurrentTemperature"))
                    .setDataType(org.eclipse.milo.opcua.stack.core.Identifiers.Double)
                    .setTypeDefinition(org.eclipse.milo.opcua.stack.core.Identifiers.BaseDataVariableType)
                    .build();
            currentTemperatureNode.setValue(new DataValue(new Variant(23.5)));
            getNodeManager().addNode(currentTemperatureNode);
            rootFolder.addOrganizes(currentTemperatureNode);

            heartbeatNode = UaVariableNode.builder(getNodeContext())
                    .setNodeId(newNodeId("LS_EXP2/Heartbeat"))
                    .setBrowseName(newQualifiedName("Heartbeat"))
                    .setDisplayName(LocalizedText.english("Heartbeat"))
                    .setDataType(org.eclipse.milo.opcua.stack.core.Identifiers.Boolean)
                    .setTypeDefinition(org.eclipse.milo.opcua.stack.core.Identifiers.BaseDataVariableType)
                    .build();
            heartbeatNode.setValue(new DataValue(new Variant(Boolean.FALSE)));
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
                double changingTemperature = 20 + Math.random() * 10;
                currentTemperatureNode.setValue(new DataValue(new Variant(changingTemperature),
                        StatusCode.GOOD, DateTime.now(), DateTime.now()));

                boolean heartbeat = (boolean) heartbeatNode.getValue().getValue().getValue();
                heartbeatNode.setValue(new DataValue(new Variant(!heartbeat),
                        StatusCode.GOOD, DateTime.now(), DateTime.now()));

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
