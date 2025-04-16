package ua.mykola.botservice.model;

public record CallbackData(CallbackType type, String value) {
    private static final String SEPARATOR = "_";

    public static CallbackData from(String data) {
        if (data == null || !data.contains(SEPARATOR)) {
            throw new IllegalArgumentException("Invalid callback data format: " + data);
        }

        String[] parts = data.split(SEPARATOR, 2);
        CallbackType type = CallbackType.valueOf(parts[0]);
        String value = parts[1];

        return new CallbackData(type, value);
    }

    @Override
    public String toString() {
        return type + SEPARATOR + value;
    }
}
