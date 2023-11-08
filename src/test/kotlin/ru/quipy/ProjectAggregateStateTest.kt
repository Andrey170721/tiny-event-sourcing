package ru.quipy

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.quipy.api.*
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.*
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectAggregateStateTest {

    @Autowired
    private lateinit var projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    private val testProjectId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testTitle = "Test Project"
    private val testStatusName = "Test Status"
    private val testStatusColor = "#ABCDEF"
    private val testTaskName = "Test Task"

    @BeforeEach
    fun init() {
        // Remove existing data
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(testProjectId)), "aggregate-project")
    }

    @Test
    fun createProjectTest() {
        val createdEvent: ProjectCreatedEvent = projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }

        Assertions.assertEquals(testProjectId, createdEvent.projectId)
        Assertions.assertEquals(testTitle, createdEvent.title)
        Assertions.assertEquals(testUserId, createdEvent.creatorId)
    }

    @Test
    fun getProjectTest() {
        projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertNotNull(projectState)
        Assertions.assertEquals(testTitle, projectState?.projectTitle)
        Assertions.assertTrue(projectState?.projectMemberIds?.contains(testUserId) ?: false)
    }

    @Test
    fun addUserToProjectTest() {
        val testActorId = UUID.randomUUID()
        projectEsService.create {
            it.create(testProjectId, testTitle, testActorId)
        }

        val addUserEvent: AddUserToProjectEvent = projectEsService.update(testProjectId) {
            it.addUser(testProjectId, testUserId, testActorId)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertTrue(projectState?.projectMemberIds?.contains(testUserId) ?: false)
    }

    @Test
    fun changeProjectTitleTest() {
        val newTitle = "New Project Title"
        val testActorId = UUID.randomUUID()
        projectEsService.create {
            it.create(testProjectId, testTitle, testActorId)
        }

        val changeTitleEvent: ProjectTitleChangedEvent = projectEsService.update(testProjectId) {
            it.changeTitle(testProjectId, newTitle, testActorId)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertEquals(newTitle, projectState?.projectTitle)
    }

    @Test
    fun createStatusTest() {
        val testActorId = UUID.randomUUID() // Assuming this actor has permissions to add a status
        projectEsService.create {
            it.create(testProjectId, testTitle, testActorId)
        }

        val statusEvent: StatusCreatedEvent = projectEsService.update(testProjectId) {
            it.addStatus(testStatusName, testStatusColor)
        }

        val projectState = projectEsService.getState(testProjectId)
        val statusExists = projectState?.projectStatus?.any {
            it.value.color == testStatusColor && it.key == statusEvent.statusId
//            it.value.name == testStatusName && it.value.color == testStatusColor && it.key == statusEvent.statusId
        } ?: false

        Assertions.assertTrue(statusExists)
    }


    @Test
    fun removeStatusTest() {
        val testActorId = UUID.randomUUID() // Assuming this actor has permissions to remove a status
        val statusId = UUID.randomUUID()
        projectEsService.create {
            it.create(testProjectId, testTitle, testActorId)
        }
        projectEsService.update(testProjectId) {
            it.addStatus(testStatusName, testStatusColor)
        }

        val removeStatusEvent: StatusDeletedEvent = projectEsService.update(testProjectId) {
            it.removeStatus(statusId, testProjectId)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertFalse(projectState?.projectStatus?.containsKey(removeStatusEvent.statusId) ?: true)
    }

    @Test
    fun createTaskTest() {
        projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }

        val taskEvent: TaskCreatedEvent = projectEsService.update(testProjectId) {
            it.addTask(testTaskName)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertTrue(projectState?.tasks?.containsValue(TaskEntity(taskEvent.taskId, testProjectId, testTaskName, null, null)) ?: false)
    }

    @Test
    fun changeTaskTitleTest() {
        // Create a project
        projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }

        // Add a task to the project and retrieve the task ID from the event
        val taskCreatedEvent: TaskCreatedEvent = projectEsService.update(testProjectId) {
            it.addTask(testTaskName)
        }
        val taskId = taskCreatedEvent.taskId // Use the taskId from the event

        // Define the new task title
        val newTaskTitle = "Updated Task Title"

        // Change the task title using the taskId from the event
        val taskTitleEvent: TaskTitleChangedEvent = projectEsService.update(testProjectId) {
            it.changeTaskTitle(taskId, newTaskTitle)
        }

        // Fetch the updated project state and check if the task title has changed
        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertEquals(newTaskTitle, projectState?.tasks?.get(taskId)?.title)
    }

    @Test
    fun changeTaskStatusTest() {
        val taskId = UUID.randomUUID()
        val statusId = UUID.randomUUID()
        projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }
        projectEsService.update(testProjectId) {
            it.addTask(testTaskName)
        }

        val taskStatusEvent: TaskStatusChangedEvent = projectEsService.update(testProjectId) {
            it.changeTaskStatus(taskId, statusId)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertEquals(statusId, projectState?.tasks?.get(taskId)?.status)
    }

    @Test
    fun assignMemberToTaskTest() {
        val taskId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }
        projectEsService.update(testProjectId) {
            it.addTask(testTaskName)
        }

        val memberAssignedEvent: MemberAssignedToTaskEvent = projectEsService.update(testProjectId) {
            it.memberAssignedToTask(userId, taskId)
        }

        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertEquals(userId, projectState?.tasks?.get(taskId)?.executorId)
    }

    @Test
    fun getTasksTest() {
        val testProjectId = UUID.randomUUID()
        val testUserId = UUID.randomUUID()
        val testTitle = "Test Project"
        val testTaskName = "Test Task"

        // Создаём проект
        projectEsService.create {
            it.create(testProjectId, testTitle, testUserId)
        }

        // Добавляем задачу в проект
        val taskCreatedEvent: TaskCreatedEvent = projectEsService.update(testProjectId) {
            it.addTask(testTaskName)
        }

        // Получаем состояние проекта
        val projectState = projectEsService.getState(testProjectId)
        Assertions.assertNotNull(projectState)

        // Получаем задачи из состояния проекта
        val tasks = projectState?.tasks
        Assertions.assertNotNull(tasks)
        Assertions.assertTrue(tasks!!.containsKey(taskCreatedEvent.taskId))

        // Проверяем, что задача с созданным ID присутствует в списке задач
        val retrievedTask = tasks[taskCreatedEvent.taskId]
        Assertions.assertNotNull(retrievedTask)
        Assertions.assertEquals(testTaskName, retrievedTask?.title)
    }

}
