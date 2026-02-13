package org.example;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;

import java.util.Set;

import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;

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
        System.out.println("SecurityPolicy: None / MessageSecurityMode: None / Auth: Anonymous");
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
                .setUserTokenPolicies(Set.of(USER_TOKEN_POLICY_ANONYMOUS))
                .build();

        OpcUaServerConfig config = OpcUaServerConfig.builder()
                .setApplicationUri(APP_URI)
                .setApplicationName(LocalizedText.english("LS eXP2 OPC UA Test Server"))
                .setBindAddresses(Set.of(BIND_IP))
                .setBindPort(ENDPOINT_PORT)
                .setEndpoints(Set.of(endpoint))
                .setIdentityValidator(new AnonymousIdentityValidator())
                .setBuildInfo(new BuildInfo(
                        APP_URI,
                        "openai",
                        "LS eXP2 OPC UA Test Server",
                        OpcUaServer.SDK_VERSION,
                        "2.0.0",
                        DateTime.now()
                ))
                .build();

        return new OpcUaServer(config);
    }
}
