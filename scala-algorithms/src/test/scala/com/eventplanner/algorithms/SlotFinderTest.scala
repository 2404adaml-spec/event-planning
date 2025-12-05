package com.eventplanner.algorithms

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._

/**
 * Unit tests for SlotFinder algorithm.
 */
class SlotFinderTest extends AnyFunSuite with Matchers {

  test("should find available slot when venue is free") {
    val venues = List(
      VenueSlot("v1", "Main Hall", 100, "Building A")
    ).asJava

    val occupiedSlots = List.empty[OccupiedSlot].asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 50,
      startDate = "2024-03-15",
      durationMinutes = 60,
      preferredStartTime = "09:00"
    )

    result.found shouldBe true
    result.venue.name shouldBe "Main Hall"
    result.date shouldBe "2024-03-15"
  }

  test("should not find slot when no venue has sufficient capacity") {
    val venues = List(
      VenueSlot("v1", "Small Room", 20, "Building A"),
      VenueSlot("v2", "Medium Room", 40, "Building B")
    ).asJava

    val occupiedSlots = List.empty[OccupiedSlot].asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 100,
      startDate = "2024-03-15",
      durationMinutes = 60,
      preferredStartTime = "09:00"
    )

    result.found shouldBe false
  }

  test("should prefer smaller suitable venue") {
    val venues = List(
      VenueSlot("v1", "Large Hall", 200, "Building A"),
      VenueSlot("v2", "Medium Room", 50, "Building B")
    ).asJava

    val occupiedSlots = List.empty[OccupiedSlot].asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 30,
      startDate = "2024-03-15",
      durationMinutes = 60,
      preferredStartTime = "09:00"
    )

    result.found shouldBe true
    result.venue.name shouldBe "Medium Room"
  }

  test("should avoid time conflicts") {
    val venues = List(
      VenueSlot("v1", "Conference Room", 50, "Building A")
    ).asJava

    val occupiedSlots = List(
      OccupiedSlot("v1", "2024-03-15", "09:00", "10:00")
    ).asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 30,
      startDate = "2024-03-15",
      durationMinutes = 60,
      preferredStartTime = "09:00"
    )

    result.found shouldBe true
    // Should find a slot that doesn't conflict with 09:00-10:00
    result.startTime should not be "09:00"
  }

  test("should return empty result when no venues provided") {
    val venues = List.empty[VenueSlot].asJava
    val occupiedSlots = List.empty[OccupiedSlot].asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 50,
      startDate = "2024-03-15",
      durationMinutes = 60,
      preferredStartTime = "09:00"
    )

    result.found shouldBe false
  }

  test("should find slot on later date if first date is fully booked") {
    val venues = List(
      VenueSlot("v1", "Small Room", 30, "Building A")
    ).asJava

    // Fill the entire day
    val occupiedSlots = (8 until 20).map { hour =>
      OccupiedSlot("v1", "2024-03-15", f"$hour%02d:00", f"${hour + 1}%02d:00")
    }.toList.asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 20,
      startDate = "2024-03-15",
      durationMinutes = 60,
      preferredStartTime = "09:00"
    )

    result.found shouldBe true
    result.date should not be "2024-03-15"
  }

  test("should calculate end time correctly") {
    val venues = List(
      VenueSlot("v1", "Room A", 50, "Building A")
    ).asJava

    val occupiedSlots = List.empty[OccupiedSlot].asJava

    val result = SlotFinder.findSlot(
      venues,
      occupiedSlots,
      requiredCapacity = 30,
      startDate = "2024-03-15",
      durationMinutes = 90,
      preferredStartTime = "10:00"
    )

    result.found shouldBe true
    result.startTime shouldBe "10:00"
    result.endTime shouldBe "11:30"
  }
}
