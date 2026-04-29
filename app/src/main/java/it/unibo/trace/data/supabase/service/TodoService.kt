package it.unibo.trace.data.supabase.service

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.supabase

/**
 * Service object handling direct CRUD operations for Todo items via Supabase Postgrest.
 */
object TodoService {
    private const val TABLE_NAME = "Todos"

    /**
     * Fetches all todos belonging to a specific user, ordered by ID descending.
     * 
     * @param userId The UID of the user whose tasks are to be retrieved.
     * @return A list of [TodoItem] objects.
     */
    suspend fun getTodos(userId: String): List<TodoItem> {
        return supabase.from(TABLE_NAME)
            .select {
                filter {
                    eq("uid", userId)
                }
                order("id", Order.DESCENDING)
            }.decodeList<TodoItem>()
    }

    /**
     * Inserts a new todo item into the database.
     * 
     * @param todo The [TodoItem] to be inserted.
     */
    suspend fun insertTodo(todo: TodoItem) {
        supabase.from(TABLE_NAME).insert(todo)
    }

    /**
     * Deletes a specific todo item by its ID.
     * 
     * @param todoId The unique ID of the todo to delete.
     */
    suspend fun deleteTodo(todoId: Long) {
        supabase.from(TABLE_NAME).delete {
            filter {
                eq("id", todoId)
            }
        }
    }

    /**
     * Updates an existing todo item in the database.
     * 
     * @param todo The [TodoItem] containing updated data. 
     *             The record is matched based on [TodoItem.id].
     */
    suspend fun updateTodo(todo: TodoItem) {
        supabase.from(TABLE_NAME).update(todo) {
            filter {
                eq("id", todo.id ?: 0L)
            }
        }
    }
}
