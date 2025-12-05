package com.eventplanner.algorithms

import scala.jdk.CollectionConverters._

// Event that needs to be scheduled
case class ScheduleEvent(
  id: String,
  title: String,
  durationMinutes: Int,
  requiredCapacity: Int,
  preferredDate: String,
  preferredTime: String
)

// Event after it's been scheduled
case class ScheduledEvent(
  eventId: String,
  eventTitle: String,
  venueId: String,
  venueName: String,
  date: String,
  startTime: String,
  endTime: String,
  scheduled: Boolean
)

// Final result with all scheduled events
case class ScheduleResult(
  scheduledEvents: java.util.List[ScheduledEvent],
  unscheduledEvents: java.util.List[String],
  conflicts: java.util.List[String]
)

// This creates schedules for events
object EventScheduler {

  // Creates a schedule for all events
  def createSchedule(
    events: java.util.List[ScheduleEvent],
    venues: java.util.List[VenueSlot],
    existingOccupied: java.util.List[OccupiedSlot]
  ): ScheduleResult = {

    val eventList = events.asScala.toList
    val venueList = venues.asScala.toList
    val occupiedList = existingOccupied.asScala.toList

    // Sort events by date - earlier dates first
    val sortedEvents = eventList.sortBy(_.preferredDate)

    // Keep track of scheduled events, unscheduled ones, and occupied slots
    var scheduled = List[ScheduledEvent]()
    var unscheduled = List[String]()
    var currentOccupied = occupiedList

    // Try to schedule each event
    for (event <- sortedEvents) {
      val result = scheduleEvent(event, venueList, currentOccupied)

      result match {
        case Some((scheduledEvent, newOccupied)) =>
          // Successfully scheduled
          scheduled = scheduled :+ scheduledEvent
          currentOccupied = currentOccupied :+ newOccupied
        case None =>
          // Couldn't schedule this event
          unscheduled = unscheduled :+ event.id
      }
    }

    // Check for any conflicts
    val conflicts = detectConflicts(scheduled)

    ScheduleResult(
      scheduled.asJava,
      unscheduled.asJava,
      conflicts.asJava
    )
  }

  // Try to schedule one event
  private def scheduleEvent(
    event: ScheduleEvent,
    venues: List[VenueSlot],
    occupiedSlots: List[OccupiedSlot]
  ): Option[(ScheduledEvent, OccupiedSlot)] = {

    // Find venues with enough capacity
    var suitableVenues = List[VenueSlot]()
    for (venue <- venues) {
      if (venue.capacity >= event.requiredCapacity) {
        suitableVenues = suitableVenues :+ venue
      }
    }
    suitableVenues = suitableVenues.sortBy(_.capacity)

    // Try each venue until we find one that works
    var result: Option[(ScheduledEvent, OccupiedSlot)] = None
    for (venue <- suitableVenues if result.isEmpty) {
      val slot = tryScheduleAtVenue(event, venue, occupiedSlots)
      if (slot.isDefined) {
        result = slot
      }
    }

    result
  }

  // Try to schedule event at this venue
  private def tryScheduleAtVenue(
    event: ScheduleEvent,
    venue: VenueSlot,
    occupiedSlots: List[OccupiedSlot]
  ): Option[(ScheduledEvent, OccupiedSlot)] = {

    // Try next 14 days
    val dates = generateDates(event.preferredDate, 14)

    var result: Option[(ScheduledEvent, OccupiedSlot)] = None
    for (date <- dates if result.isEmpty) {
      val slot = tryScheduleOnDate(event, venue, date, occupiedSlots)
      if (slot.isDefined) {
        result = slot
      }
    }

    result
  }

