package au.com.codeka.warworlds.common.sim

import au.com.codeka.warworlds.common.proto.*
import au.com.codeka.warworlds.common.sim.BuildHelper.getBuildProgress
import au.com.codeka.warworlds.common.sim.ColonyHelper.getDeltaMineralsPerHour
import au.com.codeka.warworlds.common.sim.StarHelper.getDeltaMineralsPerHour
import com.google.common.collect.Lists
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.lang.String.join
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Tests that a floating point value is "equal" to the expected value, to the given number of
 * decimal places.
 *
 * <p>Examples:
 *
 * <code>
 *   123.45123 should beCloseTo(123.45, 2) // true
 *   123.45523 should beCloseTo(123.45, 2) // false (it rounds to the nearest: 123.46 here)
 * </code>
 */
fun beCloseTo(expected: Float, decimalPlaces: Int) = object : Matcher<Float> {
  override fun test(value: Float): MatcherResult {
    // I guess this isn't that fast. but it doesn't suffer from precision issues with
    // large decimalPlaces values (like multiplying by 10^decimalPlaces would).
    val df = DecimalFormat("#." + join("", Collections.nCopies(decimalPlaces, "#")))
    df.roundingMode = RoundingMode.HALF_EVEN
    return MatcherResult(
        df.format(value.toDouble()) == df.format(expected.toDouble()),
        "$value should be close to $expected, to $decimalPlaces decimal places",
        "$value should not be close to $expected, to $decimalPlaces decimal places")
  }
}

/** Tests for [Simulation]. */
class SimulationTest {
  private val logHandler: Simulation.LogHandler = object : Simulation.LogHandler {
    private var starName: String? = null

    override fun setStarName(starName: String?) {
      this.starName = starName
    }

    override fun log(message: String) {
      println(String.format("%s : %s", starName, message))
    }
  }

  /** Tests that a simulation on an idle star does nothing.  */
  @Test
  fun `do nothing`() {
    val sim = Simulation(NOW_TIME, logHandler)
    val starBuilder = Star.Builder()
        .id(1L)
        .name("First")
    sim.simulate(starBuilder)
    starBuilder.name shouldBe "First"
    starBuilder.id shouldBe 1L
    starBuilder.last_simulation shouldBe NOW_TIME
  }

  /** Tests that we correctly simulate based on the simulation's STEP_TIME.  */
  @Test
  fun `simulate TIME_STEP`() {
    var sim = Simulation(NOW_TIME, false, logHandler)
    val starBuilder = Star.Builder()
        .id(1L)
        .name("Stardust")
    sim.simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    sim = Simulation(NOW_TIME + Simulation.STEP_TIME - 1, false, logHandler)
    sim.simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME + Simulation.STEP_TIME - 1
    sim = Simulation(NOW_TIME + Simulation.STEP_TIME, false, logHandler)
    sim.simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME + Simulation.STEP_TIME
  }

