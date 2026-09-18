package com.example.ui.screens.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.CalculadorasRepository
import com.example.domain.calculators.ConstructionCalculators
import com.example.ui.components.Obra360TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CalculadorasScreen(
    userId: String,
    calculadorasRepository: CalculadorasRepository,
    onBack: () -> Unit
) {
    val calculators = listOf(
        "Concreto",
        "Área",
        "Volume",
        "Cimento",
        "Areia",
        "Brita",
        "Argamassa",
        "Blocos",
        "Tijolos",
        "Reboco",
        "Pintura",
        "Piso",
        "Aço",
        "Escavação",
        "Conversor"
    )
    var selectedCalc by remember { mutableStateOf("Concreto") }
    val scope = rememberCoroutineScope()
    var snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Obra360TopBar(
                title = "Calculadoras de Obra",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(calculators) { calcName ->
                    FilterChip(
                        selected = (selectedCalc == calcName),
                        onClick = { selectedCalc = calcName },
                        label = { Text(calcName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate900,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Technical Disclaimer
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Slate700,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = ConstructionCalculators.DISCLAIMER,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                item {
                    when (selectedCalc) {
                        "Concreto" -> ConcretoView(
                            onSave = { details, res ->
                                scope.launch {
                                    calculadorasRepository.salvarCalculo(userId, "Concreto Estrutural", "Concreto", details, res)
                                    snackbarHostState.showSnackbar("Cálculo salvo no histórico!")
                                }
                            }
                        )
                        "Área" -> AreaView()
                        "Volume" -> VolumeView()
                        "Cimento" -> CimentoView()
                        "Areia" -> AreiaView()
                        "Brita" -> BritaView()
                        "Argamassa" -> ArgamassaView()
                        "Blocos" -> BlocosView()
                        "Tijolos" -> TijolosView()
                        "Reboco" -> RebocoView()
                        "Pintura" -> PinturaView()
                        "Piso" -> PisoView()
                        "Aço" -> AcoView()
                        "Escavação" -> EscavacaoView()
                        "Conversor" -> ConversorView()
                    }
                }
            }
        }
    }
}

@Composable
fun ConcretoView(onSave: (String, String) -> Unit) {
    var volumeStr by remember { mutableStateOf("5.0") }
    var traco by remember { mutableStateOf("1:2:3 (Estrutural)") }
    var perdaStr by remember { mutableStateOf("10.0") }

    val volume = volumeStr.toDoubleOrNull() ?: 0.0
    val perda = perdaStr.toDoubleOrNull() ?: 10.0
    val resultado = remember(volume, traco, perda) {
        ConstructionCalculators.calcularConcreto(volume, traco, perda)
    }

    CalcCard(title = "Cálculo de Concreto e Insumos") {
        OutlinedTextField(
            value = volumeStr,
            onValueChange = { volumeStr = it },
            label = { Text("Volume necessário (m³)") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = traco.startsWith("1:2:3"),
                onClick = { traco = "1:2:3 (Estrutural)" },
                label = { Text("Traço 1:2:3 (Fck ~25MPa)") }
            )
            FilterChip(
                selected = traco.startsWith("1:2.5"),
                onClick = { traco = "1:2.5:3.5 (Convencional)" },
                label = { Text("Traço 1:2.5:3.5") }
            )
        }
        OutlinedTextField(
            value = perdaStr,
            onValueChange = { perdaStr = it },
            label = { Text("Margem de Perda (%)") },
            modifier = Modifier.fillMaxWidth()
        )

        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))

        Text(text = "Resultados Técnicos:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        ResultRow(label = "Volume com perda", value = "${resultado.volumeM3} m³")
        ResultRow(label = "Cimento (sacos 50kg)", value = "${resultado.sacosCimento50kg} sacos")
        ResultRow(label = "Areia Média", value = "${resultado.areiaM3} m³")
        ResultRow(label = "Brita 1", value = "${resultado.britaM3} m³")
        ResultRow(label = "Água", value = "${resultado.aguaLitros} Litros")

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                onSave("Volume: ${resultado.volumeM3}m³ | Traço: $traco", "${resultado.sacosCimento50kg} sacos cimento, ${resultado.areiaM3}m³ areia, ${resultado.britaM3}m³ brita")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Cálculo")
        }
    }
}

