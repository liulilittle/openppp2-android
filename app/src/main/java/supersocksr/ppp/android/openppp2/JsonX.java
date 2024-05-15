package supersocksr.ppp.android.openppp2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

public final class JsonX {
    // Instantiate and new a gson object.
    @NotNull
    private static Gson gson_new() {
        GsonBuilder builder = new GsonBuilder();
        return builder.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    // Json strings are deserialized into Java objects.
    public static <T> T json_deserialize(String json, Class<T> classOfT) {
        if (classOfT == null || json == null || json.length() < 1) {
            return null;
        }

        Gson gson = gson_new();
        try {
            return gson.fromJson(json, classOfT);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Java objects are serialized to Json strings.
    public static String json_serialize(Object obj) {
        if (obj == null) {
            return null;
        }

        Gson gson = gson_new();
        try {
            return gson.toJson(obj);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
