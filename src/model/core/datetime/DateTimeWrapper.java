package model.core.datetime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Wrapper class for date/time operations that handles timezone conversions.
 * This abstraction makes the design more flexible for dealing with different date/time types.
 */
public class DateTimeWrapper {
    private final ZonedDateTime dateTime;

    public DateTimeWrapper(LocalDateTime dateTime, String timezone) {
        this.dateTime = dateTime.atZone(ZoneId.of(timezone));
    }

    public DateTimeWrapper(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime toLocalDateTime() {
        return dateTime.toLocalDateTime();
    }

    public ZonedDateTime toZonedDateTime() {
        return dateTime;
    }

    public DateTimeWrapper convertToTimezone(String targetTimezone) {
        return new DateTimeWrapper(dateTime.withZoneSameInstant(ZoneId.of(targetTimezone)));
    }

    public boolean isBefore(DateTimeWrapper other) {
        return dateTime.isBefore(other.dateTime);
    }

    public boolean isAfter(DateTimeWrapper other) {
        return dateTime.isAfter(other.dateTime);
    }

    public boolean overlaps(DateTimeWrapper start, DateTimeWrapper end) {
        return !dateTime.isBefore(start.dateTime) && !dateTime.isAfter(end.dateTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateTimeWrapper that = (DateTimeWrapper) o;
        return dateTime.equals(that.dateTime);
    }

    @Override
    public int hashCode() {
        return dateTime.hashCode();
    }
} 