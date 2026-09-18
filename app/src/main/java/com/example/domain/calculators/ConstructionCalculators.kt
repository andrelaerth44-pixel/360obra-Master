package com.example.domain.calculators

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Calculadoras matemáticas reais para Construção Civil do OBRA360.
 * Todas as fórmulas seguem normas técnicas brasileiras (ABNT) e literatura de engenharia civil.
 */
object ConstructionCalculators {

    const val DISCLAIMER = "Estimativa para referência técnica de planejamento de materiais. Não substitui o projeto executivo e dimensionamento estrutural por engenheiro responsável."

    // 1. Calculadora de Área
    data class AreaInput(
        val forma: String = "Retangular", // Retangular, Triangular, Circular, Trapezoidal
        val valor1: Double = 0.0, // largura / base / raio / base maior
        val valor2: Double = 0.0, // comprimento / altura / base menor
        val valor3: Double = 0.0  // altura trapezio
    )
    fun calcularArea(input: AreaInput): Double {
        return when (input.forma) {
            "Retangular" -> input.valor1 * input.valor2
            "Triangular" -> (input.valor1 * input.valor2) / 2.0
            "Circular" -> PI * input.valor1 * input.valor1
            "Trapezoidal" -> ((input.valor1 + input.valor2) * input.valor3) / 2.0
            else -> input.valor1 * input.valor2
        }
    }

    // 2. Calculadora de Volume
    data class VolumeInput(
        val tipo: String = "Prismático", // Prismático (Laje/Viga/Sapata), Cilíndrico (Estaca/Pilar)
        val dimensao1: Double = 0.0, // Comprimento ou Raio
        val dimensao2: Double = 0.0, // Largura ou Altura
        val dimensao3: Double = 0.0  // Altura/Espessura
    )
    fun calcularVolume(input: VolumeInput): Double {
        return when (input.tipo) {
            "Prismático" -> input.dimensao1 * input.dimensao2 * input.dimensao3
            "Cilíndrico" -> PI * input.dimensao1 * input.dimensao1 * input.dimensao2
            else -> input.dimensao1 * input.dimensao2 * input.dimensao3
        }
    }

    // 3. Calculadora de Concreto (Traço 1:2:3 ou 1:2.5:3.5)
    data class ConcretoResult(
        val volumeM3: Double,
        val sacosCimento50kg: Double,
        val areiaM3: Double,
        val britaM3: Double,
        val aguaLitros: Double
    )
    fun calcularConcreto(volumeM3: Double, traco: String = "1:2:3 (Estrutural)", margemPerdaPct: Double = 10.0): ConcretoResult {
        val volumeTotal = volumeM3 * (1.0 + margemPerdaPct / 100.0)
        // Traço 1:2:3 consome aprox 7.0 sacos de cimento (50kg) por m³
        // Areia: ~0.55 m³ por m³ de concreto; Brita: ~0.83 m³ por m³ de concreto; Água: ~180 litros por m³
        val sacosPorM3 = if (traco.startsWith("1:2:3")) 7.0 else 5.8
        val areiaPorM3 = if (traco.startsWith("1:2:3")) 0.55 else 0.62
        val britaPorM3 = if (traco.startsWith("1:2:3")) 0.83 else 0.86
        val aguaPorM3 = 185.0

        return ConcretoResult(
            volumeM3 = round2(volumeTotal),
            sacosCimento50kg = round2(volumeTotal * sacosPorM3),
            areiaM3 = round2(volumeTotal * areiaPorM3),
            britaM3 = round2(volumeTotal * britaPorM3),
            aguaLitros = round2(volumeTotal * aguaPorM3)
        )
    }

    // 4. Calculadora de Cimento
    data class CimentoResult(
        val sacos50kg: Int,
        val pesoTotalKg: Double
    )
    fun calcularCimento(areaOuVolume: Double, aplicacao: String = "Contrapiso"): CimentoResult {
        val sacos = when (aplicacao) {
            "Contrapiso (5cm)" -> ceil(areaOuVolume * 0.05 * 6.5).toInt()
            "Reboco / Emboço (2cm)" -> ceil(areaOuVolume * 0.02 * 6.0).toInt()
            "Alvenaria (por m²)" -> ceil(areaOuVolume * 0.15).toInt()
            else -> ceil(areaOuVolume * 5.0).toInt()
        }
        return CimentoResult(sacos50kg = maxOf(1, sacos), pesoTotalKg = sacos * 50.0)
    }

    // 5. Calculadora de Areia
    data class AreiaResult(
        val volumeM3: Double,
        val pesoToneladas: Double,
        val latas18L: Int
    )
    fun calcularAreia(volumeM3: Double, margemPerdaPct: Double = 10.0): AreiaResult {
        val totalM3 = volumeM3 * (1.0 + margemPerdaPct / 100.0)
        // Densidade média da areia de construção: ~1.500 kg/m³ = 1.5 t/m³
        val pesoTon = totalM3 * 1.5
        val latas = ceil((totalM3 * 1000.0) / 18.0).toInt()
        return AreiaResult(round2(totalM3), round2(pesoTon), latas)
    }

