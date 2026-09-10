package com.dieselsoft.controller_h0_5.features.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.dieselsoft.controller_h0_5.R
import com.dieselsoft.controller_h0_5.features.viewmodel.BluetoothViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerApp(viewModel: BluetoothViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("home") }

    val isConnected by viewModel.isConnected.collectAsState()
    val connectedDeviceName by viewModel.connectedDeviceName.collectAsState()
    val lastDeviceName by viewModel.lastDeviceName.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo_empresa3),
                            contentDescription = "Logo de la empresa",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentScale = ContentScale.Fit
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    actions = {
                        // Spacer para equilibrar el icono de navegación y que el logo quede centrado
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            // NUEVO: Barra de conexión Bluetooth abajo del TopBar
//            contentWindowInsets = WindowInsets.None
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {

                // ─── Strip de conexión Bluetooth ───
                BluetoothConnectionStrip(
                    isConnected = isConnected,
                    connectedDeviceName = connectedDeviceName,
                    lastDeviceName = lastDeviceName,
                    onDisconnect = { viewModel.disconnect() },
                    onReconnect = {
                        if (!viewModel.reconnectLastDevice()) {
                            // Si no hay dispositivo previo, navegar a Devices
                            currentScreen = "devices"
                        }
                    },
                    onGoToDevices = { currentScreen = "devices" }
                )

                // ─── Contenido de la pantalla ───
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        "home" -> SpeedometerScreen(viewModel)
                        "devices" -> DevicesScreen(
                            viewModel = viewModel,
                            onDeviceConnected = {
                                currentScreen = "home"
                            }
                        )
                        "calibration" -> CalibrationScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun BluetoothConnectionStrip(
    isConnected: Boolean,
    connectedDeviceName: String?,
    lastDeviceName: String?,
    onDisconnect: () -> Unit,
    onReconnect: () -> Unit,
    onGoToDevices: () -> Unit
) {
    val bgColor = if (isConnected) Color(0xFF1B5E20) else Color(0xFF212121)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot de estado
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(
                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFF616161),
                radius = size.width / 2f
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Icono bluetooth
        Icon(
            painter = painterResource(
                id = if (isConnected)
                    R.drawable.outline_bluetooth_connected_24
                else
                    R.drawable.outline_bluetooth_24
            ),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Texto de estado
        Text(
            text = if (isConnected) {
                connectedDeviceName ?: "Conectado"
            } else {
                if (lastDeviceName != null) "Desconectado · $lastDeviceName"
                else "Sin dispositivo"
            },
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )

        // Botón de acción
        if (isConnected) {
            // Conectado → mostrar botón de desconectar
            IconButton(
                onClick = onDisconnect,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_bluetooth_disabled_24),
                    contentDescription = "Desconectar",
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // Desconectado → mostrar botón de reconectar o ir a devices
            IconButton(
                onClick = {
                    if (lastDeviceName != null) onReconnect()
                    else onGoToDevices()
                },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color(0xFF81C784)
                )
            ) {
                Icon(
                    imageVector = if (lastDeviceName != null) Icons.Default.Refresh
                    else Icons.Default.Add,
                    contentDescription = if (lastDeviceName != null) "Reconectar" else "Agregar dispositivo",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_devices_24),
                contentDescription = "Speed Sensor",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Speed Sensor",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentScreen == "home",
            onClick = { onScreenSelected("home") }
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.outline_bluetooth_24),
                    contentDescription = null
                )
            },
            label = { Text("Devices") },
            selected = currentScreen == "devices",
            onClick = { onScreenSelected("devices") }
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
            },
            label = { Text("Calibrar") },
            selected = currentScreen == "calibration",
            onClick = { onScreenSelected("calibration") }
        )
    }
}