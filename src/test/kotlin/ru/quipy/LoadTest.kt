//package ru.quipy
//
//import org.junit.jupiter.api.*
//import org.junit.jupiter.api.Assertions.assertNotNull
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.annotation.DirtiesContext
//import org.springframework.test.context.ActiveProfiles
//import ru.quipy.api.ProjectAggregate
//import ru.quipy.api.UserAggregate
//import ru.quipy.core.EventSourcingService
//import ru.quipy.logic.*
//import java.util.*
//import java.util.concurrent.CountDownLatch
//
//@SpringBootTest
//@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class LoadTest {
//    @Autowired
//    private lateinit var projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
//
//    @Autowired
//    private lateinit var userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>
//
//    @Test
//    fun testCreateProjectWithAddUserConcurrently() {
//        val projectId = UUID.randomUUID()
//        val userId = UUID.randomUUID()
//        val actorId = UUID.randomUUID()
//
//        val startLatch = CountDownLatch(1)
//        val finishLatch = CountDownLatch(2)
//
//        val createProjectThread = Thread {
//            startLatch.await()
//            projectEsService.create{it.create(projectId, "Project Title", actorId)}
//            finishLatch.countDown()
//        }
//
//        val addUserThread = Thread {
//            startLatch.await()
//            projectEsService.update(projectId){it.addUser(projectId, userId, actorId)}
//            finishLatch.countDown()
//        }
//
//        createProjectThread.start()
//        addUserThread.start()
//
//        startLatch.countDown()
//
//        finishLatch.await()
//
//        val projectState = projectEsService.getState(projectId)
//        assertNotNull(projectState)
////        assertNotNull()//user
//    }
//
//    @Test
//    fun testChangeProjectTitleConcurrently() {
//        val projectId = UUID.randomUUID()
//        val actorId1 = UUID.randomUUID()
//        val actorId2 = UUID.randomUUID()
//
//        val startLatch = CountDownLatch(1)
//        val finishLatch = CountDownLatch(2)
//
//        val createProjectThread = Thread {
//            startLatch.await()
//            projectEsService.create{it.create(projectId, "Project Title", actorId1)}
//            finishLatch.countDown()
//        }
//
//        val changeTitleThread = Thread {
//            startLatch.await()
//            projectEsService.update(projectId){it.changeTitle(projectId, "New Project Title", actorId2)}
//            finishLatch.countDown()
//        }
//
//        createProjectThread.start()
//        changeTitleThread.start()
//
//        startLatch.countDown()
//
//        finishLatch.await()
//
//        // Assert the project title is changed correctly
//        // ...
//    }
//
//    @Test
//    fun testCreateStatusAndAddTaskConcurrently() {
//        val projectId = UUID.randomUUID()
//        val actorId1 = UUID.randomUUID()
//        val actorId2 = UUID.randomUUID()
//
//        val startLatch = CountDownLatch(1)
//        val finishLatch = CountDownLatch(2)
//
//        val createProjectThread = Thread {
//            startLatch.await()
//            projectEsService.create{it.create(projectId, "Project Title", actorId1)}
//            finishLatch.countDown()
//        }
//
//        val createStatusThread = Thread {
//            startLatch.await()
//            projectEsService.update(projectId){it.addStatus("New Status", "Red", actorId1)}
//            finishLatch.countDown()
//        }
//
//        val addTaskThread = Thread {
//            startLatch.await()
//            projectEsService.update(projectId){it.addTask("Task 1", actorId2)}
//            finishLatch.countDown()
//        }
//
//        createStatusThread.start()
//        addTaskThread.start()
//
//        startLatch.countDown()
//
//        finishLatch.await()
//
//        // Assert the status and task are added correctly
//        // ...
//    }
//
//    @Test
//    fun testChangeTaskStatusConcurrently() {
//        val projectId = UUID.randomUUID()
//        val taskId = UUID.randomUUID()
//        val actorId1 = UUID.randomUUID()
//        val actorId2 = UUID.randomUUID()
//
//        val startLatch = CountDownLatch(1)
//        val finishLatch = CountDownLatch(2)
//
//        val createProjectThread = Thread {
//            startLatch.await()
//            projectEsService.create{it.create(projectId, "Project Title", actorId1)}
//            finishLatch.countDown()
//        }
//
//        val createTaskThread = Thread {
//            startLatch.await()
//            projectEsService.update(projectId){it.addTask("Task 1", actorId1)}
//            finishLatch.countDown()
//        }
//
//        val changeStatusThread = Thread {
//            startLatch.await()
//            projectEsService.update(projectId){it.changeTaskStatus(taskId, UUID.randomUUID(), actorId2)}
//            finishLatch.countDown()
//        }
//
//        createTaskThread.start()
//        changeStatusThread.start()
//
//        startLatch.countDown()
//
//        finishLatch.await()
//
//        // Assert the task status is changed correctly
//        // ...
//    }
//}