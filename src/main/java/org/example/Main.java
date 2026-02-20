package org.example;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.NodeManager;
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

    private record WorkItem(String productCode, String productName, String customer, String process, String workDeadline) {
    }

    private static final WorkItem[] DESC_WORK_ITEMS = {
            new WorkItem("P015", "PRODUCT-015", "HANKOOK", "PACKING", "2026-03-15"),
            new WorkItem("P014", "PRODUCT-014", "HYUNDAI", "QC", "2026-03-14"),
            new WorkItem("P013", "PRODUCT-013", "SAMSUNG", "PAINT", "2026-03-13"),
            new WorkItem("P012", "PRODUCT-012", "LG", "WELD", "2026-03-12"),
            new WorkItem("P011", "PRODUCT-011", "KIA", "MOLD", "2026-03-11"),
            new WorkItem("P010", "PRODUCT-010", "LOTTE", "PACKING", "2026-03-10"),
            new WorkItem("P009", "PRODUCT-009", "POSCO", "QC", "2026-03-09"),
            new WorkItem("P008", "PRODUCT-008", "HANWHA", "PAINT", "2026-03-08"),
            new WorkItem("P007", "PRODUCT-007", "SK", "WELD", "2026-03-07"),
            new WorkItem("P006", "PRODUCT-006", "CJ", "MOLD", "2026-03-06"),
            new WorkItem("P005", "PRODUCT-005", "DOOSAN", "PACKING", "2026-03-05"),
            new WorkItem("P004", "PRODUCT-004", "HYOSUNG", "QC", "2026-03-04"),
            new WorkItem("P003", "PRODUCT-003", "KOLON", "PAINT", "2026-03-03"),
            new WorkItem("P002", "PRODUCT-002", "AMORE", "WELD", "2026-03-02"),
            new WorkItem("P001", "PRODUCT-001", "NONGSHIM", "MOLD", "2026-03-01")
    };

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
        System.out.println("WorkReport Request NodeId: ns=" + nsIndex.intValue() + ";s=LS_EXP2/workReportRequest (Int16, Write 1~3)");
        System.out.println("WorkReport Row1 Example: ns=" + nsIndex.intValue() + ";s=LS_EXP2/workReport/row1/productcode (String)");
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
                        "2.2.0",
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
        linkChild(nodeManager, server, rootFolder, heartbeatNode);

        UaVariableNode tempNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/temp"))
                .setBrowseName(new QualifiedName(nsIndex, "temp"))
                .setDisplayName(LocalizedText.english("temp"))
                .setDataType(Identifiers.Int16)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        tempNode.setValue(new DataValue(new Variant((short) 250)));
        nodeManager.addNode(tempNode);
        linkChild(nodeManager, server, rootFolder, tempNode);

        UaVariableNode requestNode = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, "LS_EXP2/workReportRequest"))
                .setBrowseName(new QualifiedName(nsIndex, "workReportRequest"))
                .setDisplayName(LocalizedText.english("workReportRequest"))
                .setDataType(Identifiers.Int16)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        requestNode.setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        requestNode.setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        requestNode.setValue(new DataValue(new Variant((short) 1)));
        nodeManager.addNode(requestNode);
        linkChild(nodeManager, server, rootFolder, requestNode);

        UaVariableNode[] productCodeNodes = new UaVariableNode[5];
        UaVariableNode[] productNameNodes = new UaVariableNode[5];
        UaVariableNode[] customerNodes = new UaVariableNode[5];
        UaVariableNode[] processNodes = new UaVariableNode[5];
        UaVariableNode[] workDeadlineNodes = new UaVariableNode[5];

        for (int i = 0; i < 5; i++) {
            int row = i + 1;
            productCodeNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/productcode", "productcode_row" + row);
            productNameNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/productname", "productname_row" + row);
            customerNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/customer", "customer_row" + row);
            processNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/process", "process_row" + row);
            workDeadlineNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/workdeadline", "workdeadline_row" + row);

            nodeManager.addNode(productCodeNodes[i]);
            nodeManager.addNode(productNameNodes[i]);
            nodeManager.addNode(customerNodes[i]);
            nodeManager.addNode(processNodes[i]);
            nodeManager.addNode(workDeadlineNodes[i]);

            linkChild(nodeManager, server, rootFolder, productCodeNodes[i]);
            linkChild(nodeManager, server, rootFolder, productNameNodes[i]);
            linkChild(nodeManager, server, rootFolder, customerNodes[i]);
            linkChild(nodeManager, server, rootFolder, processNodes[i]);
            linkChild(nodeManager, server, rootFolder, workDeadlineNodes[i]);
        }

        applyWorkReportPage((short) 1, productCodeNodes, productNameNodes, customerNodes, processNodes, workDeadlineNodes);

        var scheduler = Executors.newSingleThreadScheduledExecutor();
        final short[] lastRequestValue = {1};
        scheduler.scheduleAtFixedRate(() -> {
            boolean heartbeat = Boolean.TRUE.equals(heartbeatNode.getValue().getValue().getValue());
            heartbeatNode.setValue(new DataValue(new Variant(!heartbeat)));

            short tempRaw = (short) (200 + (int) (Math.random() * 120));
            tempNode.setValue(new DataValue(new Variant(tempRaw)));

            short requestValue = normalizeRequest(readShortValue(requestNode.getValue().getValue().getValue()));
            if (requestValue != lastRequestValue[0]) {
                lastRequestValue[0] = requestValue;
                requestNode.setValue(new DataValue(new Variant(requestValue)));
                applyWorkReportPage(requestValue, productCodeNodes, productNameNodes, customerNodes, processNodes, workDeadlineNodes);
                System.out.println("[CLIENT->SERVER] workReportRequest=" + requestValue + " applied (5 rows)");
            }
        }, 1, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));

        return nsIndex;
    }

    private static UaVariableNode createStringNode(UaNodeContext nodeContext, UShort nsIndex, String id, String browseName) {
        UaVariableNode node = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, id))
                .setBrowseName(new QualifiedName(nsIndex, browseName))
                .setDisplayName(LocalizedText.english(browseName))
                .setDataType(Identifiers.String)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setValue(new DataValue(new Variant("")));
        return node;
    }

    private static void linkChild(NodeManager<UaNode> nodeManager, OpcUaServer server, UaFolderNode parent, UaVariableNode child) {
        nodeManager.addReferences(new Reference(
                parent.getNodeId(),
                Identifiers.Organizes,
                child.getNodeId().expanded(),
                true
        ), server.getNamespaceTable());
    }

    private static void applyWorkReportPage(short request,
                                            UaVariableNode[] productCodeNodes,
                                            UaVariableNode[] productNameNodes,
                                            UaVariableNode[] customerNodes,
                                            UaVariableNode[] processNodes,
                                            UaVariableNode[] workDeadlineNodes) {
        int offset = (request - 1) * 5;
        for (int i = 0; i < 5; i++) {
            WorkItem item = DESC_WORK_ITEMS[offset + i];
            productCodeNodes[i].setValue(new DataValue(new Variant(item.productCode())));
            productNameNodes[i].setValue(new DataValue(new Variant(item.productName())));
            customerNodes[i].setValue(new DataValue(new Variant(item.customer())));
            processNodes[i].setValue(new DataValue(new Variant(item.process())));
            workDeadlineNodes[i].setValue(new DataValue(new Variant(item.workDeadline())));
        }
    }

    private static short normalizeRequest(short value) {
        if (value < 1) return 1;
        if (value > 3) return 3;
        return value;
    }

    private static short readShortValue(Object value) {
        if (value instanceof Number number) {
            return number.shortValue();
        }
        try {
            return Short.parseShort(String.valueOf(value));
        } catch (Exception ignored) {
            return 1;
        }
    }
}
