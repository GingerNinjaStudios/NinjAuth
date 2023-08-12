package me.gingerninja.authenticator.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.gingerninja.authenticator.core.database.dao.LabelDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LabelDaoTest {
    private lateinit var labelDao: LabelDao
    private lateinit var db: NinjAuthDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NinjAuthDatabase::class.java).build()
        labelDao = db.labelDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private suspend fun saveLabel(): Long {
        val id = labelDao.saveLabel(testLabels[0])

        val inserted = labelDao.getLabel(id)

        assertEquals(testLabels[0].name, inserted?.name)

        return id
    }

    @Test
    fun createLabel() = runTest {
        saveLabel()
    }

    @Test
    fun deleteLabel() = runTest {
        val id = saveLabel()

        labelDao.deleteLabel(id)

        val isEmpty = labelDao.getLabelsWithCounter().first().isEmpty()

        assertTrue(isEmpty, "Label was not deleted")
    }

    @Test
    @Throws(Exception::class)
    fun createLabelsWithMatchingUids_retry() = runTest {
        val id1 = labelDao.saveLabel(testLabels[0])
        val id2 = labelDao.saveLabel(testLabels[0])

        val inserted1 = labelDao.getLabel(id1)
        val inserted2 = labelDao.getLabel(id2)

        assertNotNull(inserted1, "Entity #1 was not saved")
        assertNotNull(inserted2, "Entity #2 was not saved")

        assertNotEquals(
            inserted1.uid,
            inserted2.uid,
            "The entities' UIDs are equal"
        )
    }
}