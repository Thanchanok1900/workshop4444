package com.example

import kotlinx.serialization.Serializable
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

@Serializable
data class Task(val id: Int, val content: String, val isDone: Boolean)

@Serializable
data class TaskRequest(val content: String, val isDone: Boolean)

object TaskRepository {
    private val tasks = mutableListOf<Task>(
        Task(id = 1, content = "Learn Ktor", isDone = true),
        Task(id = 2, content = "Build a Rest API", isDone = false),
        Task(id = 3, content = "Write Unit Tests", isDone = false)
    )

    fun getAll(): List<Task> = tasks

    fun getById(id: Int): Task? = tasks.find { it.id == id }

    fun add(task: Task) {
        tasks.add(task)
    }

    fun update(id: Int, updatedTask: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == id }
        return if (index != -1) {
            tasks[index] = updatedTask
            true
        } else false
    }

    fun delete(id: Int): Boolean {
        return tasks.removeIf { it.id == id }
    }

    fun getNextId(): Int {
        return (tasks.maxOfOrNull { it.id } ?: 0) + 1
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello Thanchanok Phinyojit")
        }

        // GET /tasks
        get("/tasks") {
            call.respond(TaskRepository.getAll())
        }

        // GET /tasks/{id}
        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val task = id?.let { TaskRepository.getById(it) }
            if (task == null) {
                call.respondText("Task not found", status = HttpStatusCode.NotFound)
            } else {
                call.respond(task)
            }
        }

        // POST /tasks
        post("/tasks") {
            val request = call.receive<TaskRequest>()
            val newTask = Task(id = TaskRepository.getNextId(), content = request.content, isDone = request.isDone)
            TaskRepository.add(newTask)
            call.respond(HttpStatusCode.Created, newTask)
        }

        // PUT /tasks/{id}
        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                return@put
            }
            val request = call.receive<TaskRequest>()
            val updated = Task(id = id, content = request.content, isDone = request.isDone)
            if (TaskRepository.update(id, updated)) {
                call.respond(updated)
            } else {
                call.respondText("Task not found", status = HttpStatusCode.NotFound)
            }
        }

        // DELETE /tasks/{id}
        delete("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null || !TaskRepository.delete(id)) {
                call.respondText("Task not found", status = HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