@Composable
fun AreaView() {
    var forma by remember { mutableStateOf("Retangular") }
    var val1 by remember { mutableStateOf("10.0") }
    var val2 by remember { mutableStateOf("5.0") }
    var val3 by remember { mutableStateOf("0.0") }

    val v1 = val1.toDoubleOrNull() ?: 0.0
    val v2 = val2.toDoubleOrNull() ?: 0.0
    val v3 = val3.toDoubleOrNull() ?: 0.0

    val area = remember(forma, v1, v2, v3) {
        ConstructionCalculators.calcularArea(ConstructionCalculators.AreaInput(forma, v1, v2, v3))
    }

    CalcCard(title = "Cálculo de Área Geométrica") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Retangular", "Triangular", "Circular", "Trapezoidal").forEach { f ->
                FilterChip(
                    selected = (forma == f),
                    onClick = { forma = f },
                    label = { Text(f, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        OutlinedTextField(
            value = val1,
            onValueChange = { val1 = it },
            label = { Text(if (forma == "Circular") "Raio (m)" else "Base / Largura (m)") },
            modifier = Modifier.fillMaxWidth()
        )
        if (forma != "Circular") {
            OutlinedTextField(
                value = val2,
                onValueChange = { val2 = it },
                label = { Text("Comprimento / Altura (m)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (forma == "Trapezoidal") {
            OutlinedTextField(
                value = val3,
                onValueChange = { val3 = it },
                label = { Text("Altura do Trapézio (m)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Área Total Calculada", value = "%.2f m²".format(area))
    }
}

@Composable
fun VolumeView() {
    var comp by remember { mutableStateOf("8.0") }
    var larg by remember { mutableStateOf("0.20") }
    var alt by remember { mutableStateOf("0.40") }

    val c = comp.toDoubleOrNull() ?: 0.0
    val l = larg.toDoubleOrNull() ?: 0.0
    val a = alt.toDoubleOrNull() ?: 0.0
    val vol = remember(c, l, a) {
        ConstructionCalculators.calcularVolume(ConstructionCalculators.VolumeInput("Prismático", c, l, a))
    }

    CalcCard(title = "Cálculo de Volume Prismático") {
        OutlinedTextField(value = comp, onValueChange = { comp = it }, label = { Text("Comprimento (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = larg, onValueChange = { larg = it }, label = { Text("Largura (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = alt, onValueChange = { alt = it }, label = { Text("Altura / Espessura (m)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Volume Total", value = "%.3f m³".format(vol))
        ResultRow(label = "Em Litros", value = "%.0f Litros".format(vol * 1000.0))
    }
}

@Composable
fun CimentoView() {
    var area by remember { mutableStateOf("50.0") }
    var tipo by remember { mutableStateOf("Contrapiso (5cm)") }
    val a = area.toDoubleOrNull() ?: 0.0
    val res = remember(a, tipo) { ConstructionCalculators.calcularCimento(a, tipo) }

    CalcCard(title = "Consumo de Cimento") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área de aplicação (m²)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Contrapiso (5cm)", "Reboco / Emboço (2cm)", "Alvenaria (por m²)").forEach { t ->
                FilterChip(selected = (tipo == t), onClick = { tipo = t }, label = { Text(t, style = MaterialTheme.typography.labelSmall) })
            }
        }
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Sacos necessários (50kg)", value = "${res.sacos50kg} sacos")
        ResultRow(label = "Massa total estimada", value = "${res.pesoTotalKg} kg")
    }
}

@Composable
fun AreiaView() {
    var vol by remember { mutableStateOf("3.0") }
    val v = vol.toDoubleOrNull() ?: 0.0
    val res = remember(v) { ConstructionCalculators.calcularAreia(v) }

    CalcCard(title = "Cálculo de Areia para Obras") {
        OutlinedTextField(value = vol, onValueChange = { vol = it }, label = { Text("Volume líquido (m³)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Volume com 10% perda", value = "${res.volumeM3} m³")
        ResultRow(label = "Peso estimado", value = "${res.pesoToneladas} Toneladas")
        ResultRow(label = "Quantidade de Latas 18L", value = "${res.latas18L} latas")
    }
}

@Composable
fun BritaView() {
    var vol by remember { mutableStateOf("4.0") }
    val v = vol.toDoubleOrNull() ?: 0.0
    val res = remember(v) { ConstructionCalculators.calcularBrita(v) }

    CalcCard(title = "Cálculo de Brita / Agregado Graúdo") {
        OutlinedTextField(value = vol, onValueChange = { vol = it }, label = { Text("Volume líquido (m³)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Volume com 10% perda", value = "${res.volumeM3} m³")
        ResultRow(label = "Peso estimado", value = "${res.pesoToneladas} Toneladas")
        ResultRow(label = "Quantidade de Latas 18L", value = "${res.latas18L} latas")
    }
}

@Composable
fun ArgamassaView() {
    var area by remember { mutableStateOf("45.0") }
    var tipo by remember { mutableStateOf("AC-II (Piso cerâmico comum)") }
    val a = area.toDoubleOrNull() ?: 0.0
    val res = remember(a, tipo) { ConstructionCalculators.calcularArgamassa(a, tipo) }

    CalcCard(title = "Cálculo de Argamassa Colante") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área de assentamento (m²)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("AC-I (Interior)", "AC-II (Piso cerâmico comum)", "AC-III (Porcelanatos grandes)").forEach { t ->
                FilterChip(selected = (tipo == t), onClick = { tipo = t }, label = { Text(t, style = MaterialTheme.typography.labelSmall) })
            }
        }
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Sacos necessários (20kg)", value = "${res.sacos20kg} sacos")
        ResultRow(label = "Peso total com perda", value = "${res.pesoTotalKg} kg")
    }
}

@Composable
fun BlocosView() {
    var area by remember { mutableStateOf("30.0") }
    val a = area.toDoubleOrNull() ?: 0.0
    val res = remember(a) { ConstructionCalculators.calcularBlocos(a) }

    CalcCard(title = "Blocos de Concreto Estrutural") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área de alvenaria (m²)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Blocos (14x19x39 com perda)", value = "${res.quantidadeBlocos} unidades")
        ResultRow(label = "Argamassa de assentamento", value = "${res.argamassaAssentamentoKg} kg")
    }
}

@Composable
fun TijolosView() {
    var area by remember { mutableStateOf("25.0") }
    var tipo by remember { mutableStateOf("6 Furos (9x14x19 cm)") }
    val a = area.toDoubleOrNull() ?: 0.0
    val res = remember(a, tipo) { ConstructionCalculators.calcularTijolos(a, tipo) }

    CalcCard(title = "Tijolos Cerâmicos / Alvenaria de Vedação") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área da parede (m²)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("6 Furos (9x14x19 cm)", "8 Furos (9x19x19 cm)", "Tijolo Maciço").forEach { t ->
                FilterChip(selected = (tipo == t), onClick = { tipo = t }, label = { Text(t, style = MaterialTheme.typography.labelSmall) })
            }
        }
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Tijolos necessários", value = "${res.quantidadeTijolos} unidades")
        ResultRow(label = "Cimento para argamassa", value = "${res.cimentoAssentamentoSacos} sacos")
        ResultRow(label = "Areia", value = "${res.areiaM3} m³")
    }
}

@Composable
fun RebocoView() {
    var area by remember { mutableStateOf("60.0") }
    var esp by remember { mutableStateOf("2.0") }
    val a = area.toDoubleOrNull() ?: 0.0
    val e = esp.toDoubleOrNull() ?: 2.0
    val res = remember(a, e) { ConstructionCalculators.calcularReboco(a, e) }

    CalcCard(title = "Reboco / Emboço de Paredes") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área da parede (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = esp, onValueChange = { esp = it }, label = { Text("Espessura média (cm)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Cimento (sacos 50kg)", value = "${res.sacosCimento50kg} sacos")
        ResultRow(label = "Cal hidratada (sacos 20kg)", value = "${res.sacosCal20kg} sacos")
        ResultRow(label = "Areia média", value = "${res.areiaMediaM3} m³")
    }
}

@Composable
fun PinturaView() {
    var area by remember { mutableStateOf("120.0") }
    var demaos by remember { mutableStateOf("2") }
    val a = area.toDoubleOrNull() ?: 0.0
    val d = demaos.toIntOrNull() ?: 2
    val res = remember(a, d) { ConstructionCalculators.calcularPintura(a, d) }

    CalcCard(title = "Consumo de Tinta e Demãos") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área total a pintar (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = demaos, onValueChange = { demaos = it }, label = { Text("Número de demãos") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Volume total de tinta", value = "${res.litrosTotais} Litros")
        ResultRow(label = "Latas grandes (18 Litros)", value = "${res.latas18Litros} latas")
        ResultRow(label = "Galões complementares (3.6L)", value = "${res.galoes3p6Litros} galões")
    }
}

@Composable
fun PisoView() {
    var area by remember { mutableStateOf("70.0") }
    var larg by remember { mutableStateOf("60.0") }
    var comp by remember { mutableStateOf("60.0") }
    val a = area.toDoubleOrNull() ?: 0.0
    val l = larg.toDoubleOrNull() ?: 60.0
    val c = comp.toDoubleOrNull() ?: 60.0
    val res = remember(a, l, c) { ConstructionCalculators.calcularPiso(a, l, c) }

    CalcCard(title = "Piso & Revestimento Cerâmico") {
        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Área do ambiente (m²)") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = larg, onValueChange = { larg = it }, label = { Text("Largura peça (cm)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = comp, onValueChange = { comp = it }, label = { Text("Comprimento peça (cm)") }, modifier = Modifier.weight(1f))
        }
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Área com perda de recorte", value = "${res.areaRealComPerdaM2} m²")
        ResultRow(label = "Caixas estimadas (1.8m²/cx)", value = "${res.caixasNecessarias} caixas")
        ResultRow(label = "Total de peças", value = "${res.pecasTotais} peças")
    }
}

@Composable
fun AcoView() {
    var diametro by remember { mutableStateOf("10.0") } // 10mm (3/8")
    var metros by remember { mutableStateOf("150.0") }
    val d = diametro.toDoubleOrNull() ?: 10.0
    val m = metros.toDoubleOrNull() ?: 0.0
    val res = remember(d, m) { ConstructionCalculators.calcularAco(d, m) }

    CalcCard(title = "Aço CA-50 / CA-60 (ABNT NBR 7480)") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("6.3", "8.0", "10.0", "12.5", "16.0").forEach { bitola ->
                FilterChip(selected = (diametro == bitola), onClick = { diametro = bitola }, label = { Text("${bitola}mm") })
            }
        }
        OutlinedTextField(value = metros, onValueChange = { metros = it }, label = { Text("Comprimento total do projeto (m)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Peso nominal por metro linear", value = "${res.pesoPorMetroKg} kg/m")
        ResultRow(label = "Peso Total de Aço", value = "${res.pesoTotalKg} kg")
        ResultRow(label = "Barras de 12 metros", value = "${res.barras12m} barras")
    }
}

@Composable
fun EscavacaoView() {
    var comp by remember { mutableStateOf("12.0") }
    var larg by remember { mutableStateOf("6.0") }
    var prof by remember { mutableStateOf("1.5") }
    val c = comp.toDoubleOrNull() ?: 0.0
    val l = larg.toDoubleOrNull() ?: 0.0
    val p = prof.toDoubleOrNull() ?: 0.0
    val res = remember(c, l, p) { ConstructionCalculators.calcularEscavacao(c, l, p) }

    CalcCard(title = "Movimentação de Terra & Escavação") {
        OutlinedTextField(value = comp, onValueChange = { comp = it }, label = { Text("Comprimento (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = larg, onValueChange = { larg = it }, label = { Text("Largura (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = prof, onValueChange = { prof = it }, label = { Text("Profundidade média (m)") }, modifier = Modifier.fillMaxWidth())
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Volume no corte (m³)", value = "${res.volumeCorteM3} m³")
        ResultRow(label = "Volume solto (25% empolamento)", value = "${res.volumeSoltoM3} m³")
        ResultRow(label = "Viagens caçamba 12m³", value = "${res.viagensCaminhao12m3} caminhões")
    }
}

@Composable
fun ConversorView() {
    var tipo by remember { mutableStateOf("Comprimento") }
    var valorStr by remember { mutableStateOf("10.0") }
    var de by remember { mutableStateOf("m") }
    var para by remember { mutableStateOf("cm") }

    val valor = valorStr.toDoubleOrNull() ?: 0.0
    val convertido = remember(valor, tipo, de, para) {
        ConstructionCalculators.converterUnidades(valor, tipo, de, para)
    }

    CalcCard(title = "Conversor de Unidades da Construção") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Comprimento", "Área", "Volume", "Massa").forEach { t ->
                FilterChip(
                    selected = (tipo == t),
                    onClick = {
                        tipo = t
                        when (t) {
                            "Comprimento" -> { de = "m"; para = "cm" }
                            "Área" -> { de = "m²"; para = "Hectare (ha)" }
                            "Volume" -> { de = "m³"; para = "Litros (L)" }
                            "Massa" -> { de = "kg"; para = "Toneladas (t)" }
                        }
                    },
                    label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        OutlinedTextField(value = valorStr, onValueChange = { valorStr = it }, label = { Text("Valor a converter") }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("De: $de", style = MaterialTheme.typography.labelMedium)
            Text("Para: $para", style = MaterialTheme.typography.labelMedium)
        }
        Divider(color = Slate200, modifier = Modifier.padding(vertical = 8.dp))
        ResultRow(label = "Resultado Convertido", value = "%.4f $para".format(convertido))
    }
}

@Composable
fun CalcCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Slate600)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Slate900)
    }
}
