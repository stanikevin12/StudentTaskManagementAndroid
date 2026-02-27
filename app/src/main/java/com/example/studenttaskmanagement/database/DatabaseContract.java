package com.example.studenttaskmanagement.database;

import android.provider.BaseColumns;

/**
 * Contract class that defines database schema constants for the Student Task Management System.
 */
public final class DatabaseContract {

    private DatabaseContract() {
        // Prevent instantiation.
    }

    public static final class Users implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_CREATED_AT = "created_at";

        private Users() {}
    }

    public static final class Categories implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";

        private Categories() {}
    }

    public static final class Priorities implements BaseColumns {
        public static final String TABLE_NAME = "priorities";
        public static final String COLUMN_LABEL = "label";

        private Priorities() {}
    }

    public static final class Tasks implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DEADLINE = "deadline";
        /**
         * Task status values:
         * 0 = pending
         * 1 = completed
         * 2 = not done
         */
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_PRIORITY_ID = "priority_id";
        public static final String COLUMN_USER_ID = "user_id";

        private Tasks() {}
    }

    public static final class StudySessions implements BaseColumns {
        public static final String TABLE_NAME = "study_sessions";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_DURATION = "duration";

        private StudySessions() {}
    }

    public static final class Attachments implements BaseColumns {
        public static final String TABLE_NAME = "attachments";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_FILE_PATH = "file_path";
        public static final String COLUMN_TYPE = "type";

        private Attachments() {}
    }

    public static final class Notifications implements BaseColumns {
        public static final String TABLE_NAME = "notifications";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_NOTIFY_TIME = "notify_time";
        public static final String COLUMN_IS_SENT = "is_sent";

        private Notifications() {}
    }
}