    // 6. Calculadora de Brita
    data class BritaResult(
        val volumeM3: Double,
        val pesoToneladas: Double,
        val latas18L: Int
    )
    fun calcularBrita(volumeM3: Double, margemPerdaPct: Double = 10.0): BritaResult {
        val totalM3 = volumeM3 * (1.0 + margemPerdaPct / 100.0)
        // Densidade média da brita 1: ~1.400 kg/m³ = 1.4 t/m³
        val pesoTon = totalM3 * 1.4
        val latas = ceil((totalM3 * 1000.0) / 18.0).toInt()
        return BritaResult(round2(totalM3), round2(pesoTon), latas)
    }

    // 7. Calculadora de Argamassa
    data class ArgamassaResult(
        val sacos20kg: Int,
        val pesoTotalKg: Double
    )
    fun calcularArgamassa(areaM2: Double, tipo: String = "AC-II (Piso cerâmico comum)"): ArgamassaResult {
        // Consumo médio: 5.0 kg/m² para peças até 30x30, 8.5 kg/m² para peças grandes com colagem dupla
        val kgPorM2 = when (tipo) {
            "AC-I (Interior)" -> 4.5
            "AC-II (Piso cerâmico comum)" -> 6.0
            "AC-III (Porcelanatos grandes)" -> 8.5
            else -> 6.0
        }
        val totalKg = areaM2 * kgPorM2 * 1.10 // 10% perda
        val sacos = ceil(totalKg / 20.0).toInt()
        return ArgamassaResult(sacos20kg = maxOf(1, sacos), pesoTotalKg = round2(totalKg))
    }

    // 8. Calculadora de Blocos de Concreto
    data class BlocosResult(
        val quantidadeBlocos: Int,
        val argamassaAssentamentoKg: Double
    )
    fun calcularBlocos(areaParedeM2: Double, dimensao: String = "14x19x39 cm", perdaPct: Double = 10.0): BlocosResult {
        // Dimensão nominal 14x19x39 com junta de 1cm: 20cm x 40cm = 0.08 m² por bloco -> 12.5 blocos/m²
        val blocosPorM2 = if (dimensao.contains("39")) 12.5 else 16.0
        val total = ceil(areaParedeM2 * blocosPorM2 * (1.0 + perdaPct / 100.0)).toInt()
        val argamassa = round2(total * 2.2) // ~2.2 kg de argamassa por bloco
        return BlocosResult(total, argamassa)
    }

    // 9. Calculadora de Tijolos Cerâmicos
    data class TijolosResult(
        val quantidadeTijolos: Int,
        val cimentoAssentamentoSacos: Double,
        val areiaM3: Double
    )
    fun calcularTijolos(areaParedeM2: Double, tipoTijolo: String = "6 Furos (9x14x19 cm)", perdaPct: Double = 10.0): TijolosResult {
        // 6 furos (9x14x19): ~35 tijolos/m² (espelho) ou ~48/m² (deitado)
        // 8 furos (9x19x19): ~25 tijolos/m²
        val tijolosPorM2 = when {
            tipoTijolo.contains("8 Furos") -> 26.0
            tipoTijolo.contains("Tijolo Maciço") -> 75.0
            else -> 38.0 // 6 furos padrão
        }
        val total = ceil(areaParedeM2 * tijolosPorM2 * (1.0 + perdaPct / 100.0)).toInt()
        val sacosCimento = round2(areaParedeM2 * 0.18)
        val areia = round2(areaParedeM2 * 0.03)
        return TijolosResult(total, sacosCimento, areia)
    }

    // 10. Calculadora de Reboco
    data class RebocoResult(
        val sacosCimento50kg: Int,
        val sacosCal20kg: Int,
        val areiaMediaM3: Double
    )
    fun calcularReboco(areaParedeM2: Double, espessuraCm: Double = 2.0, perdaPct: Double = 10.0): RebocoResult {
        val volumeM3 = (areaParedeM2 * (espessuraCm / 100.0)) * (1.0 + perdaPct / 100.0)
        // Traço 1:2:8 (Cimento : Cal : Areia)
        val sacosCimento = ceil(volumeM3 * 4.5).toInt()
        val sacosCal = ceil(volumeM3 * 8.0).toInt()
        val areiaM3 = round2(volumeM3 * 1.05)
        return RebocoResult(maxOf(1, sacosCimento), maxOf(1, sacosCal), areiaM3)
    }

