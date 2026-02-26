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

    private record WorkItem(
            String productCode,
            String productName,
            String customer,
            String process,
            String workDeadline,
            short targetQuantity
    ) {
    }

    private static final WorkItem[] EQUIPMENT_1_WORK_ITEMS = {
            new WorkItem("P015", "PRODUCT-015", "HANKOOK", "PACKING", "2026-03-15", (short) 950),
            new WorkItem("P014", "PRODUCT-014", "HYUNDAI", "QC", "2026-03-14", (short) 920),
            new WorkItem("P013", "PRODUCT-013", "SAMSUNG", "PAINT", "2026-03-13", (short) 900),
            new WorkItem("P012", "PRODUCT-012", "LG", "WELD", "2026-03-12", (short) 880),
            new WorkItem("P011", "PRODUCT-011", "KIA", "MOLD", "2026-03-11", (short) 850),
            new WorkItem("P010", "PRODUCT-010", "LOTTE", "PACKING", "2026-03-10", (short) 820),
            new WorkItem("P009", "PRODUCT-009", "POSCO", "QC", "2026-03-09", (short) 800),
            new WorkItem("P008", "PRODUCT-008", "HANWHA", "PAINT", "2026-03-08", (short) 780),
            new WorkItem("P007", "PRODUCT-007", "SK", "WELD", "2026-03-07", (short) 760),
            new WorkItem("P006", "PRODUCT-006", "CJ", "MOLD", "2026-03-06", (short) 740),
            new WorkItem("P005", "PRODUCT-005", "DOOSAN", "PACKING", "2026-03-05", (short) 720),
            new WorkItem("P004", "PRODUCT-004", "HYOSUNG", "QC", "2026-03-04", (short) 700),
            new WorkItem("P003", "PRODUCT-003", "KOLON", "PAINT", "2026-03-03", (short) 680),
            new WorkItem("P002", "PRODUCT-002", "AMORE", "WELD", "2026-03-02", (short) 660),
            new WorkItem("P001", "PRODUCT-001", "NONGSHIM", "MOLD", "2026-03-01", (short) 640)
    };

    private static final WorkItem[] EQUIPMENT_2_WORK_ITEMS = {
            new WorkItem("E2-P015", "EQ2-PRODUCT-015", "LOTTE", "ASSEMBLY", "2026-04-15", (short) 880),
            new WorkItem("E2-P014", "EQ2-PRODUCT-014", "POSCO", "MACHINING", "2026-04-14", (short) 860),
            new WorkItem("E2-P013", "EQ2-PRODUCT-013", "HANWHA", "QC", "2026-04-13", (short) 840),
            new WorkItem("E2-P012", "EQ2-PRODUCT-012", "SK", "PAINT", "2026-04-12", (short) 820),
            new WorkItem("E2-P011", "EQ2-PRODUCT-011", "CJ", "PACKING", "2026-04-11", (short) 800),
            new WorkItem("E2-P010", "EQ2-PRODUCT-010", "DOOSAN", "ASSEMBLY", "2026-04-10", (short) 780),
            new WorkItem("E2-P009", "EQ2-PRODUCT-009", "HYOSUNG", "MACHINING", "2026-04-09", (short) 760),
            new WorkItem("E2-P008", "EQ2-PRODUCT-008", "KOLON", "QC", "2026-04-08", (short) 740),
            new WorkItem("E2-P007", "EQ2-PRODUCT-007", "AMORE", "PAINT", "2026-04-07", (short) 720),
            new WorkItem("E2-P006", "EQ2-PRODUCT-006", "NONGSHIM", "PACKING", "2026-04-06", (short) 700),
            new WorkItem("E2-P005", "EQ2-PRODUCT-005", "HANKOOK", "ASSEMBLY", "2026-04-05", (short) 680),
            new WorkItem("E2-P004", "EQ2-PRODUCT-004", "HYUNDAI", "MACHINING", "2026-04-04", (short) 660),
            new WorkItem("E2-P003", "EQ2-PRODUCT-003", "SAMSUNG", "QC", "2026-04-03", (short) 640),
            new WorkItem("E2-P002", "EQ2-PRODUCT-002", "LG", "PAINT", "2026-04-02", (short) 620),
            new WorkItem("E2-P001", "EQ2-PRODUCT-001", "KIA", "PACKING", "2026-04-01", (short) 600)
    };

    private static final WorkItem[] EQUIPMENT_3_WORK_ITEMS = {
            new WorkItem("E3-P015", "EQ3-PRODUCT-015", "ACE", "CUTTING", "2026-05-15", (short) 990),
            new WorkItem("E3-P014", "EQ3-PRODUCT-014", "BETA", "WELD", "2026-05-14", (short) 970),
            new WorkItem("E3-P013", "EQ3-PRODUCT-013", "COSMO", "MOLD", "2026-05-13", (short) 950),
            new WorkItem("E3-P012", "EQ3-PRODUCT-012", "DELTA", "INSPECTION", "2026-05-12", (short) 930),
            new WorkItem("E3-P011", "EQ3-PRODUCT-011", "ECHO", "PACKING", "2026-05-11", (short) 910),
            new WorkItem("E3-P010", "EQ3-PRODUCT-010", "FUTURE", "CUTTING", "2026-05-10", (short) 890),
            new WorkItem("E3-P009", "EQ3-PRODUCT-009", "GAMMA", "WELD", "2026-05-09", (short) 870),
            new WorkItem("E3-P008", "EQ3-PRODUCT-008", "HERA", "MOLD", "2026-05-08", (short) 850),
            new WorkItem("E3-P007", "EQ3-PRODUCT-007", "ION", "INSPECTION", "2026-05-07", (short) 830),
            new WorkItem("E3-P006", "EQ3-PRODUCT-006", "JUNO", "PACKING", "2026-05-06", (short) 810),
            new WorkItem("E3-P005", "EQ3-PRODUCT-005", "KRONOS", "CUTTING", "2026-05-05", (short) 790),
            new WorkItem("E3-P004", "EQ3-PRODUCT-004", "LUNA", "WELD", "2026-05-04", (short) 770),
            new WorkItem("E3-P003", "EQ3-PRODUCT-003", "MARS", "MOLD", "2026-05-03", (short) 750),
            new WorkItem("E3-P002", "EQ3-PRODUCT-002", "NOVA", "INSPECTION", "2026-05-02", (short) 730),
            new WorkItem("E3-P001", "EQ3-PRODUCT-001", "ORION", "PACKING", "2026-05-01", (short) 710)
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
        System.out.println("Equipment NodeId: ns=" + nsIndex.intValue() + ";s=LS_EXP2/selectedEquipment (Int32: 1|2|1234567)");
        System.out.println("Page NodeId: ns=" + nsIndex.intValue() + ";s=LS_EXP2/workReportCurrentPage (Int16, Write 1~3)");
        System.out.println("TotalPage NodeId: ns=" + nsIndex.intValue() + ";s=LS_EXP2/workReportTotalPage (Int16, ReadOnly)");
        System.out.println("Row NodeId: ns=" + nsIndex.intValue() + ";s=LS_EXP2/workReportSelectedRow (Int16, Write 1~5)");
        System.out.println("Detail Example: ns=" + nsIndex.intValue() + ";s=LS_EXP2/workReport/detail/targetQuantityDetail (Int16)");
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
                        "2.4.0",
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

        UaVariableNode tempNode = createInt16Node(nodeContext, nsIndex, "LS_EXP2/temp", "temp");
        tempNode.setValue(new DataValue(new Variant((short) 250)));
        nodeManager.addNode(tempNode);
        linkChild(nodeManager, server, rootFolder, tempNode);

        UaVariableNode selectedEquipmentNode = createInt32RwNode(nodeContext, nsIndex,
                "LS_EXP2/selectedEquipment", "selectedEquipment");
        selectedEquipmentNode.setValue(new DataValue(new Variant(1)));
        nodeManager.addNode(selectedEquipmentNode);
        linkChild(nodeManager, server, rootFolder, selectedEquipmentNode);

        UaVariableNode currentPageNode = createInt16RwNode(nodeContext, nsIndex,
                "LS_EXP2/workReportCurrentPage", "workReportCurrentPage");
        currentPageNode.setValue(new DataValue(new Variant((short) 1)));
        nodeManager.addNode(currentPageNode);
        linkChild(nodeManager, server, rootFolder, currentPageNode);

        UaVariableNode totalPageNode = createInt16Node(nodeContext, nsIndex,
                "LS_EXP2/workReportTotalPage", "workReportTotalPage");
        totalPageNode.setAccessLevel(AccessLevel.toValue(AccessLevel.READ_ONLY));
        totalPageNode.setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_ONLY));
        totalPageNode.setValue(new DataValue(new Variant((short) 3)));
        nodeManager.addNode(totalPageNode);
        linkChild(nodeManager, server, rootFolder, totalPageNode);

        UaVariableNode selectedRowNode = createInt16RwNode(nodeContext, nsIndex,
                "LS_EXP2/workReportSelectedRow", "workReportSelectedRow");
        selectedRowNode.setValue(new DataValue(new Variant((short) 1)));
        nodeManager.addNode(selectedRowNode);
        linkChild(nodeManager, server, rootFolder, selectedRowNode);

        UaVariableNode workStartNode = createBooleanRwNode(nodeContext, nsIndex, "LS_EXP2/workStart", "workStart");
        UaVariableNode workPauseNode = createBooleanRwNode(nodeContext, nsIndex, "LS_EXP2/workPause", "workPause");
        UaVariableNode productionCounterNode = createInt16RwNode(nodeContext, nsIndex, "LS_EXP2/productionCounter", "productionCounter");
        UaVariableNode defectCountNode = createInt16RwNode(nodeContext, nsIndex, "LS_EXP2/defectCount", "defectCount");
        UaVariableNode workEndNode = createBooleanRwNode(nodeContext, nsIndex, "LS_EXP2/workEnd", "workEnd");

        nodeManager.addNode(workStartNode);
        nodeManager.addNode(workPauseNode);
        nodeManager.addNode(productionCounterNode);
        nodeManager.addNode(defectCountNode);
        nodeManager.addNode(workEndNode);

        linkChild(nodeManager, server, rootFolder, workStartNode);
        linkChild(nodeManager, server, rootFolder, workPauseNode);
        linkChild(nodeManager, server, rootFolder, productionCounterNode);
        linkChild(nodeManager, server, rootFolder, defectCountNode);
        linkChild(nodeManager, server, rootFolder, workEndNode);

        UaVariableNode[] productCodeNodes = new UaVariableNode[5];
        UaVariableNode[] productNameNodes = new UaVariableNode[5];
        UaVariableNode[] customerNodes = new UaVariableNode[5];
        UaVariableNode[] processNodes = new UaVariableNode[5];
        UaVariableNode[] workDeadlineNodes = new UaVariableNode[5];
        UaVariableNode[] targetQuantityNodes = new UaVariableNode[5];

        for (int i = 0; i < 5; i++) {
            int row = i + 1;
            productCodeNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/productcode", "productcode_row" + row);
            productNameNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/productname", "productname_row" + row);
            customerNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/customer", "customer_row" + row);
            processNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/process", "process_row" + row);
            workDeadlineNodes[i] = createStringNode(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/workdeadline", "workdeadline_row" + row);
            targetQuantityNodes[i] = createInt16Node(nodeContext, nsIndex, "LS_EXP2/workReport/row" + row + "/targetQuantity", "targetQuantity_row" + row);

            nodeManager.addNode(productCodeNodes[i]);
            nodeManager.addNode(productNameNodes[i]);
            nodeManager.addNode(customerNodes[i]);
            nodeManager.addNode(processNodes[i]);
            nodeManager.addNode(workDeadlineNodes[i]);
            nodeManager.addNode(targetQuantityNodes[i]);

            linkChild(nodeManager, server, rootFolder, productCodeNodes[i]);
            linkChild(nodeManager, server, rootFolder, productNameNodes[i]);
            linkChild(nodeManager, server, rootFolder, customerNodes[i]);
            linkChild(nodeManager, server, rootFolder, processNodes[i]);
            linkChild(nodeManager, server, rootFolder, workDeadlineNodes[i]);
            linkChild(nodeManager, server, rootFolder, targetQuantityNodes[i]);
        }

        UaVariableNode productCodeDetailNode = createStringNode(nodeContext, nsIndex,
                "LS_EXP2/workReport/detail/productcodeDetail", "productcodeDetail");
        UaVariableNode productNameDetailNode = createStringNode(nodeContext, nsIndex,
                "LS_EXP2/workReport/detail/productnameDetail", "productnameDetail");
        UaVariableNode customerDetailNode = createStringNode(nodeContext, nsIndex,
                "LS_EXP2/workReport/detail/customerDetail", "customerDetail");
        UaVariableNode processDetailNode = createStringNode(nodeContext, nsIndex,
                "LS_EXP2/workReport/detail/processDetail", "processDetail");
        UaVariableNode workDeadlineDetailNode = createStringNode(nodeContext, nsIndex,
                "LS_EXP2/workReport/detail/workdeadlineDetail", "workdeadlineDetail");
        UaVariableNode targetQuantityDetailNode = createInt16Node(nodeContext, nsIndex,
                "LS_EXP2/workReport/detail/targetQuantityDetail", "targetQuantityDetail");

        nodeManager.addNode(productCodeDetailNode);
        nodeManager.addNode(productNameDetailNode);
        nodeManager.addNode(customerDetailNode);
        nodeManager.addNode(processDetailNode);
        nodeManager.addNode(workDeadlineDetailNode);
        nodeManager.addNode(targetQuantityDetailNode);

        linkChild(nodeManager, server, rootFolder, productCodeDetailNode);
        linkChild(nodeManager, server, rootFolder, productNameDetailNode);
        linkChild(nodeManager, server, rootFolder, customerDetailNode);
        linkChild(nodeManager, server, rootFolder, processDetailNode);
        linkChild(nodeManager, server, rootFolder, workDeadlineDetailNode);
        linkChild(nodeManager, server, rootFolder, targetQuantityDetailNode);

        int initialEquipmentNo = 1;
        short initialPage = 1;
        short initialRow = 1;
        WorkItem[] initialWorkItems = getWorkItemsByEquipmentNo(initialEquipmentNo);
        applyWorkReportPage(initialWorkItems, initialPage,
                productCodeNodes, productNameNodes, customerNodes, processNodes, workDeadlineNodes, targetQuantityNodes);
        applyDetailByPageAndRow(initialWorkItems, initialPage, initialRow,
                productCodeDetailNode, productNameDetailNode, customerDetailNode,
                processDetailNode, workDeadlineDetailNode, targetQuantityDetailNode);

        var scheduler = Executors.newSingleThreadScheduledExecutor();
        final int[] lastEquipmentNo = {initialEquipmentNo};
        final short[] lastPageValue = {initialPage};
        final short[] lastRowValue = {initialRow};
        final boolean[] lastWorkStart = {false};
        final boolean[] lastWorkPause = {false};
        final short[] lastProductionCounter = {0};
        final short[] lastDefectCount = {0};
        final boolean[] lastWorkEnd = {false};

        scheduler.scheduleAtFixedRate(() -> {
            boolean heartbeat = Boolean.TRUE.equals(heartbeatNode.getValue().getValue().getValue());
            heartbeatNode.setValue(new DataValue(new Variant(!heartbeat)));

            short tempRaw = (short) (200 + (int) (Math.random() * 120));
            tempNode.setValue(new DataValue(new Variant(tempRaw)));

            Object equipmentRawValue = selectedEquipmentNode.getValue().getValue().getValue();
            int equipmentNo = normalizeEquipmentNo(equipmentRawValue);
            System.out.println("[MONITOR] selectedEquipment raw=" + equipmentRawValue + ", normalized=" + equipmentNo);
            short pageValue = normalizePage(readShortValue(currentPageNode.getValue().getValue().getValue()));
            short rowValue = normalizeRow(readShortValue(selectedRowNode.getValue().getValue().getValue()));

            WorkItem[] requestedWorkItems = getWorkItemsByEquipmentNo(equipmentNo);
            boolean equipmentChanged = false;
            if (requestedWorkItems != null && equipmentNo != lastEquipmentNo[0]) {
                lastEquipmentNo[0] = equipmentNo;
                selectedEquipmentNode.setValue(new DataValue(new Variant(equipmentNo)));
                equipmentChanged = true;
                System.out.println("[CLIENT->SERVER] selectedEquipment=" + equipmentNo + " applied");
            }
            if (requestedWorkItems == null && equipmentNo != lastEquipmentNo[0]) {
                System.out.println("[WARN] selectedEquipment invalid: " + equipmentNo + " (allowed: 1, 2, 1234567)");
            }

            boolean pageChanged = pageValue != lastPageValue[0];
            if (pageChanged) {
                lastPageValue[0] = pageValue;
                currentPageNode.setValue(new DataValue(new Variant(pageValue)));
                System.out.println("[CLIENT->SERVER] currentPage=" + pageValue + " applied (5 rows)");
            }

            boolean rowChanged = rowValue != lastRowValue[0];
            if (rowChanged) {
                lastRowValue[0] = rowValue;
                selectedRowNode.setValue(new DataValue(new Variant(rowValue)));
                System.out.println("[CLIENT->SERVER] selectedRow=" + rowValue + " applied");
            }

            WorkItem[] currentWorkItems = getWorkItemsByEquipmentNo(lastEquipmentNo[0]);
            if (currentWorkItems != null && (equipmentChanged || pageChanged)) {
                applyWorkReportPage(currentWorkItems, lastPageValue[0],
                        productCodeNodes, productNameNodes, customerNodes,
                        processNodes, workDeadlineNodes, targetQuantityNodes);
            }

            if (currentWorkItems != null && (equipmentChanged || pageChanged || rowChanged)) {
                applyDetailByPageAndRow(currentWorkItems, lastPageValue[0], lastRowValue[0],
                        productCodeDetailNode, productNameDetailNode, customerDetailNode,
                        processDetailNode, workDeadlineDetailNode, targetQuantityDetailNode);
            }

            boolean workStart = readBooleanValue(workStartNode.getValue().getValue().getValue());
            boolean workPause = readBooleanValue(workPauseNode.getValue().getValue().getValue());
            short productionCounter = readShortValue(productionCounterNode.getValue().getValue().getValue());
            short defectCount = readShortValue(defectCountNode.getValue().getValue().getValue());
            boolean workEnd = readBooleanValue(workEndNode.getValue().getValue().getValue());

            if (workStart != lastWorkStart[0]) {
                lastWorkStart[0] = workStart;
                workStartNode.setValue(new DataValue(new Variant(workStart)));
                System.out.println("[CLIENT->SERVER] workStart=" + workStart);
            }
            if (workPause != lastWorkPause[0]) {
                lastWorkPause[0] = workPause;
                workPauseNode.setValue(new DataValue(new Variant(workPause)));
                System.out.println("[CLIENT->SERVER] workPause=" + workPause);
            }
            if (productionCounter != lastProductionCounter[0]) {
                lastProductionCounter[0] = productionCounter;
                productionCounterNode.setValue(new DataValue(new Variant(productionCounter)));
                System.out.println("[CLIENT->SERVER] productionCounter=" + productionCounter);
            }
            if (defectCount != lastDefectCount[0]) {
                lastDefectCount[0] = defectCount;
                defectCountNode.setValue(new DataValue(new Variant(defectCount)));
                System.out.println("[CLIENT->SERVER] defectCount=" + defectCount);
            }
            if (workEnd != lastWorkEnd[0]) {
                lastWorkEnd[0] = workEnd;
                workEndNode.setValue(new DataValue(new Variant(workEnd)));
                System.out.println("[CLIENT->SERVER] workEnd=" + workEnd);
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

    private static UaVariableNode createInt32RwNode(UaNodeContext nodeContext, UShort nsIndex, String id, String browseName) {
        UaVariableNode node = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, id))
                .setBrowseName(new QualifiedName(nsIndex, browseName))
                .setDisplayName(LocalizedText.english(browseName))
                .setDataType(Identifiers.Int32)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        node.setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        node.setValue(new DataValue(new Variant(0)));
        return node;
    }

    private static UaVariableNode createInt16Node(UaNodeContext nodeContext, UShort nsIndex, String id, String browseName) {
        UaVariableNode node = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, id))
                .setBrowseName(new QualifiedName(nsIndex, browseName))
                .setDisplayName(LocalizedText.english(browseName))
                .setDataType(Identifiers.Int16)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setValue(new DataValue(new Variant((short) 0)));
        return node;
    }

    private static UaVariableNode createInt16RwNode(UaNodeContext nodeContext, UShort nsIndex, String id, String browseName) {
        UaVariableNode node = createInt16Node(nodeContext, nsIndex, id, browseName);
        node.setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        node.setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        return node;
    }

    private static UaVariableNode createBooleanRwNode(UaNodeContext nodeContext, UShort nsIndex, String id, String browseName) {
        UaVariableNode node = UaVariableNode.builder(nodeContext)
                .setNodeId(new NodeId(nsIndex, id))
                .setBrowseName(new QualifiedName(nsIndex, browseName))
                .setDisplayName(LocalizedText.english(browseName))
                .setDataType(Identifiers.Boolean)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build();
        node.setAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        node.setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_WRITE));
        node.setValue(new DataValue(new Variant(false)));
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

    private static void applyWorkReportPage(WorkItem[] workItems,
                                            short currentPage,
                                            UaVariableNode[] productCodeNodes,
                                            UaVariableNode[] productNameNodes,
                                            UaVariableNode[] customerNodes,
                                            UaVariableNode[] processNodes,
                                            UaVariableNode[] workDeadlineNodes,
                                            UaVariableNode[] targetQuantityNodes) {
        int offset = (currentPage - 1) * 5;
        for (int i = 0; i < 5; i++) {
            WorkItem item = workItems[offset + i];
            productCodeNodes[i].setValue(new DataValue(new Variant(item.productCode())));
            productNameNodes[i].setValue(new DataValue(new Variant(item.productName())));
            customerNodes[i].setValue(new DataValue(new Variant(item.customer())));
            processNodes[i].setValue(new DataValue(new Variant(item.process())));
            workDeadlineNodes[i].setValue(new DataValue(new Variant(item.workDeadline())));
            targetQuantityNodes[i].setValue(new DataValue(new Variant(item.targetQuantity())));
        }
    }

    private static void applyDetailByPageAndRow(WorkItem[] workItems,
                                                short currentPage,
                                                short selectedRow,
                                                UaVariableNode productCodeDetailNode,
                                                UaVariableNode productNameDetailNode,
                                                UaVariableNode customerDetailNode,
                                                UaVariableNode processDetailNode,
                                                UaVariableNode workDeadlineDetailNode,
                                                UaVariableNode targetQuantityDetailNode) {
        int itemIndex = (currentPage - 1) * 5 + (selectedRow - 1);
        WorkItem selectedItem = workItems[itemIndex];

        productCodeDetailNode.setValue(new DataValue(new Variant(selectedItem.productCode())));
        productNameDetailNode.setValue(new DataValue(new Variant(selectedItem.productName())));
        customerDetailNode.setValue(new DataValue(new Variant(selectedItem.customer())));
        processDetailNode.setValue(new DataValue(new Variant(selectedItem.process())));
        workDeadlineDetailNode.setValue(new DataValue(new Variant(selectedItem.workDeadline())));
        targetQuantityDetailNode.setValue(new DataValue(new Variant(selectedItem.targetQuantity())));
    }

    private static short normalizePage(short value) {
        if (value < 1) return 1;
        if (value > 3) return 3;
        return value;
    }

    private static short normalizeRow(short value) {
        if (value < 1) return 1;
        if (value > 5) return 5;
        return value;
    }

    private static int normalizeEquipmentNo(Object value) {
        if (value instanceof Number number) {
            int v = number.intValue();
            if (v == 1 || v == 2 || v == 1234567) return v;
            return -1;
        }
        try {
            String text = String.valueOf(value).trim();
            if (text.isEmpty()) return -1;
            int parsed = Integer.parseInt(text);
            if (parsed == 1 || parsed == 2 || parsed == 1234567) return parsed;
            return -1;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static WorkItem[] getWorkItemsByEquipmentNo(int equipmentNo) {
        return switch (equipmentNo) {
            case 1234567 -> EQUIPMENT_2_WORK_ITEMS;
            case 2 -> EQUIPMENT_3_WORK_ITEMS;
            case 1 -> EQUIPMENT_1_WORK_ITEMS;
            default -> null;
        };
    }

    private static short readShortValue(Object value) {
        if (value instanceof Number number) {
            return number.shortValue();
        }
        try {
            return Short.parseShort(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static boolean readBooleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
