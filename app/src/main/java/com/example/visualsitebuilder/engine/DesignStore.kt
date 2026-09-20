// Serializes and restores the canvas element tree as JSON.
package com.example.visualsitebuilder.engine

import com.example.visualsitebuilder.model.DesignElement
import com.example.visualsitebuilder.model.ElementType
import org.json.JSONArray
import org.json.JSONObject

object DesignStore {
    fun serialize(elements: List<DesignElement>): String {
        val array = JSONArray()
        elements.forEach { array.put(toJson(it)) }
        return array.toString()
    }

    fun deserialize(json: String): List<DesignElement> {
        val array = JSONArray(json)
        return List(array.length()) { index -> fromJson(array.getJSONObject(index)) }
    }

    private fun toJson(el: DesignElement): JSONObject {
        val children = JSONArray()
        el.children.forEach { children.put(toJson(it)) }
        return JSONObject()
            .put("id", el.id)
            .put("type", el.type.name)
            .put("x", el.x.toDouble())
            .put("y", el.y.toDouble())
            .put("width", el.width.toDouble())
            .put("height", el.height.toDouble())
            .put("text", el.text)
            .put("backgroundColor", el.backgroundColor)
            .put("textColor", el.textColor)
            .put("fontSize", el.fontSize)
            .put("imageUri", el.imageUri)
            .put("children", children)
    }

    private fun fromJson(obj: JSONObject): DesignElement {
        val children = mutableListOf<DesignElement>()
        val childrenArray = obj.optJSONArray("children")
        if (childrenArray != null) {
            for (i in 0 until childrenArray.length()) {
                children.add(fromJson(childrenArray.getJSONObject(i)))
            }
        }
        return DesignElement(
            id = obj.getString("id"),
            type = ElementType.valueOf(obj.getString("type")),
            x = obj.getDouble("x").toFloat(),
            y = obj.getDouble("y").toFloat(),
            width = obj.getDouble("width").toFloat(),
            height = obj.getDouble("height").toFloat(),
            text = obj.optString("text", ""),
            backgroundColor = obj.optString("backgroundColor", "#FFFFFF"),
            textColor = obj.optString("textColor", "#000000"),
            fontSize = obj.optInt("fontSize", 16),
            imageUri = obj.optString("imageUri", ""),
            children = children
        )
    }
}