  @Test
  fun `simple colony with nothing special`() {
    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(100)
                .mining_congeniality(100)
                .population_congeniality(1000)
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.25f)
                                .farming(0.25f)
                                .mining(0.25f)
                                .construction(0.25f)
                                .build())
                        .population(100f)
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(100f)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    starBuilder.planets[0].colony.population should beCloseTo(101.67f, 2)
    starBuilder.empire_stores[0].empire_id shouldBe 1L
    starBuilder.empire_stores[0].total_goods should beCloseTo(102.50f, 2)
    // 100 + 41.67 (=mining_congeniality (100) / 10* focus=0.25 * time_step=0.166) - population_growth=1.67
    starBuilder.empire_stores[0].total_minerals should beCloseTo(141.67f, 2)
    starBuilder.empire_stores[0].total_energy should beCloseTo(141.67f, 2)
  }

  @Test
  fun `colony with an uneven focus`() {
    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(100)
                .mining_congeniality(100)
                .population_congeniality(1000)
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.1f)
                                .farming(0.2f)
                                .mining(0.3f)
                                .construction(0.4f)
                                .build())
                        .population(100f)
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(100f)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    starBuilder.planets[0].colony.population should beCloseTo(101.67f, 2)
    starBuilder.empire_stores[0].empire_id shouldBe 1L
    starBuilder.empire_stores[0].total_goods should beCloseTo(101.67f, 2)
    starBuilder.empire_stores[0].total_minerals should beCloseTo(150.00f, 2)
    starBuilder.empire_stores[0].total_energy should beCloseTo(116.67f, 2)
  }

  @Test
  fun `colony with uneven congeniality`() {
    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality(1000)
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.25f)
                                .farming(0.25f)
                                .mining(0.25f)
                                .construction(0.25f)
                                .build())
                        .population(100f)
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(100f)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    starBuilder.planets[0].colony.population should beCloseTo(101.67f, 2)
    starBuilder.empire_stores[0].empire_id shouldBe 1L
    starBuilder.empire_stores[0].total_goods should beCloseTo(106.67f, 2)
    starBuilder.empire_stores[0].total_minerals should beCloseTo(225.00f, 2)
    starBuilder.empire_stores[0].total_energy should beCloseTo(141.67f, 2)
  }

  @Test
  fun `build colony ship with exactly enough population and minerals`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.COLONY_SHIP).build_cost
    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality((buildCost.population * 1.2f).toInt())
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.0f)
                                .mining(0.0f)
                                .construction(1.0f)
                                .build())
                        .population(buildCost.population * 1.2f)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.COLONY_SHIP)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(buildCost.minerals * 1.2f)
                .total_minerals(120f)
                .build()
        ))
        .name("Stardust 1")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    // We're 120% over quota so we should finish in 1/1.2 of a time step.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + (1 / 1.2 * Simulation.STEP_TIME).toLong()
  }

  @Test
  fun `build colony ship with too much population and exactly enough minerals`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.COLONY_SHIP).build_cost
    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality((buildCost.population * 2).toInt())
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.0f)
                                .mining(0.0f)
                                .construction(1.0f)
                                .build())
                        .population(buildCost.population * 2)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.COLONY_SHIP)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(buildCost.minerals)
                .total_minerals(120f)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    // Because we have double the population needed, we'll finish in half a step. The minerals don't
    // affect the time it takes.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + (Simulation.STEP_TIME / 2)
  }

  @Test
  fun `build colony ship with exactly enough population and too much minerals`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.COLONY_SHIP).build_cost
    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality(buildCost.population.toInt())
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.0f)
                                .mining(0.0f)
                                .construction(1.0f)
                                .build())
                        .population(buildCost.population)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.COLONY_SHIP)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(buildCost.minerals * 2)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    // We're 100% over quota on minerals so we should finish exactly at the end of the step.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + Simulation.STEP_TIME
  }

  @Test
  fun `build colony ship with not enough population`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.COLONY_SHIP).build_cost

    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality(buildCost.population.toInt())
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.9f)
                                .mining(0.0f)
                                .construction(0.1f)
                                .build())
                        .population(buildCost.population)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.COLONY_SHIP)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(120f)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    // We only have 10% of the required population, so it should take 10 steps to complete.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + 10 * Simulation.STEP_TIME
    starBuilder.planets[0].colony.build_requests[0].minerals_efficiency should beCloseTo(0.92f, 2)
    starBuilder.planets[0].colony.build_requests[0].population_efficiency should beCloseTo(0.08f, 2)

    // And it should continue taking 10 steps as we continue simulating.
    Simulation(NOW_TIME + Simulation.STEP_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + 10 * Simulation.STEP_TIME

    // Note as we progress, the efficiency of the population improves as we need less and less
    // population relative to when we started, and we use up the remaining minerals.
    starBuilder.planets[0].colony.build_requests[0].minerals_efficiency should beCloseTo(0.92f, 2)
    starBuilder.planets[0].colony.build_requests[0].population_efficiency should beCloseTo(0.08f, 2)

    Simulation(NOW_TIME + 2 * Simulation.STEP_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + 10 * Simulation.STEP_TIME
    starBuilder.planets[0].colony.build_requests[0].minerals_efficiency should beCloseTo(0.91f, 2)
    starBuilder.planets[0].colony.build_requests[0].population_efficiency should beCloseTo(0.09f, 2)

    Simulation(NOW_TIME + 3 * Simulation.STEP_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + 10 * Simulation.STEP_TIME
    starBuilder.planets[0].colony.build_requests[0].minerals_efficiency should beCloseTo(0.90f, 2)
    starBuilder.planets[0].colony.build_requests[0].population_efficiency should beCloseTo(0.10f, 2)

    // ... and so on, three steps is enough to confirm it works.
  }

  @Test
  fun `build colony ship with not enough minerals`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.COLONY_SHIP).build_cost

    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(100)
                .mining_congeniality(100)
                .population_congeniality((buildCost.population * 2).toInt())
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.0f)
                                .mining(0.0f)
                                .construction(1.0f)
                                .build())
                        .population(1000f)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.COLONY_SHIP)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(buildCost.minerals * 0.1f)
                .build()
        ))
        .name("Stardust")
    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    // We'll have started off with 10% of the required minerals. We're also not mining any and
    // since we're not doing prediction, it'll just progress 10% of the way and then think it needs
    // the same ten more times.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        (NOW_TIME + (10 * Simulation.STEP_TIME.toFloat()).roundToInt())

    starBuilder.planets[0].colony.build_requests[0].minerals_efficiency should beCloseTo(0.05f, 2)
    starBuilder.planets[0].colony.build_requests[0].population_efficiency should beCloseTo(0.95f, 2)
  }

  @Test
  fun `build scout ship half way through a step`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.SCOUT).build_cost

    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality(buildCost.population.toInt() * 200)
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.0f)
                                .mining(0.0f)
                                .construction(1.0f)
                                .build())
                        .population(buildCost.population * 200f)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.SCOUT)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME + Simulation.STEP_TIME / 4)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(1000f)
                .total_goods(1000f)
                .total_minerals(1000f)
                .build()
        ))
        .name("Stardust")

    Simulation(NOW_TIME, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe NOW_TIME
    // We started 1/4 of the way through the step, and we should finish after 1/200th of a step
    // (because we have enough minerals and 200x more population than needed)
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + Simulation.STEP_TIME / 4 + Simulation.STEP_TIME / 200
    // But we're not ACTUALLY finished yet, because actually we've only just started...
    starBuilder.planets[0].colony.build_requests[0].progress should beCloseTo(0.0f, 2)
    var now = NOW_TIME + Simulation.STEP_TIME / 4L + Simulation.STEP_TIME / 400L

    Simulation(now, false, logHandler).simulate(starBuilder)
    // Now we're half-way through
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + Simulation.STEP_TIME / 4 + Simulation.STEP_TIME / 200
    getBuildProgress(starBuilder.planets[0].colony.build_requests[0], now) should beCloseTo(0.5f, 2)

    now = NOW_TIME + Simulation.STEP_TIME / 4 + Simulation.STEP_TIME / 200
    Simulation(now, false, logHandler).simulate(starBuilder)
    // NOW we should be finished.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + Simulation.STEP_TIME / 4 + Simulation.STEP_TIME / 200
    getBuildProgress(starBuilder.planets[0].colony.build_requests[0], now) should beCloseTo(1.0f, 2)
  }

  /** If you have more minerals than you need, you can't build faster. Unlike population.  */
  @Test
  fun `extra minerals doesn't affect build time`() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.COLONY_SHIP).build_cost

    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality(buildCost.population.toInt() * 2)
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.0f)
                                .mining(0.0f)
                                .construction(1.0f)
                                .build())
                        .population(buildCost.population * 2)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.COLONY_SHIP)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(2000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(buildCost.minerals)
                .build()
        ))
        .name("Stardust")
    val now = NOW_TIME
    Simulation(now, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe now
    // Because we haven't started building yet, minerals should not be being used.
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + Simulation.STEP_TIME / 2

    // Doubling the mineral count won't affect the end time.
    starBuilder.empire_stores[0] =
        starBuilder.empire_stores[0].newBuilder().total_minerals(200f).build()
    Simulation(now, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe now
    starBuilder.planets[0].colony.build_requests[0].end_time shouldBe
        NOW_TIME + Simulation.STEP_TIME / 2
  }

  @Test
  fun testMineralUsage() {
    val buildCost = DesignHelper.getDesign(Design.DesignType.SCOUT).build_cost

    val starBuilder = Star.Builder()
        .id(1L)
        .planets(Lists.newArrayList(
            Planet.Builder()
                .index(0)
                .energy_congeniality(100)
                .farming_congeniality(200)
                .mining_congeniality(300)
                .population_congeniality(1000)
                .planet_type(Planet.PLANET_TYPE.TERRAN)
                .colony(
                    Colony.Builder()
                        .empire_id(1L)
                        .focus(
                            ColonyFocus.Builder()
                                .energy(0.0f)
                                .farming(0.8f)
                                .mining(0.1f)
                                .construction(0.1f)
                                .build())
                        .population(1000f)
                        .build_requests(Lists.newArrayList(
                            BuildRequest.Builder()
                                .count(1)
                                .design_type(Design.DesignType.SCOUT)
                                .id(1L)
                                .progress(0.0f)
                                .start_time(NOW_TIME + Simulation.STEP_TIME +
                                    Simulation.STEP_TIME / 4)
                                .build()
                        ))
                        .build())
                .build()
        ))
        .empire_stores(Lists.newArrayList(
            EmpireStorage.Builder()
                .empire_id(1L)
                .max_energy(1000f)
                .max_goods(1000f)
                .max_minerals(1000f)
                .total_energy(100f)
                .total_goods(100f)
                .total_minerals(100f)
                .build()
        ))
        .name("Stardust")

    var now = NOW_TIME
    Simulation(now, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe now
    // Because we haven't started building yet, minerals should not be being used.
    starBuilder.empire_stores[0].total_minerals should beCloseTo(600f, 2)
    getDeltaMineralsPerHour(starBuilder.build(), 1L, now) should beCloseTo(3000.0f, 2)
    getDeltaMineralsPerHour(starBuilder.planets[0].colony, now) should beCloseTo(3000.0f, 2)

    // Now progress time after the start time
    now = NOW_TIME + Simulation.STEP_TIME + Simulation.STEP_TIME / 4 + Simulation.STEP_TIME / 40
    Simulation(now, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe now
    // We're half-way through, so we should be using those minerals (it would be 1200 if we weren't
    // using it).
    starBuilder.empire_stores[0].total_minerals should beCloseTo(1100f, 2)
    getDeltaMineralsPerHour(starBuilder.build(), 1L, now) should beCloseTo(2966.67f, 2)
    getDeltaMineralsPerHour(starBuilder.planets[0].colony, now) should beCloseTo(3000f, 2)

    // And progress time a little bit more
    now = NOW_TIME + Simulation.STEP_TIME + Simulation.STEP_TIME / 4 + Simulation.STEP_TIME / 10
    Simulation(now, false, logHandler).simulate(starBuilder)
    starBuilder.last_simulation shouldBe now
    // Now we're done, we're no longer using the minerals.
    starBuilder.empire_stores[0].total_minerals should beCloseTo(1100f, 2)

    // TODO: I think this is wrong, it should be 3000.0f
    getDeltaMineralsPerHour(starBuilder.build(), 1L, now) should beCloseTo(2933.33f, 2)
    getDeltaMineralsPerHour(starBuilder.planets[0].colony, now) should beCloseTo(3000.0f, 2)
  }

  companion object {
    /** The time to use as "now", so we're not trying to use the 'real' current time.  */
    private const val NOW_TIME = Simulation.STEP_TIME * 100L
  }
}