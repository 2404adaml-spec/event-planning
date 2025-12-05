package com.eventplanner.algorithms

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._

/**
 * Unit tests for EventScheduler algorithm.
 */
class EventSchedulerTest extends AnyFunSuite with Matchers {

  test("should schedule single event successfully") {
    val events = List(
      ScheduleEvent("e1", "Workshop", 60, 30, "2024-03-15", "09:00")
    ).asJava

    val venues = List(
      VenueSlot("v1", "Conference Room", 50, "Building A")
    ).asJava

    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 1
    result.unscheduledEvents.size shouldBe 0
    result.conflicts.size shouldBe 0

    val scheduled = result.scheduledEvents.get(0)
    scheduled.eventTitle shouldBe "Workshop"
    scheduled.scheduled shouldBe true
  }

  test("should schedule multiple events without conflicts") {
    val events = List(
      ScheduleEvent("e1", "Morning Workshop", 60, 30, "2024-03-15", "09:00"),
      ScheduleEvent("e2", "Afternoon Session", 60, 30, "2024-03-15", "14:00")
    ).asJava

    val venues = List(
      VenueSlot("v1", "Room A", 50, "Building A")
    ).asJava

    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 2
    result.unscheduledEvents.size shouldBe 0
    result.conflicts.size shouldBe 0
  }

  test("should handle events requiring different venue capacities") {
    val events = List(
      ScheduleEvent("e1", "Small Meeting", 60, 20, "2024-03-15", "09:00"),
      ScheduleEvent("e2", "Large Conference", 120, 100, "2024-03-15", "09:00")
    ).asJava

    val venues = List(
      VenueSlot("v1", "Small Room", 30, "Building A"),
      VenueSlot("v2", "Large Hall", 150, "Building B")
    ).asJava

    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 2
    result.unscheduledEvents.size shouldBe 0
  }

  test("should not schedule event when no suitable venue exists") {
    val events = List(
      ScheduleEvent("e1", "Huge Event", 60, 500, "2024-03-15", "09:00")
    ).asJava

    val venues = List(
      VenueSlot("v1", "Small Room", 50, "Building A")
    ).asJava

    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 0
    result.unscheduledEvents.size shouldBe 1
  }

  test("should return empty result for empty event list") {
    val events = List.empty[ScheduleEvent].asJava
    val venues = List(VenueSlot("v1", "Room A", 50, "Building A")).asJava
    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 0
    result.unscheduledEvents.size shouldBe 0
  }

  test("should respect existing occupied slots") {
    val events = List(
      ScheduleEvent("e1", "New Event", 60, 30, "2024-03-15", "09:00")
    ).asJava

    val venues = List(
      VenueSlot("v1", "Room A", 50, "Building A")
    ).asJava

    val existingOccupied = List(
      OccupiedSlot("v1", "2024-03-15", "09:00", "10:00")
    ).asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 1
    val scheduled = result.scheduledEvents.get(0)
    // Should be scheduled at a different time to avoid conflict
    scheduled.startTime should not be "09:00"
  }

  test("should schedule events on different dates when necessary") {
    val events = List(
      ScheduleEvent("e1", "Event 1", 480, 30, "2024-03-15", "09:00"), // 8 hours
      ScheduleEvent("e2", "Event 2", 480, 30, "2024-03-15", "09:00")  // 8 hours
    ).asJava

    val venues = List(
      VenueSlot("v1", "Room A", 50, "Building A")
    ).asJava

    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 2
    // Events should be on different dates since one takes the whole day
  }

  test("should prioritize events by preferred date") {
    val events = List(
      ScheduleEvent("e1", "Later Event", 60, 30, "2024-03-20", "09:00"),
      ScheduleEvent("e2", "Earlier Event", 60, 30, "2024-03-15", "09:00")
    ).asJava

    val venues = List(
      VenueSlot("v1", "Room A", 50, "Building A")
    ).asJava

    val existingOccupied = List.empty[OccupiedSlot].asJava

    val result = EventScheduler.schedule(events, venues, existingOccupied)

    result.scheduledEvents.size shouldBe 2
    // Earlier event should be scheduled first
    val first = result.scheduledEvents.get(0)
    first.eventTitle shouldBe "Earlier Event"
  }
}
