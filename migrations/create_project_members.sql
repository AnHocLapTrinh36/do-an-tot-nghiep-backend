IF OBJECT_ID('project_members', 'U') IS NULL
BEGIN
    CREATE TABLE project_members (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        project_id BIGINT NOT NULL,
        student_id BIGINT NOT NULL,
        role NVARCHAR(20) NOT NULL CONSTRAINT df_project_members_role DEFAULT 'MEMBER',
        created_at DATETIME2 NULL,
        CONSTRAINT uk_project_member_project_student UNIQUE (project_id, student_id),
        CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id),
        CONSTRAINT fk_project_members_student FOREIGN KEY (student_id) REFERENCES students(id)
    );
END;
