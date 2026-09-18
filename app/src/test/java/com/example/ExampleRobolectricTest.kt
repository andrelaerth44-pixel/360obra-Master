package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OBRA360", appName)
  }

  @Test
  fun `test concrete and steel calculators`() {
    val concreto = com.example.domain.calculators.ConstructionCalculators.calcularConcreto(10.0)
    org.junit.Assert.assertTrue(concreto.sacosCimento50kg > 0)
    org.junit.Assert.assertTrue(concreto.areiaM3 > 0)
    org.junit.Assert.assertTrue(concreto.britaM3 > 0)

    val aco = com.example.domain.calculators.ConstructionCalculators.calcularAco(10.0, 120.0)
    org.junit.Assert.assertTrue(aco.pesoTotalKg > 0)
    org.junit.Assert.assertEquals(10, aco.barras12m)
  }
}
