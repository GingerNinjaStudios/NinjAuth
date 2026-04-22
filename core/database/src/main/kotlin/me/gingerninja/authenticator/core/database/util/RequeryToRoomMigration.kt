package me.gingerninja.authenticator.core.database.util

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlin.time.Clock

/**
 * Migration class used when upgrading to the new app which uses Room instead of requery.
 * The old app's last DB version was 11.
 */
class RequeryToRoomMigration : Migration(11, 12) {
    val millis by lazy {
        Clock.System.now().toEpochMilliseconds()
    }

    override fun migrate(db: SupportSQLiteDatabase) {
        println("[DB] migration from ${db.version}")
        migrateAccounts(db)
        migrateLabels(db)
        migrateAccountHasLabels(db)

        dropTempTables(db)

        println("[DB] migration complete")
    }

    private fun migrateAccounts(db: SupportSQLiteDatabase) {
        // create temp table
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS
                    `Account_copy` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `accountName` TEXT NOT NULL,
                        `secret` TEXT NOT NULL,
                        `digits` INTEGER NOT NULL DEFAULT 6,
                        `type` TEXT NOT NULL DEFAULT 'totp',
                        `source` TEXT NOT NULL DEFAULT 'manual',
                        `algorithm` TEXT NOT NULL DEFAULT 'sha1',
                        `typeSpecificData` INTEGER NOT NULL DEFAULT 0,
                        `title` TEXT DEFAULT null,
                        `issuer` TEXT DEFAULT null,
                        `position` INTEGER NOT NULL DEFAULT -1,
                        `createdAt` INTEGER NOT NULL DEFAULT (CAST((strftime('%s', 'now') + strftime('%f','now') - strftime('%S','now')) * 1000 AS INTEGER)), 
                        `updatedAt` INTEGER NOT NULL DEFAULT (CAST((strftime('%s', 'now') + strftime('%f','now') - strftime('%S','now')) * 1000 AS INTEGER)),
                        `uid` TEXT NOT NULL
                    )

            """.trimIndent()
        )

        // copy data from original to temp table
        db.execSQL(
            """
                INSERT INTO
                    `Account_copy` (
                        `id`,
                        `accountName`,
                        `algorithm`,
                        `digits`,
                        `issuer`,
                        `position`,
                        `secret`,
                        `source`,
                        `title`,
                        `type`,
                        `typeSpecificData`,
                        `uid`,
                        `createdAt`,
                        `updatedAt`
                    )
                SELECT
                    `id`,
                    `accountName`,
                    `algorithm`,
                    `digits`,
                    `issuer`,
                    `position`,
                    `secret`,
                    `source`,
                    `title`,
                    `type`,
                    `typeSpecificData`,
                    `uid`,
                    '$millis',
                    '$millis'
                FROM
                    `Account`
            """.trimIndent()
        )

        // drop the original table
        db.execSQL("DROP TABLE `Account`")

        // rename the temp table to the original
        db.execSQL("ALTER TABLE `Account_copy` RENAME TO `Account`")

        // create index
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Account_uid` ON `Account` (`uid`)")
    }

    private fun migrateLabels(db: SupportSQLiteDatabase) {
        // create temp table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
                `Label_copy` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `color` INTEGER NOT NULL,
                    `icon` TEXT DEFAULT null,
                    `position` INTEGER NOT NULL DEFAULT -1,
                    `createdAt` INTEGER NOT NULL DEFAULT (CAST((strftime('%s', 'now') + strftime('%f','now') - strftime('%S','now')) * 1000 AS INTEGER)),
                    `updatedAt` INTEGER NOT NULL DEFAULT (CAST((strftime('%s', 'now') + strftime('%f','now') - strftime('%S','now')) * 1000 AS INTEGER)),
                    `uid` TEXT NOT NULL
                )
            """.trimIndent()
        )

        // copy data from original to temp table
        db.execSQL(
            """
                INSERT INTO
                    `Label_copy` (
                        `id`,
                        `color`,
                        `icon`,
                        `name`,
                        `position`,
                        `uid`,
                        `createdAt`,
                        `updatedAt`
                    )
                SELECT
                    `id`,
                    `color`,
                    `icon`,
                    `name`,
                    `position`,
                    `uid`,
                    '$millis',
                    '$millis'
                FROM
                    `Label`
            """.trimIndent()
        )

        // drop the original table
        db.execSQL("DROP TABLE `Label`")

        // rename the temp table to the original
        db.execSQL("ALTER TABLE `Label_copy` RENAME TO `Label`")

        // create index
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Label_uid` ON `Label` (`uid`)")
    }

    private fun migrateAccountHasLabels(db: SupportSQLiteDatabase) {
        // create temp table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS
                `AccountHasLabel_copy` (
                    `account` INTEGER NOT NULL,
                    `label` INTEGER NOT NULL,
                    `position` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (`account`, `label`),
                    FOREIGN KEY (`account`) REFERENCES `Account` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY (`label`) REFERENCES `Label` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent()
        )

        // copy data from original to temp table
        db.execSQL(
            """
                INSERT INTO
                    `AccountHasLabel_copy` (`label`, `account`, `position`)
                SELECT
                    `label`,
                    `account`,
                    `position`
                FROM
                    `AccountHasLabel`
            """.trimIndent()
        )

        // drop the original table
        db.execSQL("DROP TABLE `AccountHasLabel`")

        // rename the temp table to the original
        db.execSQL("ALTER TABLE `AccountHasLabel_copy` RENAME TO `AccountHasLabel`")

        // create indices
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountHasLabel_account` ON `AccountHasLabel` (`account`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_AccountHasLabel_label` ON `AccountHasLabel` (`label`)")
    }

    private fun dropTempTables(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE TempAccount")
        db.execSQL("DROP TABLE TempAccountHasLabel")
        db.execSQL("DROP TABLE TempLabel")
    }
}