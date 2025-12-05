package com.eventplanner.algorithms

import scala.jdk.CollectionConverters._

// Represents a venue
case class VenueSlot(
  id: String,
  name: String,
  capacity: Int,
  location: String
)

// Represents a time slot
case class TimeSlot(
  date: String,      // YYYY-MM-DD
  startTime: String, // HH:MM
  endTime: String    // HH:MM
)

// Represents an occupied/booked slot
case class OccupiedSlot(
  venueId: String,
  date: String,
  startTime: String,
  endTime: String
)

// Result from the slot finder
case class SlotFinderResult(
  venue: VenueSlot,
  date: String,
  startTime: String,
  endTime: String,
  found: Boolean
)

// This object finds available slots for events
object SlotFinder {

  // Main function to find available slot
  // Returns the first slot that works for the event
  def findFirstAvailableSlot(
    venues: java.util.List[VenueSlot],
    occupiedSlots: java.util.List[OccupiedSlot],
    requiredCapacity: Int,
    startDate: String,
    durationMinutes: Int,
    preferredStartTime: String
  ): SlotFinderResult = {

    val venueList = venues.asScala.toList
    val occupiedList = occupiedSlots.asScala.toList

    // find venues that have enough capacity
    var suitableVenues = List[VenueSlot]()
    for (venue <- venueList) {
      if (venue.capacity >= requiredCapacity) {
        suitableVenues = suitableVenues :+ venue
      }
    }
    // sort by capacity - prefer smaller venues
    suitableVenues = suitableVenues.sortBy(_.capacity)

    // Generate dates to check (next 30 days)
    val datesToCheck = generateDates(startDate, 30)

    // Try to find available slot by checking each venue and date
    var result: Option[SlotFinderResult] = None
    for (venue <- suitableVenues if result.isEmpty) {
      for (date <- datesToCheck if result.isEmpty) {
        val slot = findAvailableTimeSlot(venue, date, occupiedList, durationMinutes, preferredStartTime)
        if (slot.isDefined) {
          result = slot
        }
      }
    }

    result.getOrElse(
      SlotFinderResult(
        VenueSlot("", "No venue found", 0, ""),
        "",
        "",
        "",
        found = false
      )
    )
  }

  /**
   * Finds an available time slot for a specific venue and date.
   */
  private def findAvailableTimeSlot(
    venue: VenueSlot,
    date: String,
    occupiedSlots: List[OccupiedSlot],
    durationMinutes: Int,
    preferredStartTime: String
  ): Option[SlotFinderResult] = {

    // Get occupied slots for this venue on this date
    val venueOccupied = occupiedSlots
      .filter(slot => slot.venueId == venue.id && slot.date == date)

    // Generate possible time slots for the day
    val possibleSlots = generateTimeSlots(preferredStartTime, durationMinutes)

    // Find first non-conflicting slot
    possibleSlots
      .find { slot =>
        !hasConflict(slot, venueOccupied, durationMinutes)
      }
      .map { slot =>
        val endTime = addMinutesToTime(slot, durationMinutes)
        SlotFinderResult(venue, date, slot, endTime, found = true)
      }
  }

  /**
   * Checks if a time slot conflicts with any occupied slots.
   */
  private def hasConflict(
    startTime: String,
    occupiedSlots: List[OccupiedSlot],
    durationMinutes: Int
  ): Boolean = {
    val endTime = addMinutesToTime(startTime, durationMinutes)
    val startMinutes = timeToMinutes(startTime)
    val endMinutes = timeToMinutes(endTime)

    occupiedSlots.exists { occupied =>
      val occupiedStart = timeToMinutes(occupied.startTime)
      val occupiedEnd = timeToMinutes(occupied.endTime)

      // Check for overlap
      !(endMinutes <= occupiedStart || startMinutes >= occupiedEnd)
    }
  }

  // Generate dates for the next 'days' days
  private def generateDates(startDate: String, days: Int): List[String] = {
    import java.time.LocalDate
    import java.time.format.DateTimeFormatter

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val start = LocalDate.parse(startDate, formatter)

    // just add days one by one
    var dateList = List[String]()
    for (i <- 0 until days) {
      val newDate = start.plusDays(i)
      dateList = dateList :+ newDate.format(formatter)
    }

    dateList
  }

  /**
   * Generates possible time slots for a day.
   */
  private def generateTimeSlots(preferredStart: String, durationMinutes: Int): List[String] = {
    val preferredMinutes = timeToMinutes(preferredStart)
    val businessStart = 8 * 60  // 08:00
    val businessEnd = 20 * 60   // 20:00

    // Start from preferred time, then check earlier and later slots
    val slots = (businessStart until (businessEnd - durationMinutes) by 30)
      .map(minutesToTime)
      .toList

    // Sort by proximity to preferred time
    slots.sortBy(slot => Math.abs(timeToMinutes(slot) - preferredMinutes))
  }

  /**
   * Converts time string (HH:MM) to minutes since midnight.
   */
  private def timeToMinutes(time: String): Int = {
    val parts = time.split(":")
    parts(0).toInt * 60 + parts(1).toInt
  }

  /**
   * Converts minutes since midnight to time string (HH:MM).
   */
  private def minutesToTime(minutes: Int): String = {
    f"${minutes / 60}%02d:${minutes % 60}%02d"
  }

  /**
   * Adds minutes to a time string and returns the new time.
   */
  private def addMinutesToTime(time: String, minutes: Int): String = {
    val totalMinutes = timeToMinutes(time) + minutes
    minutesToTime(totalMinutes)
  }

  /**
   * Java-friendly method to find available slots.
   */
  def findSlot(
    venues: java.util.List[VenueSlot],
    occupiedSlots: java.util.List[OccupiedSlot],
    requiredCapacity: Int,
    startDate: String,
    durationMinutes: Int,
    preferredStartTime: String
  ): SlotFinderResult = {
    findFirstAvailableSlot(venues, occupiedSlots, requiredCapacity, startDate, durationMinutes, preferredStartTime)
  }
}
