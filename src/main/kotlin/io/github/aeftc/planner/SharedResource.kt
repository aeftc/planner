package io.github.aeftc.planner

/**
 * Represents something that only one Task can use at a time.
 * @param id Unique identifier for this resource.
 */
class SharedResource(val id: String) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            (other == null) -> false
            is String -> id == other
            is SharedResource -> id == other.id
            else -> false
        }
    }

    override fun toString(): String {
        return "Loq:$id"
    }
}