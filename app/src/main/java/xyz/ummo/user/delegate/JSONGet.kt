package xyz.ummo.user.delegate

import org.json.JSONException
import org.json.JSONObject

object get {
    fun get(obj: JSONObject, path: String, default: Any): Any? {
        try {
            return if (path.contains("."))
                get(obj.getJSONObject(path.substringBefore(".")), path.substringAfter("."), default)
            else
                obj.get(path)
        } catch (ex: JSONException) {
            return default
        }

    }
}