  /**
   * Tries to schedule an event at a venue on a specific date.
   */
  private def tryScheduleOnDate(
    event: ScheduleEvent,
    venue: VenueSlot,
    date: String,
    occupiedSlots: List[OccupiedSlot]
  ): Option[(ScheduledEvent, OccupiedSlot)] = {

    // Get slots occupied for this venue on this date
    val venueOccupied = occupiedSlots
      .filter(slot => slot.venueId == venue.id && slot.date == date)

    // Generate time slots to try
    val timeSlots = generateTimeSlots(event.preferredTime)

    // Find first available slot
    timeSlots.find { startTime =>
      !hasTimeConflict(startTime, event.durationMinutes, venueOccupied)
    }.map { startTime =>
      val endTime = addMinutesToTime(startTime, event.durationMinutes)

      val scheduledEvent = ScheduledEvent(
        event.id,
        event.title,
        venue.id,
        venue.name,
        date,
        startTime,
        endTime,
        scheduled = true
      )

      val newOccupied = OccupiedSlot(venue.id, date, startTime, endTime)

      (scheduledEvent, newOccupied)
    }
  }

  /**
   * Checks if a time slot has conflicts with existing slots.
   */
  private def hasTimeConflict(
    startTime: String,
    durationMinutes: Int,
    occupiedSlots: List[OccupiedSlot]
  ): Boolean = {
    val startMinutes = timeToMinutes(startTime)
    val endMinutes = startMinutes + durationMinutes

    occupiedSlots.exists { occupied =>
      val occupiedStart = timeToMinutes(occupied.startTime)
      val occupiedEnd = timeToMinutes(occupied.endTime)

      // Check for overlap
      !(endMinutes <= occupiedStart || startMinutes >= occupiedEnd)
    }
  }

  /**
   * Detects conflicts in the scheduled events.
   */
  private def detectConflicts(scheduled: List[ScheduledEvent]): List[String] = {
    scheduled
      .groupBy(e => (e.venueId, e.date))
      .flatMap { case ((venueId, date), events) =>
        findOverlappingEvents(events)
      }
      .toList
  }

  /**
   * Finds overlapping events at the same venue on the same date.
   */
  private def findOverlappingEvents(events: List[ScheduledEvent]): List[String] = {
    val sorted = events.sortBy(e => timeToMinutes(e.startTime))

    sorted.sliding(2).flatMap {
      case List(e1, e2) =>
        val e1End = timeToMinutes(e1.endTime)
        val e2Start = timeToMinutes(e2.startTime)
        if (e1End > e2Start) {
          Some(s"Conflict: ${e1.eventTitle} and ${e2.eventTitle}")
        } else None
      case _ => None
    }.toList
  }

  /**
   * Generates time slots for scheduling.
   */
  private def generateTimeSlots(preferredTime: String): List[String] = {
    val preferredMinutes = timeToMinutes(preferredTime)
    val slots = (8 * 60 until 20 * 60 by 30).map(minutesToTime).toList
    slots.sortBy(slot => Math.abs(timeToMinutes(slot) - preferredMinutes))
  }

  // Generate list of dates
  private def generateDates(startDate: String, days: Int): List[String] = {
    import java.time.LocalDate
    import java.time.format.DateTimeFormatter

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val start = LocalDate.parse(startDate, formatter)

    // add days one by one
    var dateList = List[String]()
    for (i <- 0 until days) {
      val newDate = start.plusDays(i)
      dateList = dateList :+ newDate.format(formatter)
    }

    dateList
  }

  /**
   * Converts time string to minutes since midnight.
   */
  private def timeToMinutes(time: String): Int = {
    val parts = time.split(":")
    parts(0).toInt * 60 + parts(1).toInt
  }

  /**
   * Converts minutes to time string.
   */
  private def minutesToTime(minutes: Int): String = {
    f"${minutes / 60}%02d:${minutes % 60}%02d"
  }

  /**
   * Adds minutes to a time string.
   */
  private def addMinutesToTime(time: String, minutes: Int): String = {
    minutesToTime(timeToMinutes(time) + minutes)
  }

  /**
   * Java-friendly method for creating schedule.
   */
  def schedule(
    events: java.util.List[ScheduleEvent],
    venues: java.util.List[VenueSlot],
    existingOccupied: java.util.List[OccupiedSlot]
  ): ScheduleResult = {
    createSchedule(events, venues, existingOccupied)
  }
}
