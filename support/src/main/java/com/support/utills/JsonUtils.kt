package com.support.utills

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonUtils {

    fun isJSONValid(test: String): Boolean {
        try {
            JSONObject(test)
        } catch (ex: JSONException) {
            try {
                JSONArray(test)
            } catch (ex1: JSONException) {
                return false
            }
        }

        return true
    }

    fun isJSONValidArray(test: String): Boolean {
        try {
            JSONArray(test)
        } catch (ex1: JSONException) {
            return false
        }
        return true
    }

    fun isJSONValidObject(test: String): Boolean {
        try {
            JSONObject(test)
        } catch (ex1: JSONException) {
            return false
        }
        return true
    }
}
