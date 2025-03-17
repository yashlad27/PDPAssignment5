package model.event;

@FunctionalInterface
public interface EventAction {

  void apply(Event event);

  default EventAction andThen(EventAction after) {
    return event -> {
      apply(event);
      after.apply(event);
    };
  }

  static EventAction setSubject(String subject) {
    return event -> event.setSubject(subject);
  }

  static EventAction setDescription(String description) {
    return event -> event.setDescription(description);
  }

  static EventAction setLocation(String location) {
    return event -> event.setLocation(location);
  }

  static EventAction setVisibility(boolean isPublic) {
    return event -> event.setPublic(isPublic);
  }

  static EventAction setAllDay(boolean isAllDay) {
    return event -> event.setAllDay(isAllDay);
  }
}