    // 11. Calculadora de Pintura
    data class PinturaResult(
        val litrosTotais: Double,
        val latas18Litros: Int,
        val galoes3p6Litros: Int
    )
    fun calcularPintura(areaM2: Double, demãos: Int = 2, rendimentoM2PorLitro: Double = 10.0): PinturaResult {
        val litrosNecessarios = (areaM2 * demãos) / rendimentoM2PorLitro
        val latas18 = (litrosNecessarios / 18.0).toInt()
        val restoLitros = litrosNecessarios % 18.0
        val galoes36 = ceil(restoLitros / 3.6).toInt()
        return PinturaResult(round2(litrosNecessarios), latas18, galoes36)
    }

    // 12. Calculadora de Piso / Revestimento
    data class PisoResult(
        val areaRealComPerdaM2: Double,
        val caixasNecessarias: Int,
        val pecasTotais: Int
    )
    fun calcularPiso(
        areaM2: Double,
        larguraPecaCm: Double = 60.0,
        comprimentoPecaCm: Double = 60.0,
        m2PorCaixa: Double = 1.80,
        perdaRecortePct: Double = 12.0
    ): PisoResult {
        val areaTotal = areaM2 * (1.0 + perdaRecortePct / 100.0)
        val areaPecaM2 = (larguraPecaCm / 100.0) * (comprimentoPecaCm / 100.0)
        val totalPecas = ceil(areaTotal / areaPecaM2).toInt()
        val caixas = ceil(areaTotal / m2PorCaixa).toInt()
        return PisoResult(round2(areaTotal), maxOf(1, caixas), totalPecas)
    }

    // 13. Calculadora de Aço (Norma ABNT NBR 7480: kg/m = d(mm)² / 162)
    data class AcoResult(
        val pesoPorMetroKg: Double,
        val pesoTotalKg: Double,
        val barras12m: Int
    )
    fun calcularAco(diametroMm: Double, comprimentoTotalMetros: Double): AcoResult {
        val kgPorMetro = (diametroMm * diametroMm) / 162.0
        val pesoTotal = kgPorMetro * comprimentoTotalMetros
        val barras = ceil(comprimentoTotalMetros / 12.0).toInt()
        return AcoResult(round2(kgPorMetro), round2(pesoTotal), maxOf(1, barras))
    }

    // 14. Calculadora de Escavação
    data class EscavacaoResult(
        val volumeCorteM3: Double,
        val volumeSoltoM3: Double, // com empolamento
        val viagensCaminhao12m3: Int
    )
    fun calcularEscavacao(
        comprimentoM: Double,
        larguraM: Double,
        profundidadeM: Double,
        taxaEmpolamentoPct: Double = 25.0
    ): EscavacaoResult {
        val corte = comprimentoM * larguraM * profundidadeM
        val solto = corte * (1.0 + taxaEmpolamentoPct / 100.0)
        val viagens = ceil(solto / 12.0).toInt() // caçamba basculante 12 m³
        return EscavacaoResult(round2(corte), round2(solto), maxOf(1, viagens))
    }

    // 15. Conversor de Unidades
    fun converterUnidades(valor: Double, tipo: String, de: String, para: String): Double {
        return when (tipo) {
            "Comprimento" -> {
                val emMetros = when (de) {
                    "m" -> valor
                    "cm" -> valor / 100.0
                    "mm" -> valor / 1000.0
                    "pol (in)" -> valor * 0.0254
                    else -> valor
                }
                when (para) {
                    "m" -> emMetros
                    "cm" -> emMetros * 100.0
                    "mm" -> emMetros * 1000.0
                    "pol (in)" -> emMetros / 0.0254
                    else -> emMetros
                }
            }
            "Área" -> {
                val emM2 = when (de) {
                    "m²" -> valor
                    "cm²" -> valor / 10000.0
                    "Hectare (ha)" -> valor * 10000.0
                    "Alqueire SP" -> valor * 24200.0
                    else -> valor
                }
                when (para) {
                    "m²" -> emM2
                    "cm²" -> emM2 * 10000.0
                    "Hectare (ha)" -> emM2 / 10000.0
                    "Alqueire SP" -> emM2 / 24200.0
                    else -> emM2
                }
            }
            "Volume" -> {
                val emM3 = when (de) {
                    "m³" -> valor
                    "Litros (L)" -> valor / 1000.0
                    else -> valor
                }
                when (para) {
                    "m³" -> emM3
                    "Litros (L)" -> emM3 * 1000.0
                    else -> emM3
                }
            }
            "Massa" -> {
                val emKg = when (de) {
                    "kg" -> valor
                    "Toneladas (t)" -> valor * 1000.0
                    "Gramas (g)" -> valor / 1000.0
                    else -> valor
                }
                when (para) {
                    "kg" -> emKg
                    "Toneladas (t)" -> emKg / 1000.0
                    "Gramas (g)" -> emKg * 1000.0
                    else -> emKg
                }
            }
            else -> valor
        }
    }

    private fun round2(v: Double): Double = (v * 100.0).roundToInt() / 100.0
}
